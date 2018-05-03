package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.{FluidTank, Utils}
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandler, IFluidTankProperties}

sealed class Connection(s: Seq[TileTank]) extends ICapabilityProvider {
    val seq: Seq[TileTank] = s.sortBy(_.getPos.getY)
    val handler: IFluidHandler = new IFluidHandler {
        override def fill(kind: FluidStack, doFill: Boolean): Int = {
            if (kind == null || kind.amount <= 0) {
                return 0
            }
            val resource = kind.copy()
            var total = 0
            for (tileTank <- tankSeq(kind)) {
                if (resource.amount > 0) {
                    val filled = tileTank.tank.fill(resource, doFill)
                    total += filled
                    resource.amount -= filled
                }
            }
            total
        }

        override def drain(kind: FluidStack, doDrain: Boolean): FluidStack = {
            if (kind == null || kind.amount <= 0) {
                return null
            }
            val resource = kind.copy()
            var total: FluidStack = null
            for (tileTank <- tankSeq(fluidType).reverse) {
                if (resource.amount > 0) {
                    val drained = tileTank.tank.drain(resource, doDrain)
                    if (drained != null) {
                        if (total == null) {
                            total = drained
                        } else {
                            total.amount += drained.amount
                        }
                    }
                }
            }
            total
        }

        override def drain(maxDrain: Int, doDrain: Boolean): FluidStack = {
            if (maxDrain <= 0) {
                return null
            }
            var toDrain = maxDrain
            var total: FluidStack = null
            for (tileTank <- tankSeq(fluidType).reverse) {
                if (toDrain > 0) {
                    if (total == null) {
                        total = tileTank.tank.drain(toDrain, doDrain)
                        if (total != null) {
                            toDrain -= total.amount
                        }
                    } else {
                        val copy = total.copy
                        copy.amount = toDrain
                        val drain = tileTank.tank.drain(copy, doDrain)
                        if (drain != null) {
                            total.amount += drain.amount
                            toDrain -= drain.amount
                        }
                    }
                }
            }
            total
        }

        override def getTankProperties: Array[IFluidTankProperties] = {
            Array(new FluidTankProperties(getFluidStack.orNull, Utils.toInt(capacity)))
        }
    }

    protected def fluidType: FluidStack = {
        seq.headOption.flatMap(Connection.stackFromTile).orElse(seq.lastOption.flatMap(Connection.stackFromTile)).orNull
    }

    def capacity: Long = seq.map(_.tier.amount.toLong).sum

    def amount: Long = seq.map(_.tank.getFluidAmount.toLong).sum

    def tankSeq(fluid: FluidStack): Seq[TileTank] = {
        if (fluid != null && fluid.getFluid.isGaseous(fluid)) {
            seq.reverse
        } else {
            seq
        }
    }

    def getFluidStack: Option[FluidStack] = {
        Option(fluidType).map(f => {
            val stack = f.copy()
            stack.amount = Utils.toInt(amount)
            stack
        })
    }

    /**
      * Make connection.
      *
      * @param tileTank The tank added to this connection.
      * @param facing   The facing that the tank should be connected to. UP and DOWN are valid.
      * @return new connection
      */
    def add(tileTank: TileTank, facing: EnumFacing): Connection = {
        val newFluid = tileTank.tank.getFluid
        if (newFluid == null || fluidType == null || fluidType == newFluid) {
            // You can connect the tank to this connection.
            val newSeq = if (facing == EnumFacing.DOWN) {
                tileTank +: seq
            } else {
                seq :+ tileTank
            }
            val connection = new Connection(newSeq)
            val fluidStacks = newSeq.flatMap(t => Option(t.tank.drain(t.tank.getFluid, true)).toList)
            newSeq.foreach(t => {
                t.connection = connection
                t.tank.setFluid(null)
            })
            fluidStacks.foreach(connection.handler.fill(_, true))
            connection
        } else {
            // You have to make new connection.
            val connection = new Connection(Seq(tileTank))
            tileTank.connection = connection
            connection
        }
    }

    def remove(tileTank: TileTank): Unit = {
        val (s1, s2) = seq.sortBy(_.getPos.getY).span(_ != tileTank)
        s1.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, EnumFacing.UP) }
        s2.tail.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, EnumFacing.UP) }
    }

    def getComparatorLevel: Int = {
        if (amount > 0)
            MathHelper.floor(amount.toDouble / capacity.toDouble * 14) + 1
        else 0
    }

    def updateComparator(): Unit = {
        seq.foreach(_.markDirty())
    }

    override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handler)
        } else {
            null.asInstanceOf[T]
        }
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing) =
        capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY

    override def toString: String = {
        val fluid = getFluidStack
        val name = fluid.map(_.getLocalizedName).getOrElse("null")
        s"Connection of $name : $amount / $capacity mB"
    }
}

object Connection {

    val invalid: Connection = new Connection(Nil) {
        override def fluidType: FluidStack = null

        override def capacity: Long = 0

        override def amount: Long = 0

        override val handler: IFluidHandler = EmptyFluidHandler.INSTANCE

        override val toString: String = "Connection.Invalid"

        override def getComparatorLevel: Int = 0

        override def remove(tileTank: TileTank): Unit = ()
    }

    val stackFromTile: TileTank => Option[FluidStack] = t => Option(t.tank.getFluid)

    def load(world: World, pos: BlockPos): Unit = {
        val lowest = Iterator.iterate(pos)(_.down()).takeWhile(p => world.getTileEntity(p).isInstanceOf[TileTank])
          .toList.lastOption.getOrElse({
            FluidTank.LOGGER.fatal("No lowest tank", new IllegalArgumentException("No lowest tank"))
            pos
        })
        val tanks = Iterator.iterate(lowest)(_.up()).map(world.getTileEntity).takeWhile(_.isInstanceOf[TileTank])
          .toList.map(_.asInstanceOf[TileTank])
        tanks.foldLeft(Connection.invalid) { case (c, tank) => c.add(tank, EnumFacing.UP) }
    }
}
