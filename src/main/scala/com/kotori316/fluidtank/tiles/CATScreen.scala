package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.FluidTank
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class CATScreen(screenContainer: CATContainer, inv: Inventory, titleIn: Component)
  extends AbstractContainerScreen[CATContainer](screenContainer, inv, titleIn) {
  private final val catTile = screenContainer.catTile

  override protected def renderBg(matrices: PoseStack, delta: Float, mouseX: Int, mouseY: Int): Unit = {
    RenderSystem.setShader(() => GameRenderer.getPositionTexShader)
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F)
    RenderSystem.setShaderTexture(0, new ResourceLocation(FluidTank.modID, "textures/gui/cat.png"))
    this.blit(matrices, getGuiLeft, getGuiTop, 0, 0, imageWidth, imageHeight)
  }

  override protected def renderLabels(matrices: PoseStack, mouseX: Int, mouseY: Int): Unit = {
    super.renderLabels(matrices, mouseX, mouseY)
    val stacks = catTile.fluidCache
    for ((a, i) <- stacks.zipWithIndex) {
      this.font.draw(matrices, a.toString, 8.toFloat, (16 + 10 * i).toFloat, 0x404040)
    }
  }

  override def render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float): Unit = {
    this.renderBackground(matrices)
    super.render(matrices, mouseX, mouseY, delta)
    this.renderTooltip(matrices, mouseX, mouseY)
  }
}