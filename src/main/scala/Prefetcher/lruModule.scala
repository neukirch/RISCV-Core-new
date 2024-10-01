package Prefetcher

import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

class lruModule extends Module{
  val io = IO(new Bundle {
    val flush = Input(Bool())               //variable to show a buffer has been flushed
    val usedValid = Input(Bool())           //shows that a buffer has been used
    val used = Input(UInt(2.W))             //which buffer has been used
    val out = Output(UInt(2.W))             //the least recently used buffer
  })

  val lru = RegInit(VecInit(Seq(4.U(8.W), 3.U(8.W), 2.U(8.W), 1.U(8.W))))//lru register vector with start values
  val highest = RegInit(0.U(2.W)) //register with current lru buffer
  val highestNext = RegInit(0.U(2.W)) //register  with next lru after current is used
  
  when(io.usedValid === true.B){ //counts every register up except the one used
    for(i <- 0 until 4){
      when(i.asUInt =/= io.used){
        lru(i) := lru(i) + 1.U
      }
    }
    lru(io.used) := 0.U //sets register for the used buffer to 0
  }

  io.out := MuxCase(0.U, Array(
    (lru(0) >= lru(1) && lru(0) >= lru(2) && lru(0) >= lru(3)) -> 0.U, //wenn lru(0)>alle anderen wird 0 ausgegeben
    (lru(1) >= lru(0) && lru(1) >= lru(2) && lru(1) >= lru(3)) -> 1.U, //bei lru(1) out = 1
    (lru(2) >= lru(0) && lru(2) >= lru(1) && lru(2) >= lru(3)) -> 2.U, //analog
    (lru(3) >= lru(0) && lru(3) >= lru(1) && lru(3) >= lru(2)) -> 3.U  //analog
  ))
}
