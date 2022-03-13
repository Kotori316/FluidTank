package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.items.ReservoirItem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.{BlockEntityWithoutLevelRenderer, MultiBufferSource, RenderType}
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler

import scala.jdk.OptionConverters._

@OnlyIn(Dist.CLIENT)
object RenderReservoirItem {
  private final val d = 1f / 16f

  private def renderFluid(stack: FluidStack, capacity: Int, matrixStack: PoseStack, buffer: MultiBufferSource, light: Int, overlay: Int): Unit = {
    val textureName = stack.getFluid.getAttributes.getStillTexture(stack)
    val texture = Minecraft.getInstance.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureName)
    val color = stack.getFluid.getAttributes.getColor(stack)
    val wrapper = new Wrapper(buffer.getBuffer(RenderType.translucent))
    val alpha = if ((color >> 24 & 0xFF) > 0) color >> 24 & 0xFF else 0xFF
    val red = color >> 16 & 0xFF
    val green = color >> 8 & 0xFF
    val blue = color & 0xFF

    val height = Math.max(0.01f, stack.getAmount.toFloat / capacity)
    val maxV = texture.getV1 //texture.getV(height / d);
    val minV = texture.getV0
    val maxU = texture.getU1
    val minU = texture.getU0
    wrapper.pos(2 * d, height, 0.5, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightMap(light, overlay).endVertex()
    wrapper.pos(2 * d, 0, 0.5, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightMap(light, overlay).endVertex()
    wrapper.pos(14 * d, 0, 0.5, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightMap(light, overlay).endVertex()
    wrapper.pos(14 * d, height, 0.5, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightMap(light, overlay).endVertex()
  }
}

@OnlyIn(Dist.CLIENT)
class RenderReservoirItem() extends BlockEntityWithoutLevelRenderer(Minecraft.getInstance.getBlockEntityRenderDispatcher, Minecraft.getInstance.getEntityModels) {
  override def renderByItem(stack: ItemStack, cameraType: ItemTransforms.TransformType, matrixStack: PoseStack, buffer: MultiBufferSource, combinedLight: Int, combinedOverlay: Int): Unit =
    if (stack.getItem.isInstanceOf[ReservoirItem]) {
      val itemModel = Minecraft.getInstance.getItemRenderer.getItemModelShaper.getItemModel(stack)
      itemModel match {
        case wrapper: ModelWrapper =>
          matrixStack.pushPose()
          matrixStack.translate(0.5D, 0.5D, 0.5D)
          val original = wrapper.getOriginalModel
          Minecraft.getInstance.getItemRenderer.render(stack, cameraType, false, matrixStack, buffer, combinedLight, combinedOverlay, original)
          matrixStack.popPose()
          if (cameraType == ItemTransforms.TransformType.GUI) {
            matrixStack.pushPose()
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
              .resolve()
              .toScala
              .map(h => (h.getFluidInTank(0), h.getTankCapacity(0)))
              .filter { case (stack, _) => !stack.isEmpty && stack.getAmount > 0 }
              .foreach { case (stack, capacity) => RenderReservoirItem.renderFluid(stack, capacity, matrixStack, buffer, combinedLight, combinedOverlay) }
            matrixStack.popPose()
          }
        case _ =>
      }
    }
}
