package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.packet.ClientProxy
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class RenderItemTank extends TileEntityItemStackRenderer {

    val tileTank = new TileTank()

    override def renderByItem(stack: ItemStack, partialTicks: Float) = {
        stack.getItem match {
            case tankItem: ItemBlockTank =>
                val compound = stack.getSubCompound("BlockEntityTag")
                tileTank.tier = tankItem.blockTank.getTierByMeta(stack.getMetadata)
                tileTank.tank.setFluid(null)
                if (compound != null)
                    tileTank.readFromNBT(compound)

                GlStateManager.pushMatrix()
                //                GlStateManager.scale(-.5F, .5F, .5F)
                ClientProxy.RENDER_TANK.render(tileTank, 0d, 0d, 0d, partialTicks, -1, 1.0f)
                GlStateManager.popMatrix()

            case _ =>
        }
    }
}
