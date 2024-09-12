package PrefICacheIMem

import ICache._
import Prefetcher._
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
//import DCache.Cache
//import InstructionMemory.InstructionMemory
import config.IMEMsetupSignals

class PrefICacheIMem (IMemFile : String) extends Module {

  val testHarness = IO(
    new Bundle {
      val setupSignals = Input(new IMEMsetupSignals)
      val requestedAddress = Output(UInt())
    })

  val io = IO(new Bundle {
    val instr_addr = Input(UInt(32.W)) // address to be fetched and saved
    val instr_out = Output(UInt(32.W))
    val valid = Output(Bool())
    val busy = Output(Bool())
  })

  val icacheimem = Module(new ICacheAndIMemory(IMemFile))
  val pref = Module(new prefetcher(IMemFile))

  pref.io.idleToCompare := icacheimem.io.idleToCompare

  //pref.io.missAddress := io.instr_addr
  pref.io.missAddress := testHarness.setupSignals.address
  pref.io.cacheBusy := icacheimem.io.busy
  pref.io.cacheValid := icacheimem.io.valid
  io.instr_out := pref.io.result
  icacheimem.io.hit := pref.io.hit
  icacheimem.io.prefData := pref.io.result


  //icacheimem.io.instr_addr := io.instr_addr
  icacheimem.io.instr_addr := testHarness.setupSignals.address
  io.instr_out := icacheimem.io.instr_out
  io.valid := icacheimem.io.valid
  io.busy := icacheimem.io.busy

  icacheimem.testHarness.setupSignals := testHarness.setupSignals
  testHarness.requestedAddress := icacheimem.testHarness.requestedAddress
}