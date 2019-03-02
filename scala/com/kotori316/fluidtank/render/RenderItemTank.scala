package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.packet.ClientProxy
import com.kotori316.fluidtank.tiles.{TileTank, TileTankNoDisplay}
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{GlStateManager, RenderHelper, Tessellator}
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

@SideOnly(Side.CLIENT)
class RenderItemTank extends TileEntityItemStackRenderer {

  lazy val tileTank = new TileTank()

  override def renderByItem(stack: ItemStack, partialTicks: Float): Unit = {
    stack.getItem match {
      case tankItem: ItemBlockTank =>
        tileTank.tier = tankItem.blockTank.getTierByMeta(stack.getMetadata)
        tileTank.tank.setFluid(null)
        val compound = stack.getSubCompound(TileTankNoDisplay.NBT_BlockTag)
        if (compound != null)
          tileTank.readNBTClient(compound)
        val inSlot = Minecraft.getMinecraft.getRenderItem.zLevel > 0

        val tessellator = Tessellator.getInstance
        val buffer = tessellator.getBuffer
        ClientProxy.RENDER_TANK.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)

        if (!inSlot) RenderHelper.disableStandardItemLighting()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableBlend()
        //                GlStateManager.disableCull()
        GlStateManager.enableRescaleNormal()
        if (Minecraft.isAmbientOcclusionEnabled)
          GlStateManager.shadeModel(GL11.GL_SMOOTH)
        else
          GlStateManager.shadeModel(GL11.GL_FLAT)
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK)

        ClientProxy.RENDER_TANK.renderTileEntityFast(tileTank, 0, 0, 0, partialTicks, -1, 1, buffer)
        buffer.setTranslation(0, 0, 0)
        tessellator.draw()
        if (!inSlot) RenderHelper.enableStandardItemLighting()

        val state = FluidTank.BLOCK_TANKS.get(tileTank.tier.rank - 1).getStateFromMeta(tileTank.tier.meta)
        val model = Minecraft.getMinecraft.getBlockRendererDispatcher.getModelForState(state)
        GlStateManager.pushMatrix()
        GlStateManager.translate(0.5f, 0.5f, 0.5f)
        Minecraft.getMinecraft.getRenderItem.renderItem(stack, model)
        GlStateManager.popMatrix()

      case _ => FluidTank.LOGGER.info("RenderItemTank is called for " + stack.getItem)
    }
  }
}
