
package prefetchTB

import chisel3._
import chisel3.util._
import chiseltest._
//import Prefetcher._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.control.Breaks._
import RISCV_TOP._


class fir_test extends AnyFlatSpec with ChiselScalatestTester {

  "fir_test" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      println(f"---------------------------------------------fir_test start---------------------------------------------\n")
      var pc = 0
      var counter = 0
      var test = 0

      val expectedValues = Seq(
        "h00000093".U, "h00100093".U, "h00200093".U, "h00300093".U,
        "h00400093".U, "h00500093".U, "h00600093".U, "h00700093".U,
        "h00800093".U, "h00900093".U, "h00A00093".U, "h00B00093".U,
        "h00C00093".U, "h00D00093".U, "h00E00093".U, "h00F00093".U,
        "h01000093".U, "h01100093".U, "h01200093".U, "h01300093".U,
        "h01400093".U, "h01500093".U, "h01600093".U, "h01700093".U,
        "h01800093".U, "h01900093".U, "h01A00093".U, "h01B00093".U,
        "h01C00093".U, "h01D00093".U, "h01E00093".U, "h01F00093".U
      )


      var imemOut = dut.io.IMEMOut.peek().litValue

      breakable {
        for(i <- 0 until 1000) {
          if(dut.io.IMEMCacheBusy.peek().litToBoolean){
            if(dut.io.IMEMCacheValid.peek().litToBoolean) {
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128)/4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
              if(pc == 24 || pc == 68 || pc == 112 || pc == 156 || pc == 200 || pc == 244 ||
                pc == 288 || pc == 332 || pc == 376 || pc == 420 || pc == 464 ||
                pc == 508 || pc == 552 || pc == 596){
                pc += 20
              }
              else{
                pc += 4
              }
              counter += 1
            }
          }
          dut.io.IMEMAddr.poke((pc).U)

          if (pc >155*4) {
            break()  // Exit the loop early
          }
          dut.clock.step(1)
        }
        println(s"fir_test ENDE at i: 1000, counter: ${counter},-------------------------------------------------------------\n\n\n\n")
      }
    }
  }
}



class linear_test extends AnyFlatSpec with ChiselScalatestTester {

  "linear_test" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      println(f"---------------------------------------------linear_test start---------------------------------------------\n")
      var pc = 0
      var counter = 0
      var test = 0

      val expectedValues = Seq(
        "h00000093".U, "h00100093".U, "h00200093".U, "h00300093".U,
        "h00400093".U, "h00500093".U, "h00600093".U, "h00700093".U,
        "h00800093".U, "h00900093".U, "h00A00093".U, "h00B00093".U,
        "h00C00093".U, "h00D00093".U, "h00E00093".U, "h00F00093".U,
        "h01000093".U, "h01100093".U, "h01200093".U, "h01300093".U,
        "h01400093".U, "h01500093".U, "h01600093".U, "h01700093".U,
        "h01800093".U, "h01900093".U, "h01A00093".U, "h01B00093".U,
        "h01C00093".U, "h01D00093".U, "h01E00093".U, "h01F00093".U
      )


        var imemOut = dut.io.IMEMOut.peek().litValue

      breakable {
        for(i <- 0 until 1000) {
          if(dut.io.IMEMCacheBusy.peek().litToBoolean){
            if(dut.io.IMEMCacheValid.peek().litToBoolean) {
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128)/4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
              pc += 4
              counter += 1
            }
          }

          dut.io.IMEMAddr.poke((pc).U)

          if (dut.io.IMEMOut.peek().litValue == 51) {
            println(s"linear_test BREAK at i: $i, counter: ${counter},-------------------------------------------------------------\n\n\n\n")
            break()  // Exit the loop early
          }
          dut.clock.step(1)
        }
      }
    }
  }
}


class loop_test extends AnyFlatSpec with ChiselScalatestTester {

  "loop_test" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      //println(f"---------------------------------------------loop_test start---------------------------------------------\n")
      var pc = 0
      var counter = 0
      var test = 0
      val expectedValues = Seq(
        "h00000093".U, "h00100093".U, "h00200093".U, "h00300093".U,
        "h00400093".U, "h00500093".U, "h00600093".U, "h00700093".U,
        "h00800093".U, "h00900093".U, "h00A00093".U, "h00B00093".U,
        "h00C00093".U, "h00D00093".U, "h00E00093".U, "h00F00093".U,
        "h01000093".U, "h01100093".U, "h01200093".U, "h01300093".U,
        "h01400093".U, "h01500093".U, "h01600093".U, "h01700093".U,
        "h01800093".U, "h01900093".U, "h01A00093".U, "h01B00093".U,
        "h01C00093".U, "h01D00093".U, "h01E00093".U, "h01F00093".U
      )


