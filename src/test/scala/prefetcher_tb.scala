
package prefetchTB

import chisel3._
import chisel3.util._
import chiseltest._
//import Prefetcher._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.control.Breaks._
import RISCV_TOP._


class fir_test_no extends AnyFlatSpec with ChiselScalatestTester {

  "fir_test no prefetcher" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      var pc = 0
      var counter = 0
      var test = 0
      dut.io.cacheOnly.poke(true.B)

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
            println(f"fir_test ohne prefetcher BREAK at i: ${i}, counter: ${counter},--------------------------\n\n\n\n")
            break()  // Exit the loop early
          }
          dut.clock.step(1)
        }
        println(f"fir_test ohne prefetcher ENDE at i: 1000, counter: ${counter},--------------------------\n\n\n\n")
      }
    }
  }
}

class fir_test_pref extends AnyFlatSpec with ChiselScalatestTester {

  "fir_test with prefetcher" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      var pc = 0
      var counter = 0
      var test = 0
      dut.io.cacheOnly.poke(false.B)

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
            println(f"fir_test mit prefetcher BREAK at i: ${i}, counter: ${counter},--------------------------\n\n\n\n")
            break()  // Exit the loop early
          }
          dut.clock.step(1)
        }
        println(s"fir_test mit prefetcher ENDE at i: 1000, counter: ${counter},-------------------------\n\n\n\n")
      }
    }
  }
}



class linear_test_no extends AnyFlatSpec with ChiselScalatestTester {

  "linear_test no prefetcher" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      var pc = 0
      var counter = 0
      var test = 0
      dut.io.cacheOnly.poke(true.B)

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

          if (pc > 180*4) {
            println(s"linear_test_no BREAK at i: $i, counter: ${counter},--------------------------------\n\n\n\n")
            break()  // Exit the loop early
          }
          dut.clock.step(1)
        }
        println(s"linear_test_no ENDE at i: 1000, counter: ${counter},-----------------------------\n\n\n\n")
      }
    }
  }
}

class linear_test_pref extends AnyFlatSpec with ChiselScalatestTester {

  "linear_test with prefetcher" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      var pc = 0
      var counter = 0
      var test = 0
      dut.io.cacheOnly.poke(false.B)

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

          if (pc > 180*4) {
            println(s"linear_test_pref BREAK at i: $i, counter: ${counter},--------------------------------\n\n\n\n")
            break()  // Exit the loop early
          }
          dut.clock.step(1)
        }
        println(s"linear_test_pref ENDE at i: 1000, counter: ${counter},-----------------------------\n\n\n\n")
      }
    }
  }
}

class worst_case_no extends AnyFlatSpec with ChiselScalatestTester {
  "worst_case_no" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
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
        for (i <- 0 until 1000) {
          if (dut.io.IMEMCacheBusy.peek().litToBoolean) {
            if (dut.io.IMEMCacheValid.peek().litToBoolean) {
              counter += 1
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128) / 4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
              pc = pc + 8 //next in prefetcher would be +4
            }
          }
          if (pc > 720) {
            println(s"worst_case_no BREAK i=${i}, counter: ${counter}-----------------------------------------\n")
            break() // Exit the loop early
          }
          dut.io.IMEMAddr.poke((pc).U)
          dut.clock.step(1)
        }
        println(s"worst_case_no ENDE i=1000, counter: ${counter}-----------------------------------------\n")
      }
    }
  }
}

class worst_case_pref extends AnyFlatSpec with ChiselScalatestTester {
  "worst_case_pref" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
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
      for (i <- 0 until 1000) {
        if (dut.io.IMEMCacheBusy.peek().litToBoolean) {
          if (dut.io.IMEMCacheValid.peek().litToBoolean) {
            counter += 1
            imemOut = dut.io.IMEMOut.peek().litValue
            test = (pc % 128) / 4
            assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
            pc = pc + 8 //next in prefetcher would be +4
          }
        }
        if(pc > 720){
          println(s"worst_case_pref BREAK i=${i}, counter: ${counter}-----------------------------------------\n")
          break()  // Exit the loop early
        }
        dut.io.IMEMAddr.poke((pc).U)
        dut.clock.step(1)
      }
      println(s"worst_case_pref ENDE i=1000, counter: ${counter}-----------------------------------------\n")
      }
    }
  }
}