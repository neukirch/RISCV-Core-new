package Prefetcher

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

import ICache._
import DCache.Cache
import InstructionMemory._

class prefetcher(IMemFile: String) extends Module {
  val io = IO(new Bundle {
    val missAddress = Input(UInt(32.W)) // address to be fetched and saved
    val cacheBusy = Input(Bool())
    val cacheValid = Input(Bool())
    val result = Output(UInt(32.W))
    val hit = Output(Bool())
    val idleToCompare = Input(Bool())
  })

  io.hit := false.B

  val IMem = Module(new InstructionMemory(IMemFile))
  IMem.io.instructionAddress := io.missAddress
  IMem.testHarness.setupSignals.setup := 0.B
  IMem.testHarness.setupSignals.address := 0.U
  IMem.testHarness.setupSignals.instruction := 0.U


  val waitValid :: fetch :: flush :: Nil = Enum(3)
  val state = RegInit(waitValid)
  val nextAddress = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))

  io.result := 0.U

  //multi-way prefetcher
  val buffer = VecInit(Seq.fill(4)(Module(new streamBuffer(6, 65)).io))
  for (i <- 0 until 4) {
    buffer(i).flush := false.B //set flush to false so it doesnt go undefined or wrongly flushes

    // Connecting enqueue interface
    buffer(i).enq.valid := false.B // Set to true when data is available for enqueue
    buffer(i).enq.bits := 0.U // Data to be enqueued

    // Connecting dequeue interface
    buffer(i).deq.ready := false.B // Set to true when ready to receive data
  }

  val fetchBuf = RegInit(0.U(2.W))
  val fetchBufWire = Wire(UInt(2.W))
  fetchBufWire := 0.U

  val emptyCheck = Wire(UInt(1.W))
  emptyCheck := 0.U
  val hitCheck = Wire(UInt(1.W))
  hitCheck := 0.U

  val leastU = Module(new lruModule)
  leastU.io.flush := false.B
  leastU.io.usedValid := false.B
  leastU.io.used := 0.U

  printf(p"nextAddress0: ${nextAddress(0)}, nextAddress1: ${nextAddress(1)}, nextAddress2: ${nextAddress(2)}, nextAddress3: ${nextAddress(3)}\n")
  printf(p"heads buf0: ${buffer(0).head}, buf1: ${buffer(1).head}, buf2: ${buffer(2).head}, buf3: ${buffer(3).head}\n")
  printf(p"next deq buf0: 0x${Hexadecimal(buffer(0).deq.bits(31, 0))}, buf1: 0x${Hexadecimal(buffer(1).deq.bits(31, 0))}, buf2: 0x${Hexadecimal(buffer(2).deq.bits(31, 0))}, buf3: 0x${Hexadecimal(buffer(3).deq.bits(31, 0))}\n")
  printf(p"count buf0: ${buffer(0).count}, buf1: ${buffer(1).count}, buf2: ${buffer(2).count}, buf3: ${buffer(3).count}\n")
