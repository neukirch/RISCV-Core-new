package Prefetcher

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

class lruModule extends Module{
  val io = IO(new Bundle {
    val flush = Input(Bool())
    val usedValid = Input(Bool())
    val used = Input(UInt(2.W))
    val out = Output(UInt(2.W))
  })

  val lru = RegInit(VecInit(Seq(4.U(8.W), 3.U(8.W), 2.U(8.W), 1.U(8.W))))//lru register vec with start values
  val highest = RegInit(0.U(2.W)) //register  with current lru buffer
  val highestNext = RegInit(0.U(2.W)) //register  with next lru after current is used
  
  when(io.usedValid === true.B){ //counts every register up except the one used
    for(i <- 0 until 4){
      when(i.asUInt =/= io.used){
        lru(i) := lru(i) + 1.U
      }
    }
    lru(io.used) := 0.U
  }

  io.out := MuxCase(0.U, Array(
    (lru(0) >= lru(1) && lru(0) >= lru(2) && lru(0) >= lru(3)) -> 0.U,
    (lru(1) >= lru(0) && lru(1) >= lru(2) && lru(1) >= lru(3)) -> 1.U,
    (lru(2) >= lru(0) && lru(2) >= lru(1) && lru(2) >= lru(3)) -> 2.U,
    (lru(3) >= lru(0) && lru(3) >= lru(1) && lru(3) >= lru(2)) -> 3.U
  ))
}
