package DCache_test

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import DCache.{Cache, DCache}

class DCache_test extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "DCache"
/*  "read hit" should "pass" in {
    test(new Cache("src/main/scala/DCache/CacheContent.bin", read_only = false)){ c =>
      println("\n ---------------- test 1: read hit ------------------\n")
      c.clock.step()
      c.clock.step()
      c.clock.step()
      c.clock.step()
      c.io.write_en match {
        case Some(write_en) => write_en.poke(0.B)
        case None => // nothing
      }
      c.io.read_en.poke(1.B)
      c.io.data_addr.poke(2273941156L.U)
      //c.clock.step()

      //c.io.read_en.poke(0.B)
      c.io.data_out.expect(2284362812L.U)
      c.io.valid.expect(1.B)
      c.clock.step()
      c.io.read_en.poke(0.B)

      c.clock.step()
      c.clock.step()
    }
  }

  "read miss clean" should "pass" in {
    test(new Cache("src/main/scala/DCache/CacheContent.bin", read_only = false)) { c =>
      println("\n ---------------- test 2: read miss clean ------------------\n")
      c.io.write_en match {
        case Some(write_en) => write_en.poke(0.B)
        case None => // nothing
      }
      c.io.read_en.poke(1.B)
      c.io.data_addr.poke(31519552L.U)
      c.clock.step()
      // compare
      //c.io.read_en.poke(0.B)
      //c.clock.step()
      // allocate: read from mem
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(1.B)
      c.io.mem_data_addr.expect(31519552L.U)
      c.clock.step()
      // allocate: write it to cache
      c.io.mem_data_out.poke(13.U)
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(0.B)
      c.clock.step()
      // compare
      c.io.data_out.expect(13.U)
      c.io.valid.expect(1.B)
      c.clock.step()
      // idle
      c.clock.step()
      c.clock.step()
    }
  }

  "read miss dirty" should "pass" in {
    test(new Cache("src/main/scala/DCache/CacheContent.bin", read_only = false)) { c =>
      println("\n ---------------- test 3: read miss dirty ------------------\n")
      c.io.write_en match {
        case Some(write_en) => write_en.poke(0.B)
        case None => // nothing
      }
      c.io.read_en.poke(1.B)
      c.io.data_addr.poke(3359519652L.U)
      c.clock.step()
      // compare
      //c.io.read_en.poke(0.B)
      //c.clock.step()
      // write-back
      c.io.mem_data_in.expect(2284362812L.U)
      c.io.mem_write_en.expect(1.B)
      c.io.mem_read_en.expect(0.B)
      c.io.mem_data_addr.expect(2273941156L.U)
      c.clock.step()
      // allocate: read from mem
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(1.B)
      c.io.mem_data_addr.expect(3359519652L.U)
      c.clock.step()
      // allocate: write it to cache
      c.io.mem_data_out.poke(987654321.U)
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(0.B)
      c.clock.step()
      // compare
      c.io.data_out.expect(987654321.U)
      c.io.valid.expect(1.B)
      c.clock.step()
      // idle
      c.clock.step()
      c.clock.step()
    }
  }

  "write hit" should "pass" in {
    test(new Cache("src/main/scala/DCache/CacheContent.bin", read_only = false)) { c =>
      println("\n ---------------- test 4: write hit ------------------\n")
      c.io.write_en match {
        case Some(write_en) => write_en.poke(1.B)
        case None => // nothing
      }
      c.io.read_en.poke(0.B)
      c.io.data_addr.poke(1318216816.U)
      c.io.data_in match {
        case Some(data_in) => data_in.poke(48.U)
        case None => // nothing
      }
      c.clock.step()
      // compare
      c.io.valid.expect(1.B)
      c.io.write_en match {
        case Some(write_en) => write_en.poke(0.B)
        case None => // nothing
      }
      c.clock.step()
      // idle
      c.clock.step()
      c.clock.step()
    }
  }

  "write miss clean" should "pass" in {
    test(new Cache("src/main/scala/DCache/CacheContent.bin", read_only = false)) { c =>
      println("\n ---------------- test 5: write miss clean ------------------\n")
      c.io.write_en match {
        case Some(write_en) => write_en.poke(1.B)
        case None => // nothing
      }
      c.io.read_en.poke(0.B)
      c.io.data_addr.poke(4.U)
      c.io.data_in match {
        case Some(data_in) => data_in.poke(48.U)
        case None => // nothing
      }
      c.clock.step()
      // compare
      c.io.write_en match {
        case Some(write_en) => write_en.poke(0.B)
        case None => // nothing
      }
      //c.clock.step()
      // allocate: read from mem
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(1.B)
      c.io.mem_data_addr.expect(4.U)
      c.clock.step()
      // allocate: write it to cache
      c.io.mem_data_out.poke(46.U)
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(0.B)
      c.clock.step()
      // compare
      c.io.read_en.poke(1.B)
      c.io.data_addr.poke(4.U)
      c.io.valid.expect(1.B)
      c.clock.step()
      // idle
      c.clock.step()
      c.clock.step()
    }
  }

  "write miss dirty" should "pass" in {
    test(new Cache("src/main/scala/DCache/CacheContent.bin", read_only = false)) { c =>
      println("\n ---------------- test 6: write miss dirty ------------------\n")
      // c.io.write_en.poke(1.B)
      c.io.data_in match {
        case Some(data_in) => data_in.poke(48.U)
        case None => // nothing
      }
      c.io.write_en match {
        case Some(write_en) => write_en.poke(1.B)
        case None => // nothing
      }
      c.io.read_en.poke(0.B)
      c.io.data_addr.poke(0.U)
      //println(s"\n address poke\n")
     // c.io.data_in.poke(48.U)

      c.clock.step()
//      println(s"\n compare11111111111111111111 mem_data_in ${c.io.mem_data_in.peek.litValue}\n")
//      println(s" mem_write_en ${c.io.mem_write_en.peek.litValue}\n")
//      println(s" mem_read_en ${c.io.mem_read_en.peek.litValue}\n")
//      println(s" mem_data_addr ${c.io.mem_data_addr.peek.litValue}\n")
      // compare
      c.io.read_en.poke(0.B)
      c.io.data_addr.poke(0.U)
      c.io.write_en match {
        case Some(write_en) => write_en.poke(0.B)
        case None => // nothing
      }
      //c.clock.step()
      // write-back
//      println(s"\nwrite-back 2222222222222222222222 mem_data_in ${c.io.mem_data_in.peek.litValue}, expect 1153902698\n")
//      println(s" mem_write_en ${c.io.mem_write_en.peek.litValue}, expect 1\n")
//      println(s" mem_read_en ${c.io.mem_read_en.peek.litValue}, expect 0\n")
//      println(s" mem_data_addr ${c.io.mem_data_addr.peek.litValue}, expect 384929280\n")
      c.io.mem_data_in.expect(1153902698.U)
      c.io.mem_write_en.expect(1.B)
      c.io.mem_read_en.expect(0.B)
      c.io.mem_data_addr.expect(384929280.U)
      c.clock.step()
//      println(s"\nallocate 3333333333333333333333 mem_data_in ${c.io.mem_data_in.peek.litValue}\n")
//      println(s" mem_write_en ${c.io.mem_write_en.peek.litValue}, expect 0\n")
//      println(s" mem_read_en ${c.io.mem_read_en.peek.litValue}, expect 1\n")
//      println(s" mem_data_addr ${c.io.mem_data_addr.peek.litValue}expect 0\n")
      // allocate: read from mem
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(1.B)
      c.io.mem_data_addr.expect(0.U)
      c.clock.step()
      // allocate: write it to cache
//      println(s"\n4444444444444444 allocate mem_data_in ${c.io.mem_data_in.peek.litValue}\n")
//      println(s" mem_write_en ${c.io.mem_write_en.peek.litValue}, expect 0\n")
//      println(s" mem_read_en ${c.io.mem_read_en.peek.litValue}, expect 0\n")
//      println(s" mem_data_addr ${c.io.mem_data_addr.peek.litValue}\n")
      c.io.mem_data_out.poke(46.U)
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(0.B)
      c.clock.step()
      //println(s"\n dazwischen\n")
      c.clock.step()
      // compare

      println(s"\n valid ${c.io.valid.peek.litValue}, expect 1, HIER AUSKOMMENTIERT\n")
      //c.io.valid.expect(1.B)
      c.clock.step()
      // idle
      c.clock.step()
      c.clock.step()
    }
  }

  "write then read" should "pass" in {
    test(new Cache("src/main/scala/DCache/CacheContent.bin", read_only = false)){ c =>
      println("\n ---------------- test 7: write then read ------------------\n")
      c.clock.step()
      c.clock.step()
      c.clock.step()
      c.clock.step()
      c.io.write_en match {
        case Some(write_en) => write_en.poke(1.B)
        case None => // nothing
      }
      c.io.read_en.poke(0.B)
      c.io.data_addr.poke(40.U)
      c.io.data_in match {
        case Some(data_in) => data_in.poke(123.U)
        case None => // nothing
      }
      c.clock.step()
      c.io.write_en match {
        case Some(write_en) => write_en.poke(0.B)
        case None => // nothing
      }
      c.clock.step()
      c.clock.step()

      c.io.mem_data_out.poke(2564.U)
      c.clock.step()
      c.clock.step()
      c.clock.step()

      // read hit
      c.io.write_en match {
        case Some(write_en) => write_en.poke(0.B)
        case None => // nothing
      }
      c.io.read_en.poke(1.B)
      c.io.data_addr.poke(40.U)
      println(s" data_out ${c.io.data_out.peek.litValue}\n")
      c.clock.step()
      c.io.read_en.poke(0.B)
      c.io.valid.expect(1.B)
      println(s" data_out ${c.io.data_out.peek.litValue}\n")
      c.io.data_out.expect(123.U)
      c.clock.step()
      println(s" data_out ${c.io.data_out.peek.litValue}\n")
      c.clock.step()
      println(s" data_out ${c.io.data_out.peek.litValue}\n")

      // read miss dirty
      c.io.write_en match {
        case Some(write_en) => write_en.poke(0.B)
        case None => // nothing
      }
      c.io.read_en.poke(1.B)
      c.io.data_addr.poke(296.U)
      c.clock.step()
      // compare
      c.io.read_en.poke(0.B)
      c.clock.step()
      // write-back
      c.io.mem_data_in.expect(123.U)
      c.io.mem_write_en.expect(1.B)
      c.io.mem_read_en.expect(0.B)
      c.io.mem_data_addr.expect(40.U)
      c.clock.step()
      // allocate: read from mem
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(1.B)
      c.io.mem_data_addr.expect(296.U)
      c.clock.step()
      // allocate: write it to cache
      c.io.mem_data_out.poke(999.U)
      c.io.mem_write_en.expect(0.B)
      c.io.mem_read_en.expect(0.B)
      c.clock.step()
      // compare
      c.io.valid.expect(1.B)
      c.io.data_out.expect(999.U)
      c.clock.step()
      // idle
      c.clock.step()
      c.clock.step()
    }
  }*/
}
