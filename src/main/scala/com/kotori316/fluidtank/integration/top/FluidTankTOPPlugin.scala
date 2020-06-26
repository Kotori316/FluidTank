package com.kotori316.fluidtank.integration.top
/*
import cats.data.Reader
import mcjty.theoneprobe.api.ITheOneProbe
import net.minecraftforge.fml.{InterModComms, ModList}

object FluidTankTOPPlugin {
  private[this] final val TOP_ID = "theoneprobe"

  private class Function extends java.util.function.Function[ITheOneProbe, Void] {
    override def apply(t: ITheOneProbe): Void = {
      t.registerProvider(new TankDataProvider)
      null
    }
  }

  val send: Reader[String, Boolean] = Reader {
    id =>
      InterModComms.sendTo(id, TOP_ID, "getTheOneProbe", () => new Function)
  }

  def sendIMC: Reader[String, Boolean] =
    if (ModList.get().isLoaded(TOP_ID)) send
    else Reader(_ => false)
}
*/