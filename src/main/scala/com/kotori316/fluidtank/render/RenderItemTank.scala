package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.fluids.Tank
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.TileTank
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.BlockPos
import net.minecraft.world.item.{BlockItem, ItemDisplayContext, ItemStack}

@Environment(EnvType.CLIENT)
class RenderItemTank extends BuiltinItemRendererRegistry.DynamicItemRenderer {

  lazy val tileTank = new TileTank(BlockPos.ZERO, ModObjects.blockTanks.head.defaultBlockState())
  private val internalModel = new ModelWrapper(null)

  override def render(stack: ItemStack, cameraType: ItemDisplayContext, matrixStack: PoseStack,
                      renderTypeBuffer: MultiBufferSource, light: Int, otherLight: Int): Unit = {
    stack.getItem match {
      case tankItem: ItemBlockTank =>

        val state = tankItem.blockTank.defaultBlockState()
        val model = Minecraft.getInstance.getBlockRenderer.getBlockModel(state)
        //          ForgeHooksClient.handleCameraTransforms(matrixStack, Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state),
        //          TransformType.FIXED, false)
        //val renderType = RenderTypeLookup.func_239219_a_(stack, true)
        //val b = ItemRenderer.getBuffer(renderTypeBuffer, renderType, true, stack.hasEffect)
        RenderSystem.enableCull()
        renderItemModel(Minecraft.getInstance().getItemRenderer, model, stack, light, otherLight, matrixStack, renderTypeBuffer)

        tileTank.tier = tankItem.blockTank.tier
        tileTank.internalTank.setTank(Tank.EMPTY)
        val compound = BlockItem.getBlockEntityData(stack)
        if (compound != null)
          tileTank.readNBTClient(compound)
        //        RenderHelper.disableStandardItemLighting()
        Lighting.setupForFlatItems()
        Minecraft.getInstance.getBlockEntityRenderDispatcher.renderItem(
          tileTank, matrixStack, renderTypeBuffer, light, otherLight
        )
        Lighting.setupFor3DItems()

      case _ => FluidTank.LOGGER.info(ModObjects.MARKER_RenderItemTank, "RenderItemTank is called for " + stack.getItem)
    }
  }

  // copy of ItemRenderer#func_229114_a_()
  def renderItemModel(renderer: ItemRenderer, model: BakedModel, stack: ItemStack, light: Int, otherLight: Int,
                      matrixStack: PoseStack, renderTypeBuffer: MultiBufferSource): Unit = {
    //    renderBakedItemModel.invoke(renderer,
    //      model, stack, light, otherLight, matrixStack, builder)
    internalModel.setModel(model)
    matrixStack.pushPose()
    matrixStack.translate(0.5D, 0.5D, 0.5D)
    renderer.render(stack, ItemDisplayContext.NONE, false, matrixStack, renderTypeBuffer, light, otherLight, internalModel)
    matrixStack.popPose()
  }

}
