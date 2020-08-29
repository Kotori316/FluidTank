package com.kotori316.fluidtank.render

import java.util.Objects

import com.kotori316.fluidtank.tank.TileTank
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.block.entity.{BlockEntityRenderDispatcher, BlockEntityRenderer}
import net.minecraft.client.render.{RenderLayer, VertexConsumerProvider}
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.tag.FluidTags

@Environment(EnvType.CLIENT)
class RenderTank(d: BlockEntityRenderDispatcher) extends BlockEntityRenderer[TileTank](d) {

  override def render(te: TileTank, partialTicks: Float, matrix: MatrixStack, buffer: VertexConsumerProvider, light: Int, otherLight: Int): Unit = {
    MinecraftClient.getInstance.getProfiler.push("RenderTank")
    if (te.hasContent) {
      matrix.push()
      val b = buffer.getBuffer(RenderLayer.getCutoutMipped)
      val tank = te.tank
      if (tank.box != null) {
        val texture = RenderTank.textureName(te)
        //        val texture = MinecraftClient.getInstance.getSpriteAtlas(PlayerContainer.BLOCK_ATLAS_TEXTURE).apply(resource)
        val color = RenderTank.color(te)

        val value = Box.LightValue(light).overrideBlock(0 /*te.tank.fluid.fluid.getAttributes.getLuminosity(te.tank.fluid.toStack)*/)
        tank.box.render(b, matrix, texture, color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)(value)
      }
      matrix.pop()
    }
    MinecraftClient.getInstance.getProfiler.pop()
  }
}

object RenderTank {
  private def textureName(tile: TileTank) = {
    val tank: TileTank#Tank = tile.tank
    val world = if (tile.hasWorld) tile.getWorld else MinecraftClient.getInstance().world
    val pos = if (tile.hasWorld) tile.getPos else MinecraftClient.getInstance().player.getBlockPos
    Objects.requireNonNull(tank, "Tank should not be null.")
    Objects.requireNonNull(tank.fluid, "Content of Tank should not be null.")
    Objects.requireNonNull(world, "World should not be null.")
    Objects.requireNonNull(pos, "BlockPos should not be null.")
    val handler = FluidRenderHandlerRegistry.INSTANCE.get(tank.fluid.fluid)
    val sprites = handler.getFluidSprites(world, pos, tank.fluid.fluid.getDefaultState)
    sprites.apply(0)
  }

  private def color(tile: TileTank) = {
    val fluidAmount = tile.tank.fluid
    val world = if (tile.hasWorld) tile.getWorld else MinecraftClient.getInstance().world
    val pos = if (tile.hasWorld) tile.getPos else MinecraftClient.getInstance().player.getBlockPos
    val c = FluidRenderHandlerRegistry.INSTANCE.get(fluidAmount.fluid).getFluidColor(world, pos, fluidAmount.fluid.getDefaultState)
    if (fluidAmount.fluid.isIn(FluidTags.WATER)) {
      c | 0xFF000000
    } else {
      c
    }
  }

}