//  printf(p"io.cacheBusy: ${io.cacheBusy},    io.cacheValid: ${io.cacheValid},   address: ${io.missAddress}\n")
  switch(state) {
    is(waitValid) {
      //printf(p"prefetcher waitValid state\n")
      when(io.cacheBusy === true.B) {
        //printf(p"prefetcher waitValid state\n")
        for (i <- 0 until 4) {
          when(buffer(i).count === 0.U) { //if empty
            fetchBufWire := i.U // save which  one is empty
            emptyCheck := 1.U //condition to show a buffer is empty
          }
        }

        //check if missAddress is at top of a buffer
        for (i <- 0 until 4) {
          when(buffer(i).head === io.missAddress && buffer(i).count =/= 0.U) { //when hit and buffer not empty
            fetchBufWire := i.U // save which  one is a hit, can overwrite the empty one because hit is more important
            hitCheck := 1.U //condition to show a buffer is hit
          }
        }

        fetchBuf := fetchBufWire //register to save which buffer to use

        when(hitCheck === true.B) { //hit
          when(io.cacheValid === true.B) {
            //printf(p"prefetcher hit, cache valid fetched: ${nextAddress(fetchBufWire)}\n")
            //hit in cache and in buffer, so dequeue element and go back to waiting for next miss
            buffer(fetchBufWire).deq.ready := true.B //start dequeue
            IMem.io.instructionAddress := nextAddress(fetchBufWire) //set next address to fetch
            //nextAddress(fetchBufWire) := nextAddress(fetchBufWire) + 4.U
            state := fetch //go to fetch state
          }.otherwise {
            buffer(fetchBufWire).deq.ready := true.B //start dequeue
            io.result := buffer(fetchBufWire).deq.bits(31, 0) //output data
            IMem.io.instructionAddress := nextAddress(fetchBufWire) //set next address to fetch
            //nextAddress(fetchBufWire) := nextAddress(fetchBufWire) + 4.U
            io.hit := true.B
            state := fetch //go to fetch state
            //printf(p"prefetcher hit, cache miss fetched: ${nextAddress(fetchBufWire)}\n")
          }

        }.elsewhen(emptyCheck === true.B) { //no hit and one empty
          IMem.io.instructionAddress := io.missAddress + 4.U
          nextAddress(fetchBufWire) := io.missAddress + 4.U
          //printf(p"prefetcher empty\n")
          state := fetch
        }.otherwise { //flush, no hit no empty
          //printf(p"prefetcher flush\n")
          leastU.io.flush := true.B //set signal to true to get which is lru to flush
          state := flush //go to flush state
        }
      }
    }
    is(fetch) { //prefetch state
      //printf(p"prefetcher fetch state\n")
      //printf(p"prefetcher fetch state miss address: ${io.missAddress}\n")
      //printf(p"buf0: ${buffer(0).head}, buf1: ${buffer(1).head}, buf2: ${buffer(2).head}, buf3: ${buffer(3).head}\n")
      //printf(p"buf0: 0x${Hexadecimal(buffer(0).deq.bits(31, 0))}, buf1: 0x${Hexadecimal(buffer(1).deq.bits(31, 0))}, buf2: 0x${Hexadecimal(buffer(2).deq.bits(31, 0))}, buf3: 0x${Hexadecimal(buffer(3).deq.bits(31, 0))}\n")
      //printf(p"buf0: ${buffer(0).count}, buf1: ${buffer(1).count}, buf2: ${buffer(2).count}, buf3: ${buffer(3).count}\n")
      //when(buffer(fetchBuf).count =/= 6.U && io.cacheBusy === true.B && io.cacheValid === false.B) { //until buffer is full or new cache miss occurs
      when(buffer(fetchBuf).count =/= 6.U && io.idleToCompare === false.B) {
        //update lru
        leastU.io.usedValid := true.B
        leastU.io.used := fetchBuf

        //enqueue
        buffer(fetchBuf).enq.valid := true.B
        //buffer(fetchBuf).enq.bits := Cat(nextAddress(fetchBuf) - 4.U, 1.U, IMem.io.instruction)
        //printf(p"----------------fetched address: ${(nextAddress(fetchBuf) - 4.U)}, instruction: 0x${Hexadecimal(IMem.io.instruction)}--------------\n")
        buffer(fetchBuf).enq.bits := Cat(nextAddress(fetchBuf), 1.U, IMem.io.instruction)
        //printf(p"----------------fetched address: ${(nextAddress(fetchBuf))}, instruction: 0x${Hexadecimal(IMem.io.instruction)}--------------\n")
        IMem.io.instructionAddress := nextAddress(fetchBuf) + 4.U// << 2.U //next address to fetch
        nextAddress(fetchBuf) := nextAddress(fetchBuf) + 4.U //1.U
        //printf(p"next fetch: ${(nextAddress(fetchBuf))/4.U}\n")
      }.elsewhen(buffer(fetchBuf).count =/= 6.U && io.idleToCompare === true.B){
        leastU.io.usedValid := true.B
        leastU.io.used := fetchBuf

        //enqueue
        buffer(fetchBuf).enq.valid := true.B
        buffer(fetchBuf).enq.bits := Cat(nextAddress(fetchBuf) , 1.U, IMem.io.instruction)
        //printf(p"fetching xxxxxxxfetched address: ${nextAddress(fetchBuf) }, instruction: 0x${Hexadecimal(IMem.io.instruction)}xxxxxxxxxxxxxxx\n")
        //printf(p"fetched address: ${(nextAddress(fetchBuf) - 4.U)/4.U}, instruction: 0x${Hexadecimal(IMem.io.instruction)}\n")
        //printf(p"fetch io.idleToCompare: ${io.idleToCompare}\n")
        for (i <- 0 until 4) {
          when(buffer(i).count === 0.U) { //if empty
            fetchBufWire := i.U // save which  one is empty
            emptyCheck := 1.U //condition to show a buffer is empty
          }
        }

        //check if missAddress is at top of a buffer
        for (i <- 0 until 4) {
          when(buffer(i).head === io.missAddress && buffer(i).count =/= 0.U) { //when hit and buffer not empty
            fetchBufWire := i.U // save which  one is a hit, can overwrite the empty one because hit is more important
            hitCheck := 1.U //condition to show a buffer is hit
          }
        }

        fetchBuf := fetchBufWire //register to save which buffer to use

        when(hitCheck === true.B) { //hit
          when(io.cacheValid === true.B) {
            //printf(p"fetching prefetcher hit, cache valid fetched: ${nextAddress(fetchBufWire) + 4.U}\n")
            //hit in cache and in buffer, so dequeue element and go back to waiting for next miss
            buffer(fetchBufWire).deq.ready := true.B //start dequeue
            IMem.io.instructionAddress := nextAddress(fetchBufWire) + 4.U //set next address to fetch
            nextAddress(fetchBufWire) := nextAddress(fetchBufWire) + 4.U
            //nextAddress(fetchBufWire) := nextAddress(fetchBufWire) + 4.U
            state := fetch //go to fetch state
          }.otherwise {
            buffer(fetchBufWire).deq.ready := true.B //start dequeue
            io.result := buffer(fetchBufWire).deq.bits(31, 0) //output data
            IMem.io.instructionAddress := nextAddress(fetchBufWire) + 4.U //set next address to fetch
            nextAddress(fetchBufWire) := nextAddress(fetchBufWire) + 4.U
            //nextAddress(fetchBufWire) := nextAddress(fetchBufWire) + 4.U
            io.hit := true.B
            state := fetch //go to fetch state
            //printf(p"fetching prefetcher hit, cache miss fetch: ${nextAddress(fetchBufWire) + 4.U}\n")
          }

        }.elsewhen(emptyCheck === true.B) { //no hit and one empty
          //IMem.io.instructionAddress := io.missAddress + 4.U
          //nextAddress(fetchBufWire) := io.missAddress + 8.U
          IMem.io.instructionAddress := io.missAddress + 4.U
          nextAddress(fetchBufWire) := io.missAddress + 4.U
          //printf(p"fetching prefetcher empty ${io.missAddress}\n")
          state := fetch
        }.otherwise { //flush, no hit no empty
          //printf(p"fetching prefetcher flush\n")
          leastU.io.flush := true.B //set signal to true to get which is lru to flush
          state := flush //go to flush state
        }
        //1.U
      }.otherwise { //if no miss occurs and the buffer is fully  fetched
        //printf(p"prefetcher fetch state to waitValid\n")
          state := waitValid //idle state
        }
      }

    is(flush) { //flush state
      printf(p"\n")
      printf(p"\n")
      printf(p"\n")
      printf(p"------------------------------------------prefetcher flush state------------------------------------------\n")
      printf(p"\n")
      printf(p"\n")
      printf(p"\n")
      //update  lru
      buffer(leastU.io.out).flush := true.B
      nextAddress(leastU.io.out) := io.missAddress + 4.U
      fetchBuf := leastU.io.out //save which buffer was flushed to fetch into it
      IMem.io.instructionAddress := io.missAddress + 4.U
      state := fetch
    }
  }
}
