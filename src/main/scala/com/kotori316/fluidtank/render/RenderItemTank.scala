package com.kotori316.fluidtank.render

import java.util.Random

import cats.syntax.eq._
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{TileTank, TileTankNoDisplay}
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.tileentity.{ItemStackTileEntityRenderer, TileEntityRendererDispatcher}
import net.minecraft.client.renderer.{IRenderTypeBuffer, ItemRenderer, RenderHelper, RenderTypeLookup}
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.client.model.data.EmptyModelData

@OnlyIn(Dist.CLIENT)
class RenderItemTank extends ItemStackTileEntityRenderer {

  lazy val tileTank = new TileTank()

  override def func_228364_a_(stack: ItemStack, matrixStack: MatrixStack, renderTypeBuffer: IRenderTypeBuffer, light: Int, otherLight: Int): Unit = {
    stack.getItem match {
      case tankItem: ItemBlockTank =>

        val state = ModObjects.blockTanksInvisible.find(_.tier === tankItem.blockTank.tier).map(_.getDefaultState).getOrElse(tankItem.blockTank.getDefaultState)
        val model = Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state)
        //          ForgeHooksClient.handleCameraTransforms(matrixStack, Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state),
        //          TransformType.FIXED, false)
        val renderType = RenderTypeLookup.func_228389_a_(stack)
        val b = ItemRenderer.func_229113_a_(renderTypeBuffer, renderType, true, stack.hasEffect)
        renderItemModel(Minecraft.getInstance().getItemRenderer, model, stack, light, otherLight, matrixStack, b)

        tileTank.tier = tankItem.blockTank.tier
        tileTank.tank.setFluid(null)
        val compound = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)
        if (compound != null)
          tileTank.readNBTClient(compound)
        RenderHelper.disableStandardItemLighting()
        TileEntityRendererDispatcher.instance.func_228852_a_(
          tileTank, matrixStack, renderTypeBuffer, light, otherLight
        )

      case _ => FluidTank.LOGGER.info("RenderItemTank is called for " + stack.getItem)
    }
  }

  // copy of ItemRenderer#func_229114_a_()
  def renderItemModel(renderer: ItemRenderer, model: IBakedModel, stack: ItemStack, light: Int, otherLight: Int, matrixStack: MatrixStack, builder: IVertexBuilder): Unit = {
    val random = new Random
    val seed = 42L

    for (direction <- Direction.values) {
      random.setSeed(seed)
      renderer.func_229112_a_(matrixStack, builder, model.getQuads(null, direction, random, EmptyModelData.INSTANCE), stack, light, otherLight)
    }

    random.setSeed(seed)
    renderer.func_229112_a_(matrixStack, builder, model.getQuads(null, null, random, EmptyModelData.INSTANCE), stack, light, otherLight)

  }

}
