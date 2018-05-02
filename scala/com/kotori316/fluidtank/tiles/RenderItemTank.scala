package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.packet.ClientProxy
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class RenderItemTank extends TileEntityItemStackRenderer {

    val tileTank = new TileTank()

    override def renderByItem(stack: ItemStack, partialTicks: Float) = {
        stack.getItem match {
            case tankItem: ItemBlockTank =>
                tileTank.tier = tankItem.blockTank.getTierByMeta(stack.getMetadata)
                tileTank.tank.setFluid(null)
                val compound = stack.getSubCompound("BlockEntityTag")
                if (compound != null)
                    tileTank.readFromNBT(compound)

                ClientProxy.RENDER_TANK.render(tileTank, 0d, 0d, 0d, partialTicks, -1, 1.0f)

            case _ => FluidTank.LOGGER.info("RenderItemTank is called for " + stack.getItem)
        }
    }
}
