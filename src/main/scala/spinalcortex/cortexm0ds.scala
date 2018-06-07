package spinalcortex

import spinal.core._
import spinal.lib.bus.amba3.ahblite._

class CortexM0DS(
  fclkDomain: ClockDomain = ClockDomain.current,
  sclkDomain: ClockDomain = ClockDomain.current,
  hclkDomain: ClockDomain = ClockDomain.current,
  dclkDomain: ClockDomain = ClockDomain.current)
  extends BlackBox  
{
  val BusConfig = AhbLite3Config(32, 32)

  val io = new Bundle {
    val FCLK = in Bool
    val SCLK = in Bool
    val HCLK = in Bool
    val DCLK = in Bool
    val PORESETn = in Bool
    val DBGRESETn = in Bool
    val HRESETn = in Bool
    val SWCLKTCK = in Bool
    val nTRST = in Bool

    // AHB-LITE MASTER PORT
    val AHBM = AhbLite3Master(BusConfig)
    val HMASTER = out Bool

    // CODE SEQUENTIALITY AND SPECULATION
    val CODENSEQ = out Bool
    val CODEHINTDE = out Bits(3 bits)
    val SPECHTRANS = out Bool

    // DEBUG
    val SWDITMS = in Bool
    val TDI = in Bool
    val SWDO = out Bool
    val SWDOEN = out Bool
    val TDO = out Bool
    val nTDOEN = out Bool
    val DBGRESTART = in Bool
    val DBGRESTARTED = out Bool
    val EDBGRQ = in Bool
    val HALTED = out Bool

    // MISC
    val NMI = in Bool
    val IRQ = in Bits(32 bits)
    val TXEV = out Bool
    val RXEV = in Bool
    val LOCKUP = out Bool
    val SYSRESETREQ = out Bool
    val STCALIB = in Bits(26 bits)
    val STCLKEN = in Bool
    val IRQLATENCY = in Bits(8 bits)
    val ECOREVNUM = in Bits(28 bits) // [27:20] to DAP [19:0] to core

    // POWER MANAGEMENT
    val GATEHCLK = out Bool
    val SLEEPING = out Bool
    val SLEEPDEEP = out Bool
    val WAKEUP = out Bool
    val WICSENSE = out Bits(34 bits)
    val SLEEPHOLDREQn = in Bool
    val SLEEPHOLDACKn = out Bool
    val WICENREQ = in Bool
    val WICENACK = out Bool
    val CDBGPWRUPREQ = out Bool
    val CDBGPWRUPACK = in Bool

    // SCAN IO
    val SE = in Bool
    val RSTBYPASS = in Bool
  }

  io.AHBM.asMaster()
  io.AHBM.HADDR.setName("HADDR")
  io.AHBM.HWRITE.setName("HWRITE")
  io.AHBM.HSIZE.setName("HSIZE")
  io.AHBM.HBURST.setName("HBURST")
  io.AHBM.HPROT.setName("HPROT")
  io.AHBM.HTRANS.setName("HTRANS")
  io.AHBM.HMASTLOCK.setName("HMASTLOCK")
  io.AHBM.HWDATA.setName("HWDATA")
  io.AHBM.HRDATA.setName("HRDATA")
  io.AHBM.HREADY.setName("HREADY")
  io.AHBM.HRESP.setName("HRESP")

  mapClockDomain(fclkDomain, io.FCLK)
  mapClockDomain(sclkDomain, io.SCLK)
  mapClockDomain(hclkDomain, io.HCLK)
  mapClockDomain(dclkDomain, io.DCLK)

  noIoPrefix()

  addRTLPath("./cortexm0ds_logic.v")
  addRTLPath("./CORTEXM0INTEGRATION.v")

  setBlackBoxName("CORTEXM0INTEGRATION")
}
