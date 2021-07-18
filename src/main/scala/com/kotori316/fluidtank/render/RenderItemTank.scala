package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.ModTank
import com.kotori316.fluidtank.tank.{TankBlock, TankBlockItem, TileTank}
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.render.{DiffuseLighting, VertexConsumerProvider}
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

@Environment(EnvType.CLIENT)
class RenderItemTank extends BuiltinItemRendererRegistry.DynamicItemRenderer {

  lazy val tileTank = new TileTank(BlockPos.ORIGIN, ModTank.Entries.WOOD_TANK.getDefaultState)
  private val internalModel = new RenderItemTank.Model

  override def render(stack: ItemStack, mode: ModelTransformation.Mode, matrixStack: MatrixStack, renderTypeBuffer: VertexConsumerProvider, light: Int, otherLight: Int): Unit = {
    stack.getItem match {
      case tankItem: TankBlockItem =>

        val state = tankItem.blockTank.getDefaultState
        val model = MinecraftClient.getInstance.getBlockRenderManager.getModel(state)
        //          ForgeHooksClient.handleCameraTransforms(matrixStack, Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state),
        //          TransformType.FIXED, false)
        //val renderType = RenderLayers.getItemLayer(stack, true)
        //val b = renderTypeBuffer.getBuffer(renderType)
        renderItemModel(MinecraftClient.getInstance().getItemRenderer, model, stack, light, otherLight, matrixStack, renderTypeBuffer)

        tileTank.tier = tankItem.blockTank.tiers
        tileTank.tank.setFluid(null)
        val compound = stack.getSubNbt(TankBlock.NBT_BlockTag)
        if (compound != null)
          tileTank.readNBTClient(compound)
        DiffuseLighting.disableGuiDepthLighting()
        MinecraftClient.getInstance.getBlockEntityRenderDispatcher.renderEntity(
          tileTank, matrixStack, renderTypeBuffer, light, otherLight
        )

      case _ => ModTank.LOGGER.warn("RenderItemTank is called for " + stack.getItem)
    }
  }

  // copy of ItemRenderer#func_229114_a_()
  def renderItemModel(renderer: ItemRenderer, model: BakedModel, stack: ItemStack, light: Int, otherLight: Int,
                      matrixStack: MatrixStack, renderTypeBuffer: VertexConsumerProvider): Unit = {
    //    renderBakedItemModel.invoke(renderer,
    //      model, stack, light, otherLight, matrixStack, builder)
    internalModel.setModel(model)
    matrixStack.push()
    matrixStack.translate(0.5D, 0.5D, 0.5D)
    renderer.renderItem(stack, ModelTransformation.Mode.NONE, false, matrixStack, renderTypeBuffer, light, otherLight, internalModel)
    matrixStack.pop()
  }

}

object RenderItemTank {

  private class Model extends ForwardingBakedModel {
    def setModel(newModel: BakedModel): Unit = {
      this.wrapped = newModel
    }

    override def isBuiltin: Boolean = false
  }

}
