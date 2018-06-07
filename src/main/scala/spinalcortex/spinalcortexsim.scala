package spinalcortexsim

import spinal.core._
import spinal.sim._
import spinal.core.sim._
import spinalcortex._

object SpinalCortexSim {
  def main(args: Array[String]) {
    SimConfig.withWave.compile(new SpinalCortex("./sw/blink.bin", 0x100)).doSim { dut =>
      // clock signal
      fork {
        dut.clockDomain.fallingEdge()
        while (true) {
          sleep(1)
          dut.clockDomain.risingEdge()
          sleep(1)
          dut.clockDomain.fallingEdge()
        }
      }
      // perform initial reset
      dut.clockDomain.assertReset()
      dut.clockDomain.waitRisingEdge(5)
      dut.clockDomain.disassertReset()
      
      dut.clockDomain.waitRisingEdge(10000)
      simSuccess()
    }
  }
}
