package spinalcortex

import collection.JavaConverters._
import java.nio.file.{Files, Paths}
import spinal.core._
import spinal.lib._
import spinal.lib.bus.misc._
import spinal.lib.bus.amba3.ahblite._

class SpinalCortex(romPath: String, romSize: BigInt) extends Component {
  val io = new Bundle {
    val gpioOut = out Bits(8 bits)
  }

  val mainClock = ClockDomain.external("clk", ClockDomainConfig(resetActiveLevel = LOW))

  val mainClockArea = new ClockingArea(mainClock) {
    val divider = new ClockDivider(
      ClockDomain.current, div = 4,
      config = ClockDomainConfig(resetKind = BOOT)
    )


    val dividedClockArea = new ClockingArea(divider.outDomain) {
      val core = new CortexM0DS
      val romContents = java.nio.file.Files.readAllBytes(Paths.get(romPath))
        .grouped(4)
        .map(w => new BigInt(new java.math.BigInteger(
          Array(0.toByte, 0.toByte, 0.toByte, 0.toByte, w(3), w(2), w(1), w(0)))))
      val rom = new AhbLite3OnChipRom(
        core.BusConfig,
        romContents.map(w => B(w, 32 bits)).toSeq
      )
      val gpio = new Gpio(core.BusConfig)
      io.gpioOut := gpio.io.output(7 downto 0)

      val interconnect = AhbLite3CrossbarFactory(core.BusConfig)
      interconnect.addSlave(rom.io.ahb, SizeMapping(0x0, romSize))
      interconnect.addSlave(gpio.io.ahb, SizeMapping(0x4000, 0x1000))
      interconnect.addConnection(core.io.AHBM.toAhbLite3, Seq(rom.io.ahb, gpio.io.ahb))
      interconnect.build()

      val ResetCycles = 10
      val resetCounter = Reg(UInt(log2Up(ResetCycles) bits)) init(0)
      when (resetCounter < ResetCycles) {
        resetCounter := resetCounter + 1
      }
      val reset = (resetCounter === ResetCycles)

      // resets
      core.io.PORESETn := reset
      core.io.DBGRESETn := True
      core.io.HRESETn := reset
      core.io.nTRST := True

      // interrupts
      core.io.NMI := False
      core.io.IRQ := B(0, 32 bits)

      // misc signals
      core.io.RXEV := False
      core.io.STCALIB := B(26 bits, 25 -> true, default -> false)
      core.io.STCLKEN := False
      core.io.IRQLATENCY := B(0, 8 bits)
      core.io.ECOREVNUM := B(0, 28 bits)

      // unused debug port
      core.io.SWCLKTCK := False
      core.io.SWDITMS := False
      core.io.TDI := False
      core.io.DBGRESTART := False
      core.io.EDBGRQ := False

      // See ARM reference for these unused signals
      core.io.SLEEPHOLDREQn := True
      core.io.WICENREQ := False
      core.io.CDBGPWRUPACK := core.io.CDBGPWRUPREQ
      core.io.SE := False
      core.io.RSTBYPASS := False
    }
  }
}

class Gpio(busConfig: AhbLite3Config) extends Component {
  val io = new Bundle {
    val ahb = slave(AhbLite3(busConfig))
    val output = out Bits(32 bits)
  }

  val value = Reg(Bits(32 bits)) init(B(0x0, 32 bits))
  io.output := value

  val addrLoaded = Reg(Bool) init(False)

  io.ahb.HRDATA := value
  io.ahb.HRESP := False
  io.ahb.HREADYOUT := True

  when (io.ahb.HSEL && io.ahb.HREADY) {
    addrLoaded := True
  } otherwise {
    addrLoaded := False
  }

  when (addrLoaded) {
    value := io.ahb.HWDATA
  }
}

class ClockDivider(
  inDomain: ClockDomain, div: Int, config: ClockDomainConfig
  ) extends Component
{
  val inDomainArea = new ClockingArea(inDomain) {
    val count = Reg(UInt(log2Up(div) bits)) init(0)
    val out = Reg(Bool) init(False)

    when (count === div - 1) {
      count := 0
      out := ~out
    } otherwise {
      count := count + 1
    }
  }

  val outDomain = ClockDomain(
    clock = inDomainArea.out,
    reset = if (config.resetKind == BOOT) { null} else { inDomain.reset },
    softReset = if (config.resetKind == BOOT) { null} else { inDomain.reset },
    clockEnable = inDomain.clockEnable,
    config = config
  )
}

object TopLevelVerilog {
  def main(args: Array[String]) {
    SpinalConfig(
      inlineRom = true
    ).generateVerilog(new SpinalCortex(
      romPath = "sw/blink.bin",
      romSize = 0x1000))
  }
}
