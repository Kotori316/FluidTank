package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.ModTank
import com.kotori316.fluidtank.tank.{TankBlock, TankBlockItem, TileTank}
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack

@Environment(EnvType.CLIENT)
class RenderItemTank extends BuiltinItemRendererRegistry.DynamicItemRenderer {

  lazy val tileTank = new TileTank(BlockPos.ZERO, ModTank.Entries.WOOD_TANK.defaultBlockState())
  private val internalModel = new RenderItemTank.Model

  override def render(stack: ItemStack, mode: ItemTransforms.TransformType, matrixStack: PoseStack, renderTypeBuffer: MultiBufferSource, light: Int, otherLight: Int): Unit = {
    stack.getItem match {
      case tankItem: TankBlockItem =>

        val state = tankItem.blockTank.defaultBlockState()
        val model = Minecraft.getInstance.getBlockRenderer.getBlockModel(state)
        //          ForgeHooksClient.handleCameraTransforms(matrixStack, Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state),
        //          TransformType.FIXED, false)
        //val renderType = RenderLayers.getItemLayer(stack, true)
        //val b = renderTypeBuffer.getBuffer(renderType)
        renderItemModel(Minecraft.getInstance().getItemRenderer, model, stack, light, otherLight, matrixStack, renderTypeBuffer)

        tileTank.tier = tankItem.blockTank.tiers
        tileTank.tank.setFluid(null)
        val compound = stack.getTagElement(TankBlock.NBT_BlockTag)
        if (compound != null)
          tileTank.readNBTClient(compound)
        Lighting.setupForFlatItems()
        Minecraft.getInstance.getBlockEntityRenderDispatcher.renderItem(
          tileTank, matrixStack, renderTypeBuffer, light, otherLight
        )
        Lighting.setupFor3DItems()

      case _ => ModTank.LOGGER.warn("RenderItemTank is called for " + stack.getItem)
    }
  }

  // copy of ItemRenderer#func_229114_a_()
  def renderItemModel(renderer: ItemRenderer, model: BakedModel, stack: ItemStack, light: Int, otherLight: Int,
                      matrixStack: PoseStack, renderTypeBuffer: MultiBufferSource): Unit = {
    //    renderBakedItemModel.invoke(renderer,
    //      model, stack, light, otherLight, matrixStack, builder)
    internalModel.setModel(model)
    matrixStack.push()
    matrixStack.translate(0.5D, 0.5D, 0.5D)
    renderer.render(stack, ItemTransforms.TransformType.NONE, false, matrixStack, renderTypeBuffer, light, otherLight, internalModel)
    matrixStack.pop()
  }

}

object RenderItemTank {

  private class Model extends ForwardingBakedModel {
    def setModel(newModel: BakedModel): Unit = {
      this.wrapped = newModel
    }

    override def isCustomRenderer: Boolean = false
  }

}
