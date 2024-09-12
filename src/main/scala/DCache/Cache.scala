package DCache

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental._
import firrtl.annotations.MemoryLoadFileType

class Cache (CacheFile: String, read_only: Boolean = false) extends Module{
  val io = IO(new Bundle {
    val write_en = if (!read_only) Some(Input(Bool())) else None
    val read_en = Input(Bool())
    val data_addr = Input(UInt(32.W))
    val data_in = if (!read_only) Some(Input(UInt(32.W))) else None
    val data_out = Output(UInt(32.W))
    val valid = Output(Bool())
    val busy = Output(Bool())

    val mem_write_en = Output(Bool())
    val mem_read_en = Output(Bool())
    val mem_data_in = Output(UInt(32.W))
    val mem_data_addr = Output(UInt(32.W))
    val mem_data_out = Input(UInt(32.W))

    //added here
    val hit = Input(Bool())
    val prefData = Input(UInt(32.W))
    val idleToCompare = Output(Bool())
  })

  val scalaReadOnlyBool = if(read_only) true.B else false.B
  val write_en_reg = RegInit(false.B)
  val read_en_reg = RegInit(false.B)
  val data_addr_reg = Reg(UInt(32.W))
  val data_in_reg = if (!read_only) Some(Reg(UInt(32.W))) else None

  val cacheLines = 64.U // cache lines as a variable
  val idle :: compare :: writeback :: allocate :: Nil = Enum(4)//allocatePrefetcher :: Nil = Enum(5)
  val stateReg = RegInit(idle)
  //printf(p"state     anfang: ${stateReg}\n")
  val index = Reg(UInt(6.W)) // stores the current cache index in a register to use in later states

  //ADDED HERE
  val index_wire = WireInit(0.asUInt(6.W))
  //

  val data_element = Reg(UInt(58.W)) // stores the loaded cache element in a register to use in later states
  val data_element_wire = WireInit(0.asUInt(58.W)) // stores the loaded cache element in a wire to use in the same state
  val statecount = Reg(Bool()) // waiting for 1 cycle in allocate state

  val cache_data_array = Mem(64, UInt(58.W))  // give here 64 as a variable
  loadMemoryFromFileInline(cache_data_array, CacheFile, MemoryLoadFileType.Binary)

  io.data_out := 0.U
  io.valid := 0.B
  io.busy := (stateReg =/= idle)
  //ADDED HERE
  io.idleToCompare := false.B

