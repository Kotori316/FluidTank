package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.network.ClientProxy
import com.kotori316.fluidtank.tiles.{TileTank, TileTankNoDisplay}
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.AtlasTexture
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{RenderHelper, Tessellator}
import net.minecraft.item.ItemStack
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import org.lwjgl.opengl.GL11
/*
@OnlyIn(Dist.CLIENT)
class RenderItemTank extends ItemStackTileEntityRenderer {

  lazy val tileTank = new TileTank()

  override def renderByItem(stack: ItemStack): Unit = {
    stack.getItem match {
      case tankItem: ItemBlockTank =>
        tileTank.tier = tankItem.blockTank.tier
        tileTank.tank.setFluid(null)
        val compound = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)
        if (compound != null)
          tileTank.readNBTClient(compound)
        val inSlot = Minecraft.getInstance.getItemRenderer.zLevel > 0

        val tessellator = Tessellator.getInstance
        val buffer = tessellator.getBuffer
        ClientProxy.RENDER_TANK.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE)

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

        ClientProxy.RENDER_TANK.renderTileEntityFast(tileTank, 0, 0, 0, 0, -1, buffer)
        buffer.setTranslation(0, 0, 0)
        tessellator.draw()
        if (!inSlot) RenderHelper.enableStandardItemLighting()

        val state = tankItem.blockTank.getDefaultState
        val model = Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state)
        GlStateManager.pushMatrix()
        GlStateManager.translatef(0.5f, 0.5f, 0.5f)
        Minecraft.getInstance().getItemRenderer.renderItem(stack, model)
        GlStateManager.popMatrix()

      case _ => FluidTank.LOGGER.info("RenderItemTank is called for " + stack.getItem)
    }
  }
}
*/