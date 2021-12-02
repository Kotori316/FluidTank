package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.fluids.Tank
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.TileTank
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.{BlockEntityWithoutLevelRenderer, MultiBufferSource}
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.BlockPos
import net.minecraft.world.item.{BlockItem, ItemStack}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

import scala.collection.mutable

@OnlyIn(Dist.CLIENT)
class RenderItemTank extends BlockEntityWithoutLevelRenderer(Minecraft.getInstance.getBlockEntityRenderDispatcher, Minecraft.getInstance.getEntityModels) {

  lazy val tileTank = new TileTank(BlockPos.ZERO, ModObjects.blockTanks.head.defaultBlockState())
  private final val modelWrapperMap = mutable.Map.empty[BakedModel, TankModelWrapper]

  override def renderByItem(stack: ItemStack, cameraType: ItemTransforms.TransformType, matrixStack: PoseStack,
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
        Minecraft.getInstance.getBlockEntityRenderDispatcher.renderItem(
          tileTank, matrixStack, renderTypeBuffer, light, otherLight
        )

      case _ => FluidTank.LOGGER.info(ModObjects.MARKER_RenderItemTank, "RenderItemTank is called for " + stack.getItem)
    }
  }

  // copy of ItemRenderer#func_229114_a_()
  def renderItemModel(renderer: ItemRenderer, model: BakedModel, stack: ItemStack, light: Int, otherLight: Int, matrixStack: PoseStack, renderTypeBuffer: MultiBufferSource): Unit = {
    val tankModelWrapper = modelWrapperMap.getOrElseUpdate(model, new TankModelWrapper(model))
    matrixStack.pushPose()
    matrixStack.translate(0.5D, 0.5D, 0.5D)
    renderer.render(stack, ItemTransforms.TransformType.NONE, false, matrixStack, renderTypeBuffer, light, otherLight, tankModelWrapper)
    matrixStack.popPose()
  }

}
