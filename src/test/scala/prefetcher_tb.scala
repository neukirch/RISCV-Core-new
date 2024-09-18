
package prefetchTB

import chisel3._
import chisel3.util._
import chiseltest._
//import Prefetcher._
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.control.Breaks._
import RISCV_TOP._


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
          println(s"i: ${i}\n")
          //println(f"i: ${i}, pc: ${pc/4}\n")
          //println(s"i: ${i}, pc: ${pc}\n")
          if(dut.io.IMEMCacheBusy.peek().litToBoolean){
            //println(f"busy peek cache: 0x${dut.io.resultCache.peek().litValue}%x, busy peek cache: 0x${dut.io.resultCache.peek().litValue}%x\n")
            if(dut.io.IMEMCacheValid.peek().litToBoolean) {


              //dut.io.pc.poke((pc + 1).U)

              //println(s"--------------------------------------------------------\n")
//              println(s"i: ${i}, pc: ${pc/4}\n")
//              println(f"\n")
//              println(f"\n")
//              println(f"\n")
//              println(f"cacheValid: ${dut.io.IMEMCacheValid.peek().litValue}\n")
//              println(f"valid peek cache result: 0x${dut.io.IMEMOut.peek().litValue}%x, pc: ${pc/4}\n")
//              println(f"\n")
//              println(f"\n")
//              println(f"\n")
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128)/4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
              pc += 4
              counter += 1
            }
          }
            //println(f"nothing peek cache: 0x${dut.io.resultCache.peek().litValue}%x, nothing peek cache: 0x${dut.io.resultCache.peek().litValue}%x\n")
            //dut.io.pc.poke(0.U)
          dut.io.IMEMAddr.poke((pc).U)

          if (dut.io.IMEMOut.peek().litValue == 51) {
            println(s"BREAK at i: $i, counter: ${counter},-------------------------------------------------------------\n\n\n\n")
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
      println(f"---------------------------------------------loop_test start---------------------------------------------\n")
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
          println(s"i: ${i}, pc: ${pc}\n")
          //println(s"i: ${i}, pc: ${pc}\n")
          //println(s"i: ${i}, pc: ${pc}\n")
          if(dut.io.IMEMCacheBusy.peek().litToBoolean){
            if(dut.io.IMEMCacheValid.peek().litToBoolean) {
              //dut.io.pc.poke((pc + 1).U)
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128)/4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
              counter += 1
              //              println(s"--------------------------------------------------------\n")
              //
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
                println(s"BREAK at i: ${i},counter: ${counter}\n")
                break()  // Exit the loop early
              }
              else{
                pc += 4
              }
              //              println(f"cacheValid: ${dut.io.IMEMCacheValid.peek().litValue}, poke: ${pc}\n")
            }
          }




          dut.io.IMEMAddr.poke((pc).U)
          //            println(f"poke: ${pc}\n")

          dut.clock.step(1)
          println(s"\n\n")
        }
      }


    }
  }
}


class cache_test extends AnyFlatSpec with ChiselScalatestTester {

