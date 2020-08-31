package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.ModTank
import com.kotori316.fluidtank.tank.{TankBlock, TankBlockItem, TileTank}
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.{DiffuseLighting, RenderLayers, VertexConsumer, VertexConsumerProvider}
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack

@Environment(EnvType.CLIENT)
class RenderItemTank extends BuiltinItemRenderer {

  lazy val tileTank = new TileTank()
  private final val renderBakedItemModel = classOf[ItemRenderer].getDeclaredMethod("renderBakedItemModel",
    classOf[BakedModel], classOf[ItemStack], Integer.TYPE, Integer.TYPE, classOf[MatrixStack], classOf[VertexConsumer])
  renderBakedItemModel.setAccessible(true)

  override def render(stack: ItemStack, matrixStack: MatrixStack, renderTypeBuffer: VertexConsumerProvider, light: Int, otherLight: Int): Unit = {
    stack.getItem match {
      case tankItem: TankBlockItem =>

        val state = tankItem.blockTank.getDefaultState
        val model = MinecraftClient.getInstance.getBlockRenderManager.getModel(state)
        //          ForgeHooksClient.handleCameraTransforms(matrixStack, Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state),
        //          TransformType.FIXED, false)
        val renderType = RenderLayers.getItemLayer(stack, true)
        val b = renderTypeBuffer.getBuffer(renderType)
        renderItemModel(MinecraftClient.getInstance().getItemRenderer, model, stack, light, otherLight, matrixStack, b)

        tileTank.tier = tankItem.blockTank.tiers
        tileTank.tank.setFluid(null)
        val compound = stack.getSubTag(TankBlock.NBT_BlockTag)
        if (compound != null)
          tileTank.readNBTClient(compound)
        DiffuseLighting.disable()
        BlockEntityRenderDispatcher.INSTANCE.renderEntity(
          tileTank, matrixStack, renderTypeBuffer, light, otherLight
        )

      case _ => ModTank.LOGGER.info("RenderItemTank is called for " + stack.getItem)
    }
  }

  // copy of ItemRenderer#func_229114_a_()
  def renderItemModel(renderer: ItemRenderer, model: BakedModel, stack: ItemStack, light: Int, otherLight: Int, matrixStack: MatrixStack, builder: VertexConsumer): Unit = {
    renderBakedItemModel.invoke(renderer,
      model, stack, light, otherLight, matrixStack, builder)
  }

}
