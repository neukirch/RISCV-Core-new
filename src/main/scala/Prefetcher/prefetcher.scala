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
    val missAddress = Input(UInt(32.W))     //address of miss
    val cacheBusy = Input(Bool())           //is cache working on something
    val cacheValid = Input(Bool())          //does the cache have a valid value
    val idleToCompare = Input(Bool())       //is cache only in idle or was there a miss
    val result = Output(UInt(32.W))         //output of instruction at the requested address
    val hit = Output(Bool())                //show that there was a hit in a buffer
  })

  //setup outputs
  io.hit := false.B
  io.result := 0.U

  //setup for instruction memory
  val IMem = Module(new InstructionMemory(IMemFile))
  IMem.io.instructionAddress := io.missAddress
  IMem.testHarness.setupSignals.setup := 0.B
  IMem.testHarness.setupSignals.address := 0.U
  IMem.testHarness.setupSignals.instruction := 0.U

  //setup finite state machine
  val waitValid :: fetch :: flush :: Nil = Enum(3)
  val state = RegInit(waitValid)

  //setup module for least recently used buffer
  val leastU = Module(new lruModule)
  leastU.io.flush := false.B
  leastU.io.usedValid := false.B
  leastU.io.used := 0.U

  //register to save the next adress to fetch
  val nextAdress = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))


  //multi-way prefetcher with depth 6
  val buffer = VecInit(Seq.fill(4)(Module(new streamBuffer(6, 65)).io))
  for (i <- 0 until 4) {
    buffer(i).flush := false.B //set flush to false so it doesnt go undefined or wrongly flushes

    // Connecting enqueue interface
    buffer(i).enq.valid := false.B //set to true when data is available for enqueue, false otherwise
    buffer(i).enq.bits := 0.U // Data to be enqueued

    // Connecting dequeue interface
    buffer(i).deq.ready := false.B //set to true when ready to receive data, false otherwise
  }

  //register and wire to save which buffer to use
  val fetchBuf = RegInit(0.U(2.W))
  val fetchBufWire = Wire(UInt(2.W))
  fetchBufWire := 0.U

  //variables to save if a buffer is empty or has a hit
  val emptyCheck = Wire(UInt(1.W))
  emptyCheck := 0.U
  val hitCheck = Wire(UInt(1.W))
  hitCheck := 0.U


  switch(state) {
    //state to wait for hit
    is(waitValid) {
      when(io.cacheBusy === true.B) {//wait until cache is busy

        //check if a buffer is empty
        for (i <- 0 until 4) {
          when(buffer(i).count === 0.U) {//if empty
            fetchBufWire := i.U //save which one is empty
            emptyCheck := 1.U //variable to show a buffer is empty
          }
        }

        //check if missAddress is at top of a buffer
        for (i <- 0 until 4) {
          when(buffer(i).head === io.missAddress && buffer(i).count =/= 0.U) {//when hit and buffer not empty
            fetchBufWire := i.U //save which one is a hit, can overwrite the empty one because hit is more important
            hitCheck := 1.U //condition to show a buffer is hit
          }
        }

        fetchBuf := fetchBufWire //save buffer to use to the register

        when(hitCheck === true.B) { //hit in a buffer
          when(io.cacheValid === true.B) {//hit in cache and in buffer, dequeue element and start fetching to buffer
            buffer(fetchBufWire).deq.ready := true.B //start dequeue
            IMem.io.instructionAddress := nextAdress(fetchBufWire) //set next address to fetch
            state := fetch //go to fetch state
          }.otherwise {//hit in buffer but not in cache, dequeue element to output and start fetching to buffer
            buffer(fetchBufWire).deq.ready := true.B //start dequeue
            io.result := buffer(fetchBufWire).deq.bits(31, 0) //output data
            IMem.io.instructionAddress := nextAdress(fetchBufWire)//set next adress to fetch
            io.hit := true.B//set hit to true to show there was a hit in a buffer
            state := fetch //go to fetch state
          }

        }.elsewhen(emptyCheck === true.B) {//no hit, but a buffer is empty, start fetching at adress after miss
          IMem.io.instructionAddress := io.missAddress + 4.U //set adress to fetch
          nextAdress(fetchBufWire) := io.missAddress + 4.U//update register
          state := fetch //go to fetch state
        }.otherwise { //no hit, no buffer empty, need to flush a buffer
          leastU.io.flush := true.B //set flush signal to true to get which buffer is lru to flush
          state := flush //go to flush state
        }
      }
    }
    is(fetch) { //prefetch state
      when(buffer(fetchBuf).count =/= 6.U && io.idleToCompare === false.B) { //if buffer is not full and there is no new miss
        //update lru
        leastU.io.usedValid := true.B
        leastU.io.used := fetchBuf

        //enqueue
        buffer(fetchBuf).enq.valid := true.B
        buffer(fetchBuf).enq.bits := Cat(nextAdress(fetchBuf), 1.U, IMem.io.instruction)

        IMem.io.instructionAddress := nextAdress(fetchBuf) + 4.U//next address to fetch
        nextAdress(fetchBuf) := nextAdress(fetchBuf) + 4.U //update register
      }.elsewhen(buffer(fetchBuf).count =/= 6.U && io.idleToCompare === true.B){ //buffer not full but new miss
        //update lru
        leastU.io.usedValid := true.B
        leastU.io.used := fetchBuf

        //enqueue
        buffer(fetchBuf).enq.valid := true.B
        buffer(fetchBuf).enq.bits := Cat(nextAdress(fetchBuf) , 1.U, IMem.io.instruction)

        //analog to waitValid state
        //check if a buffer is empty
        for (i <- 0 until 4) {
          when(buffer(i).count === 0.U) {//if empty
            fetchBufWire := i.U //save which one is empty
            emptyCheck := 1.U //variable to show a buffer is empty
          }
        }

        //check if missAddress is at top of a buffer
        for (i <- 0 until 4) {
          when(buffer(i).head === io.missAddress && buffer(i).count =/= 0.U) {//when hit and buffer not empty
            fetchBufWire := i.U //save which one is a hit, can overwrite the empty one because hit is more important
            hitCheck := 1.U //condition to show a buffer is hit
          }
        }

        fetchBuf := fetchBufWire //save buffer to use to the register

          when(hitCheck === true.B) { //hit in a buffer, can keep fetching
            when(io.cacheValid === true.B) {//hit in cache and in buffer, dequeue element and start fetching to buffer
              buffer(fetchBufWire).deq.ready := true.B //start dequeue
              IMem.io.instructionAddress := nextAdress(fetchBufWire) + 4.U//set next address to fetch
              nextAdress(fetchBufWire) := nextAdress(fetchBufWire) + 4.U//update register
              state := fetch //go to fetch state
            }.otherwise {
            buffer(fetchBufWire).deq.ready := true.B //start dequeue
            io.result := buffer(fetchBufWire).deq.bits(31, 0) //output data
            IMem.io.instructionAddress := nextAdress(fetchBufWire) + 4.U //set next address to fetch
            nextAdress(fetchBufWire) := nextAdress(fetchBufWire) + 4.U//update register
            io.hit := true.B//set hit to true to show there was a hit in a buffer
              state := fetch //go to fetch state
          }
        }.elsewhen(emptyCheck === true.B) { //no hit, but a buffer is empty, start fetching at adress after miss
            IMem.io.instructionAddress := io.missAddress + 4.U //set adress to fetch
            nextAdress(fetchBufWire) := io.missAddress + 4.U//update register
          state := fetch//go to fetch state
        }.otherwise { //no hit, no buffer empty, need to flush a buffer
          leastU.io.flush := true.B //set flush signal to true to get which buffer is lru to flush
          state := flush //go to flush state
        }
      }.otherwise { //if no miss occurs and the buffer is fully  fetched go to idle state and wait for miss
          state := waitValid //idle state
        }
      }

    is(flush) { //flush state
      buffer(leastU.io.out).flush := true.B//update lru
      fetchBuf := leastU.io.out //save which buffer was flushed to fetch into it

      nextAdress(leastU.io.out) := io.missAddress + 4.U//update register
      IMem.io.instructionAddress := io.missAddress + 4.U//set adress to fetch
      state := fetch//go to fetch state
    }
  }
}