  "cache_test" should "work" in {
    test(new RISCV_TOP("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      println(f"---------------------------------------------cache_test start---------------------------------------------\n")
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


      //println(f"cache_test start---------------xxxxxxxxxxx-----------------\n\n\n\n\n\n")
      breakable {
        for(i <- 0 until 1000) {
          println(s"i: ${i}\n")
          //println(s"i: ${i}, pc: ${pc}\n")
          //println(s"i: ${i}, pc: ${pc}\n")
          if(dut.io.IMEMCacheBusy.peek().litToBoolean){
            //println(f"busy peek cache: 0x${dut.io.resultCache.peek().litValue}%x, busy peek cache: 0x${dut.io.resultCache.peek().litValue}%x\n")
            if(dut.io.IMEMCacheValid.peek().litToBoolean) {
              //dut.io.pc.poke((pc + 1).U)
              counter += 1
              //println(s"--------------------------------------------------------\n")

              //println(f"valid peek cache result: 0x${dut.io.IMEMOut.peek().litValue}%x\n\n\n")
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128)/4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")
              if(pc == 12 && counter < 15){
                pc = 0
              }
              else if (pc == 36){
                println(s"BREAK at i: ${i}, counter: ${counter}\n")
                break()  // Exit the loop early
              }
              else{
                pc += 4
              }
              //println(f"cacheValid: ${dut.io.IMEMCacheValid.peek().litValue}, poke: ${pc}\n")
            }
          }
          dut.io.IMEMAddr.poke((pc).U)
          //println(f"poke: ${pc}\n")

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
      println(f"---------------------------------------------random_test start---------------------------------------------\n")
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
          println(s"i: ${i}\n")
          if(dut.io.IMEMCacheBusy.peek().litToBoolean){
            if(dut.io.IMEMCacheValid.peek().litToBoolean) {
              counter += 1
              //println(s"--------------------------------------------------------\n")

              //println(f"valid peek cache result: 0x${dut.io.IMEMOut.peek().litValue}%x\n\n\n")
              imemOut = dut.io.IMEMOut.peek().litValue
              test = (pc % 128)/4
              assert(imemOut == expectedValues(test).litValue, f"ADR ${test} PC ${pc}  failed: Expected 0x${expectedValues(test).litValue}%08x but got 0x${imemOut}%08x")

              //println(f"cacheValid: ${dut.io.IMEMCacheValid.peek().litValue}, poke: ${pc}\n")
              pc = random.nextInt(192) * 4 //only 192 because 00000033 not in seq
            }
          }
          dut.io.IMEMAddr.poke((pc).U)
          //println(f"poke: ${pc}\n")

          dut.clock.step(1)
        }
      println(s"counter: ${counter}, i=200\n")
    }
  }
}



//class test_performance extends AnyFlatSpec with ChiselScalatestTester {
//
//  "linear_test" should "work" in {
//    test(new PreMemCache("src/test/programs/aaPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//            dut.clock.setTimeout(0)
//            var pc = 0
//
//            dut.io.pc.poke(0.U)
//            dut.clock.step(5)
//            breakable {
//                for(i <- 0 until 1000) {
//                    //println(s"i: ${i}, pc: ${pc}\n")
//                    if(dut.io.busy.peek().litToBoolean){
//                        //println(f"busy peek cache: 0x${dut.io.resultCache.peek().litValue}%x, busy peek cache: 0x${dut.io.resultCache.peek().litValue}%x\n")
//                        if(dut.io.valid.peek().litToBoolean) {
//                            //dut.io.pc.poke((pc + 1).U)
//
//                            //println(s"--------------------------------------------------------\n")
//                            println(s"i: ${i}, pc: ${pc}\n")
//                            println(f"cacheValid: ${dut.io.valid.peek().litValue}\n")
//                            println(f"valid peek cache result: 0x${dut.io.instr.peek().litValue}%x\n")
//                            pc += 1
//                        }
//                    }else{
//                        //println(f"nothing peek cache: 0x${dut.io.resultCache.peek().litValue}%x, nothing peek cache: 0x${dut.io.resultCache.peek().litValue}%x\n")
//                        //dut.io.pc.poke(0.U)
//                      dut.io.pc.poke((pc).U)
//                    }
//
//                    if (dut.io.instr.peek().litValue == 22020243) {
//                        println(s"BREAK at i: $i")
//                        break()  // Exit the loop early
//                    }
//                    dut.clock.step(1)
//                }
//            }
//
//
//        }
//    }
//
//  "loop_test" should "work" in {
//    test(new PreMemCache("src/test/programs/bbPerformance")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
//      dut.clock.setTimeout(0)
//      var pc = 0
//
//      dut.io.pc.poke(0.U)
//      dut.clock.step(5)
//      breakable {
//        for(i <- 0 until 1000) {
//          //println(s"i: ${i}, pc: ${pc}\n")
//          if(dut.io.busy.peek().litToBoolean){
//            //println(f"busy peek cache: 0x${dut.io.resultCache.peek().litValue}%x, busy peek cache: 0x${dut.io.resultCache.peek().litValue}%x\n")
//            if(dut.io.valid.peek().litToBoolean) {
//              //dut.io.pc.poke((pc + 1).U)
//
//              //println(s"--------------------------------------------------------\n")
//              println(s"i: ${i}, pc: ${pc}\n")
//              println(f"cacheValid: ${dut.io.valid.peek().litValue}\n")
//              println(f"valid peek cache result: 0x${dut.io.instr.peek().litValue}%x\n")
//              pc += 1
//            }
//          }else{
//            //println(f"nothing peek cache: 0x${dut.io.resultCache.peek().litValue}%x, nothing peek cache: 0x${dut.io.resultCache.peek().litValue}%x\n")
//            //dut.io.pc.poke(0.U)
//            dut.io.pc.poke((pc).U)
//          }
//
//          if (dut.io.instr.peek().litValue == 22020243) {
//            println(s"BREAK at i: $i")
//            break()  // Exit the loop early
//          }
//          dut.clock.step(1)
//        }
//      }
//
//
//    }
//  }
//}
/*
class general extends AnyFlatSpec with ChiselScalatestTester {

"general_test" should "work" in {
  test(new prefetcher("src/test/programs/prefetchMem_test")).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      //dut.clock.setTimeout(0)


      //
      //MULTI-WAY-PREFETCHER
      //
      //first fetch on all empty buffers, load buffer 3
      dut.clock.step(1)
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(true.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)

      dut.clock.step(1)
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      dut.clock.step(1)
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.result.expect(19.U)//fetch in 1 cycle
      dut.io.valid.expect(true.B)

      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      //then wait until buffer half full and sequential fetch
      dut.clock.step(1)
      for(i<- 0 until 2){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      dut.io.missAddress.poke(1.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(1.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(1.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(4194451.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)

      //wait until buffer full and sequential fetch
      for(i<- 0 until 10){
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.io.valid.expect(false.B)
          dut.clock.step(1)
      }

      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(true.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)

      dut.io.missAddress.poke(2.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(2.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(5243155.U)//is in buffer
      dut.clock.step(1)
      dut.io.valid.expect(false.B)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.clock.step(1)

      for(i<- 0 until 3){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      //non sequential  fetch to load buffer 2
      dut.io.missAddress.poke(5.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(5.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(5.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1118899.U)//fetch in 1 cycle

      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      //then wait until buffer half full and sequential fetch
      dut.clock.step(1)
      for(i<- 0 until 2){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      dut.io.missAddress.poke(6.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(6.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1123123.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)


      //wait until buffer full and sequential fetch
      dut.clock.step(1)
      for(i<- 0 until 10){
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.io.valid.expect(false.B)
          dut.clock.step(1)
      }

      dut.io.missAddress.poke(7.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(7.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(true.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(7.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)

      dut.io.missAddress.poke(7.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2139059.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      dut.clock.step(1)
      for(i<- 0 until 3){
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.io.valid.expect(false.B)
          dut.clock.step(1)
      }

      //hit on buffer 3
      dut.io.missAddress.poke(3.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(3.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2130355.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      dut.clock.step(1)
      for(i<- 0 until 3){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }




      //miss to load buffer 1
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)

      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(19.U)//fetch in 1 cycle

      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      //then wait until buffer half full and sequential fetch
      dut.clock.step(1)
      for(i<- 0 until 2){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      dut.io.missAddress.poke(1.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(1.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(4194451.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)


      //wait until buffer full and sequential fetch
      dut.clock.step(1)
      for(i<- 0 until 10){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      dut.io.missAddress.poke(2.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(2.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(5243155.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      dut.clock.step(1)
      for(i<- 0 until 4){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      //hit on buffer 3
      dut.io.missAddress.poke(4.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(4.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1074889267.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      dut.clock.step(1)
      for(i<- 0 until 3){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }



      //miss to load buffer 0
      dut.io.missAddress.poke(11.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(11.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)

      dut.io.missAddress.poke(11.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2147763.U)//fetch in 1 cycle

      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      //then wait until buffer half full and sequential fetch
      dut.clock.step(1)
      for(i<- 0 until 2){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      dut.io.missAddress.poke(12.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(12.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1234483.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      //wait until buffer full and sequential fetch
      dut.clock.step(1)
      for(i<- 0 until 10){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      dut.io.missAddress.poke(13.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)

      dut.io.missAddress.poke(13.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1081136819.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      dut.clock.step(1)
      for(i<- 0 until 2){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      //hit on buffer 3
      dut.io.missAddress.poke(5.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(5.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1118899.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      dut.clock.step(1)
      for(i<- 0 until 3){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }



      //lru  should be buffer 2, miss on all to flush buffer 2 and load new instructions
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)

      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)//flush
      dut.clock.step(1)

      dut.io.missAddress.poke(0.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(19.U)//fetch after flush

      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      for(i<- 0 until 10){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      //test if buffer 2 is loaded correctly
      dut.io.missAddress.poke(1.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(1.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(4194451.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      dut.clock.step(1)
      for(i<- 0 until 3){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }



      //new lru should be buffer 1, miss on all to flush buffer 1 and load new instructions
      dut.io.missAddress.poke(9.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(9.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)

      dut.io.missAddress.poke(9.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)//flush

      dut.clock.step(1)
      dut.io.missAddress.poke(9.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2143411.U)//fetch after flush

      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      for(i<- 0 until 10){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      //test if buffer 1 is loaded correctly
      dut.io.missAddress.poke(10.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(10.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1127731.U)//is in buffer
      dut.clock.step(1)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)

      dut.clock.step(1)
      for(i<- 0 until 10){
          dut.io.valid.expect(false.B)
          dut.io.cacheBusy.poke(false.B)
          dut.io.cacheValid.poke(false.B)
          dut.clock.step(1)
      }

      dut.io.valid.expect(false.B)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.clock.step(1)
      dut.io.valid.expect(false.B)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.clock.step(1)
      dut.io.valid.expect(false.B)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(true.B)
      dut.clock.step(1)
      dut.io.valid.expect(false.B)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.clock.step(1)
      dut.io.valid.expect(false.B)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.clock.step(1)

      //check every element in the buffers, no fetch inbetween
      //buffer 0
      dut.io.missAddress.poke(14.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(14.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1173299.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(15.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(15.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1144755.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(16.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(16.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(19.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(17.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(17.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(4194451.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(18.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(18.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(5243155.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(19.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(19.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2130355.U)//is in buffer
      dut.clock.step(1)

      //buffer 1
      dut.io.missAddress.poke(11.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(11.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2147763.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(12.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(12.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1234483.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(13.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(13.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1081136819.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(14.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(14.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1173299.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(15.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(15.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1144755.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(16.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(16.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(19.U)//is in buffer
      dut.clock.step(1)

      //buffer 2
      dut.io.missAddress.poke(2.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(2.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(5243155.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(3.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(3.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2130355.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(4.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(4.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1074889267.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(5.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(5.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1118899.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(6.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(6.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1123123.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(7.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(7.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2139059.U)//is in buffer
      dut.clock.step(1)

      //buffer 3
      dut.io.missAddress.poke(6.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(6.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1123123.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(7.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(7.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2139059.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(8.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(8.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2171955.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(9.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(9.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2143411.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(10.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(10.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(1127731.U)//is in buffer
      dut.clock.step(1)

      dut.io.missAddress.poke(11.U)
      dut.io.cacheBusy.poke(true.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(false.B)
      dut.clock.step(1)
      dut.io.missAddress.poke(11.U)
      dut.io.cacheBusy.poke(false.B)
      dut.io.cacheValid.poke(false.B)
      dut.io.valid.expect(true.B)
      dut.io.result.expect(2147763.U)//is in buffer
      dut.clock.step(1)

      /*
0: 00000013     19
1: 00400093     4194451
2: 00500113     5243155
3: 002081b3     2130355
4: 40118233     1074889267
5: 001112b3     1118899
6: 00112333     1123123
7: 0020a3b3     2139059
8: 00212433     2171955
9: 0020b4b3     2143411
10: 00113533    1127731
11: 0020c5b3    2147763
12: 0012d633    1234483
13: 4070d6b3    1081136819
14: 0011e733    1173299
15: 001177b3    1144755
16: 00000013     19
17: 00400093     4194451
18: 00500113     5243155
19: 002081b3     2130355
20: 40118233     1074889267
21: 001112b3     1118899
22: 00112333     1123123
23: 0020a3b3     2139059
24: 00212433     2171955
25: 0020b4b3     2143411
26: 00113533    1127731
27: 0020c5b3    2147763
28: 0012d633    1234483
29: 4070d6b3    1081136819
30: 0011e733    1173299
31: 001177b3    1144755
 */

    }
  }

}

        }
    }
}
*/

