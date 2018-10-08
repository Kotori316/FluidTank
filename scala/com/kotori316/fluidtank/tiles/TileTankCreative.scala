package com.kotori316.fluidtank.tiles

import net.minecraftforge.fluids.FluidStack

class TileTankCreative extends TileTank(Tiers.CREATIVE) {

    override val tank = new CreativeTank

    class CreativeTank extends Tank {

        import com.kotori316.fluidtank.blocks.AbstractTank._

        setCapacity(Int.MaxValue)

        override def fillInternal(resource: FluidStack, doFill: Boolean): Int = {
            if (resource.isEmpty) return 0
            if (!doFill) {
                if (fluid.isEmpty) resource.amount
                else 0
            } else {
                if (fluid.isEmpty) {
                    fluid = new FluidStack(resource, getCapacity)
                    onContentsChanged()
                    resource.amount
                }
                else 0
            }
        }

        override def drainInternal(maxDrain: Int, doDrain: Boolean): FluidStack = {
            if (fluid.isEmpty || maxDrain <= 0) null
            else new FluidStack(fluid, maxDrain)
        }

        def drainAll(): Unit = {
            fluid = null
            onContentsChanged()
        }
    }

}
