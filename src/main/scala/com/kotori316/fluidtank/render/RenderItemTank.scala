package com.kotori316.fluidtank.render

import java.util.Random

import com.kotori316.fluidtank.ModTank
import com.kotori316.fluidtank.tank.{TankBlock, TankBlockItem, TileTank}
import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.item.{BuiltinModelItemRenderer, ItemRenderer}
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.{DiffuseLighting, RenderLayers, VertexConsumer, VertexConsumerProvider}
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

@Environment(EnvType.CLIENT)
class RenderItemTank extends BuiltinModelItemRenderer {

  lazy val tileTank = new TileTank()

  override def render(stack: ItemStack, matrixStack: MatrixStack, renderTypeBuffer: VertexConsumerProvider, light: Int, otherLight: Int): Unit = {
    stack.getItem match {
      case tankItem: TankBlockItem =>

        val state = tankItem.blockTank.getDefaultState
        val model = MinecraftClient.getInstance.getBlockRenderManager.getModel(state)
        //          ForgeHooksClient.handleCameraTransforms(matrixStack, Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state),
        //          TransformType.FIXED, false)
        val renderType = RenderLayers.getItemLayer(stack)
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
    val random = new Random
    val seed = 42L

    for (direction <- Direction.values) {
      random.setSeed(seed)
//      renderer.renderBakedItemQuads(matrixStack, builder, model.getQuads(null, direction, random), stack, light, otherLight)
    }

    random.setSeed(seed)
//    renderer.renderBakedItemQuads(matrixStack, builder, model.getQuads(null, null, random), stack, light, otherLight)

  }

}
