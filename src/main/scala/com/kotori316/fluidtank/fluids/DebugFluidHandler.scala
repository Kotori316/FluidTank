package com.kotori316.fluidtank.fluids

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.fluids.DebugFluidHandler.{LOGGER, MARKER}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler
import org.apache.logging.log4j.LogManager

class DebugFluidHandler private() extends EmptyFluidHandler {
  override def fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int = {
    LOGGER.info(MARKER, "Fill {}, mode {}", resource: AnyRef, action: AnyRef)
    super.fill(resource, action)
  }

  override def drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = {
    LOGGER.info(MARKER, "Drain {}, mode {}", resource: AnyRef, action: AnyRef)
    super.drain(resource, action)
  }

  override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = {
    LOGGER.info(MARKER, "Drain amount {}, mode {}", maxDrain: Any, action: AnyRef)
    super.drain(maxDrain, action)
  }

  override def toString: String = "DebugFluidHandler@FluidTank"
}

object DebugFluidHandler {
  final val INSTANCE = new DebugFluidHandler
  private final val LOGGER = LogManager.getLogger(classOf[DebugFluidHandler])
  private final val MARKER = ModObjects.MARKER_DebugFluidHandler
}