  io.mem_write_en := 0.B
  io.mem_read_en := 0.B
  io.mem_data_in := 0.U
  io.mem_data_addr := 0.U
  //printf(p"state0: ${stateReg}\n")
  //printf(p"ANFANG busy: ${io.busy},    valid: ${io.valid}\n")
  switch(stateReg) {
    is(idle) {
      //printf(p"state1: ${stateReg}\n")
      //printf(p"idle state\n")
      io.data_out := data_element(31, 0)
      when(io.read_en || io.write_en.getOrElse(false.B)) {
        stateReg := compare
        write_en_reg := io.write_en.getOrElse(false.B)
        read_en_reg := io.read_en
        data_addr_reg := io.data_addr
        if (!read_only) data_in_reg.foreach(_ := io.data_in.get)
        statecount := false.B
        //ADDED HERE
        io.idleToCompare := true.B
      }
    }

    is(compare) {
      //printf(p"state2: ${stateReg}\n")
      //printf(p"compare state, miss address: ${data_addr_reg}\n")
      index := (data_addr_reg / 4.U) % cacheLines
      index_wire := (data_addr_reg / 4.U) % cacheLines
      data_element_wire := cache_data_array((data_addr_reg / 4.U) % cacheLines).asUInt
      data_element := data_element_wire

      when(data_element_wire(57) && (data_element_wire(55, 32).asUInt === data_addr_reg(31, 8).asUInt)) {
        //printf(p"state3: ${stateReg}\n")
        //printf(p"compare state cache hit-------------------------------\n")
        stateReg := idle
        io.valid := true.B
        when(read_en_reg) {
          io.data_out := data_element_wire(31, 0)
        }
        if (!read_only) {
          when(write_en_reg) {
            val temp = Wire(Vec(58, Bool()))
            temp := 0.U(58.W).asBools
            temp(57) := true.B
            temp(56) := true.B // set dirty bit

            for (i <- 0 until 32) {
              temp(i) := data_in_reg.get(i)
            } // new data is stored
            for (i <- 32 until 56) {
              temp(i) := data_element_wire(i)
            } // the tag remains the same
            cache_data_array(index) := temp.asUInt
          }
        }
        //CHANGES HERE
      }.elsewhen(io.hit){
        //printf(p"state4: ${stateReg}\n")
        //printf(p"compare state cache miss but prefetcher hit\n")
        //stateReg := allocatePrefetcher
        io.mem_read_en := false.B
        val temp = Wire(Vec(58, Bool()))
        temp := 0.U(58.W).asBools
        for (i <- 0 until 32) { temp(i) := io.prefData(i) }
        temp(56) := false.B
        temp(57) := true.B
        for (i <- 32 until 56) { temp(i) := data_addr_reg(i - 24) }
        cache_data_array(index_wire) := temp.asUInt
        io.data_out := io.prefData
        stateReg := compare
        //
      }.otherwise {
        //printf(p"state5: ${stateReg}\n")
        //printf(p"compare state no hit\n")
        if(!read_only) {
          when(data_element_wire(56) && data_element_wire(57)) {
            stateReg := writeback
            // printf(p"state6: ${stateReg}\n")
          }.otherwise {
            stateReg := allocate
            //printf(p"state7: ${stateReg}\n")
          }
        }
        else {
          stateReg := allocate
          //printf(p"state8: ${stateReg}\n")
        }

      }
    }
//    is(allocatePrefetcher) {
//      //printf(p"state9: ${stateReg}\n")
//      printf(p"allocatePrefetcher\n")
//      io.mem_read_en := false.B
//      val temp = Wire(Vec(58, Bool()))
//      temp := 0.U(58.W).asBools
//      for (i <- 0 until 32) { temp(i) := io.prefData(i) }
//      temp(56) := false.B
//      temp(57) := true.B
//      for (i <- 32 until 56) { temp(i) := data_addr_reg(i - 24) }
//      cache_data_array(index) := temp.asUInt
//      io.data_out := io.prefData
//      stateReg := compare
//    }

    is(writeback) {
      //printf(p"state10: ${stateReg}\n")
      //printf(p"writeback state\n")
      io.mem_write_en := true.B
      io.mem_read_en := false.B
      val temp = Wire(Vec(32, Bool()))
      temp := 0.U(32.W).asBools // the address where the dirty cache element should be stored in memory
      temp(1) := false.B
      temp(0) := false.B // first two bits 0 as byte offset
      for (i <- 2 until 8) { temp(i) := index.asBools(i - 2) } // next 6 bits from index
      for (i <- 8 until 32) { temp(i) := data_element(i + 24) } // the rest 24 bits from the tag
      io.mem_data_addr := temp.asUInt
      io.mem_data_in := data_element(31, 0) // write the data in the dirty element to the memory
      stateReg := allocate
    }

    is(allocate) {
      //printf(p"state11: ${stateReg}\n")
      when(statecount) {
        //printf(p"allocate state 2nd cycle\n")
        statecount := false.B
        io.mem_read_en := false.B
        val temp = Wire(Vec(58, Bool()))
        temp := 0.U(58.W).asBools
        for (i <- 0 until 32) { temp(i) := io.mem_data_out(i) }
        temp(56) := false.B
        temp(57) := true.B
        for (i <- 32 until 56) { temp(i) := data_addr_reg(i - 24) }
        cache_data_array(index) := temp.asUInt
        stateReg := compare
      }.otherwise {
        //printf(p"allocate state 1st cycle\n")
        statecount := true.B
        io.mem_read_en := true.B
        io.mem_write_en := false.B
        io.mem_data_addr := data_addr_reg
      }
    }
  }
  //printf(p"state     ende: ${stateReg}\n")
  //printf(p"ENDE busy: ${io.busy},    valid: ${io.valid}\n")
}