      var imemOut = dut.io.IMEMOut.peek().litValue


      //      println(f"loop_test start---------------xxxxxxxxxxx-----------------\n\n\n\n\n\n")
      breakable {
        for(i <- 0 until 1000) {
          if(dut.io.IMEMCacheBusy.peek().litToBoolean){
            if(dut.io.IMEMCacheValid.peek().litToBoolean) {
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128)/4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
              counter += 1
              if(pc == 12*4){
                pc = 20*4
              }
              else if(pc == 32*4){
                pc = 40*4
              }
              else if(pc == 52*4){
                pc = 60*4
              }//all buffers full, flush
              else if(pc== 72*4){
                pc = 80*4
              }
              else if(pc== 92*4){
                pc = 100*4
              }
              else if (pc == 120*4 || i == 999){
                println(s"loop_test BREAK at i: ${i},counter: ${counter}----------------------------------\n")
                break()  // Exit the loop early
              }
              else{
                pc += 4
              }
            }
          }
          dut.io.IMEMAddr.poke((pc).U)
          dut.clock.step(1)
        }
      }


    }
  }
}


class cache_test extends AnyFlatSpec with ChiselScalatestTester {

  "cache_test" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      //println(f"---------------------------------------------cache_test start---------------------------------------------\n")
      var pc = 0
      var counter = 0
      var test = 0

      val expectedValues = Seq(
        "h00000093".U, "h00100093".U, "h00200093".U, "h00300093".U,
        "h00400093".U, "h00500093".U, "h00600093".U, "h00700093".U,
        "h00800093".U, "h00900093".U, "h00A00093".U, "h00B00093".U,
        "h00C00093".U, "h00D00093".U, "h00E00093".U, "h00F00093".U,
        "h01000093".U, "h01100093".U, "h01200093".U, "h01300093".U,
        "h01400093".U, "h01500093".U, "h01600093".U, "h01700093".U,
        "h01800093".U, "h01900093".U, "h01A00093".U, "h01B00093".U,
        "h01C00093".U, "h01D00093".U, "h01E00093".U, "h01F00093".U
      )


      var imemOut = dut.io.IMEMOut.peek().litValue

      breakable {
        for(i <- 0 until 1000) {
          if(dut.io.IMEMCacheBusy.peek().litToBoolean){
            if(dut.io.IMEMCacheValid.peek().litToBoolean) {
              counter += 1
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128)/4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
              if(pc == 12 && counter < 15){
                pc = 0
              }
              else if (pc == 36){
                println(s"cache_test BREAK at i: ${i}, counter: ${counter}-------------------------------\n")
                break()  // Exit the loop early
              }
              else{
                pc += 4
              }
            }
          }
          dut.io.IMEMAddr.poke((pc).U)
          dut.clock.step(1)
        }
      }
    }
  }
}


class random_test extends AnyFlatSpec with ChiselScalatestTester {

  "random_test" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      //println(f"---------------------------------------------random_test start---------------------------------------------\n")
      var pc = 0
      var counter = 0
      var test = 0

      val expectedValues = Seq(
        "h00000093".U, "h00100093".U, "h00200093".U, "h00300093".U,
        "h00400093".U, "h00500093".U, "h00600093".U, "h00700093".U,
        "h00800093".U, "h00900093".U, "h00A00093".U, "h00B00093".U,
        "h00C00093".U, "h00D00093".U, "h00E00093".U, "h00F00093".U,
        "h01000093".U, "h01100093".U, "h01200093".U, "h01300093".U,
        "h01400093".U, "h01500093".U, "h01600093".U, "h01700093".U,
        "h01800093".U, "h01900093".U, "h01A00093".U, "h01B00093".U,
        "h01C00093".U, "h01D00093".U, "h01E00093".U, "h01F00093".U
      )


      var imemOut = dut.io.IMEMOut.peek().litValue

      val random = scala.util.Random
        for(i <- 0 until 200) {
          if(dut.io.IMEMCacheBusy.peek().litToBoolean){
            if(dut.io.IMEMCacheValid.peek().litToBoolean) {
              counter += 1
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128)/4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
              pc = random.nextInt(192) * 4 //only 192 because 00000033 not in seq
            }
          }
          dut.io.IMEMAddr.poke((pc).U)
          dut.clock.step(1)
        }
      println(s"random_test ENDE i=200, counter: ${counter}-----------------------------------------\n")
    }
  }


