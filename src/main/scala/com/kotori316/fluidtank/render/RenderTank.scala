package com.kotori316.fluidtank.render

import java.util.Objects

import com.kotori316.fluidtank.Utils
import com.kotori316.fluidtank.tank.TileTank
import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.api.{EnvType, Environment}
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.blockentity.{BlockEntityRenderer, BlockEntityRendererProvider}
import net.minecraft.client.renderer.{MultiBufferSource, RenderType}
import net.minecraft.core.Direction
import net.minecraft.tags.FluidTags

@Environment(EnvType.CLIENT)
class RenderTank(d: BlockEntityRendererProvider.Context) extends BlockEntityRenderer[TileTank] {

  override def render(te: TileTank, partialTicks: Float, matrix: PoseStack, buffer: MultiBufferSource, light: Int, otherLight: Int): Unit = {
    Minecraft.getInstance.getProfiler.push("RenderTank")
    if (te.hasContent) {
      matrix.push()
      val tank = te.tank
      if (tank.box != null) {
        if (tank.fluid.fluid != null) {
          val texture = RenderTank.textureName(te)
          //        val texture = Minecraft.getInstance.getSpriteAtlas(PlayerContainer.BLOCK_ATLAS_TEXTURE).apply(resource)
          val color = RenderTank.color(te)

          val b = buffer.getBuffer {
            if (Minecraft.useShaderTransparency()) RenderType.cutout()
            else RenderType.translucent()
          }
          val value = Box.LightValue(light).overrideBlock(te.tank.fluid.fluidVolume.getFluidKey.luminosity)
          tank.box.render(b, matrix, texture, color >> 24 & 0xFF, color >> 16 & 0xFF, color >> 8 & 0xFF, color >> 0 & 0xFF)(value)
        } else {
          import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace
          val faces = new java.util.ArrayList[FluidRenderFace]
          val x0 = tank.box.startX - tank.box.offX
          val y0 = tank.box.startY
          val z0 = tank.box.startZ - tank.box.offZ
          val x1 = tank.box.endX + tank.box.offX
          val y1 = tank.box.endY
          val z1 = tank.box.endZ + tank.box.offZ
          FluidRenderFace.appendCuboid(x0, y0, z0, x1, y1, z1, 1.0D, java.util.EnumSet.allOf(classOf[Direction]), faces)
          faces.forEach(_.light = light)
          tank.fluid.fluidVolume.render(faces, buffer, matrix)
        }
      }
      matrix.pop()
    }
    Minecraft.getInstance.getProfiler.pop()
  }
}

object RenderTank {
  private def textureName(tile: TileTank) = {
    val tank: TileTank#Tank = tile.tank
    val (world, pos) = getWorldAndPos(tile)
    Objects.requireNonNull(tank, "Tank should not be null.")
    Objects.requireNonNull(tank.fluid, "Content of Tank should not be null.")
    Objects.requireNonNull(world, "World should not be null.")
    Objects.requireNonNull(pos, "BlockPos should not be null.")
    val handler = FluidRenderHandlerRegistry.INSTANCE.get(tank.fluid.fluid)
    val sprites = handler.getFluidSprites(world, pos, tank.fluid.fluid.defaultFluidState())
    sprites.apply(0)
  }

  private def color(tile: TileTank) = {
    val fluidAmount = tile.tank.fluid
    val (world, pos) = getWorldAndPos(tile)
    val c = FluidRenderHandlerRegistry.INSTANCE.get(fluidAmount.fluid).getFluidColor(world, pos, fluidAmount.fluid.defaultFluidState())
    if (Utils.fluidIs(fluidAmount.fluid, FluidTags.WATER)) {
      c | 0xFF000000
    } else {
      c
    }
  }

  private[this] def getWorldAndPos(tileTank: TileTank) =
    if (tileTank.hasLevel) (tileTank.getLevel, tileTank.getBlockPos) else (Minecraft.getInstance().level, Minecraft.getInstance().player.getOnPos)
}
