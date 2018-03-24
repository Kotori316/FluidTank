package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.Utils
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fluids.capability.templates.{EmptyFluidHandler, FluidHandlerConcatenate}
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandler, IFluidTankProperties}
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidUtil}

class Connection(fisrt: TileTank, seq: Seq[TileTank]) extends ICapabilityProvider {

    //TODO strict fluid to be inserted.
    val handler: IFluidHandler = new FluidHandlerConcatenate(seq.map(_.tank): _*) {
        override def getTankProperties: Array[IFluidTankProperties] = {
            val fluid = fluidType
            if (fluid != null) {
                Array(new FluidTankProperties(new FluidStack(fluidType, Utils.toInt(amount)), Utils.toInt(capacity)))
            } else {
                Array(new FluidTankProperties(null, Utils.toInt(capacity)))
            }
        }
    }
    seq.foreach(tileTank => FluidUtil.tryFluidTransfer(handler, tileTank.tank, tileTank.tank.getFluid, true))

    def fluidType = Option(fisrt.tank.getFluid).map(_.getFluid).orNull

    def capacity = seq.map(_.tier.amount.toLong).sum

    def amount = seq.map(_.tank.getFluidAmount.toLong).sum

    def hasChain = seq.size > 1

    override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handler)
        } else {
            null.asInstanceOf[T]
        }
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing) =
        capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY

    override def toString: String = s"Connection of $fluidType : $amount / $capacity mB"
}

object Connection {

    val invalid = new Connection(null, Nil) {
        override def fluidType: Fluid = null

        override def capacity: Long = 0

        override def amount: Long = 0

        override val handler: IFluidHandler = EmptyFluidHandler.INSTANCE

        override val toString: String = "Connection.Invalid"
    }

    def apply(fisrt: Option[TileTank], seq: Seq[TileTank]): Connection =
        fisrt match {
            case Some(tank) => new Connection(tank, seq)
            case None => invalid
        }
}