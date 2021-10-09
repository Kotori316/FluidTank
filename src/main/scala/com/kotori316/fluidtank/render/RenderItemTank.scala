package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.fluids.Tank
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.TileTank
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.model.{IBakedModel, ItemCameraTransforms}
import net.minecraft.client.renderer.tileentity.{ItemStackTileEntityRenderer, TileEntityRendererDispatcher}
import net.minecraft.client.renderer.{IRenderTypeBuffer, ItemRenderer, RenderHelper}
import net.minecraft.item.ItemStack
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

import scala.collection.mutable

@OnlyIn(Dist.CLIENT)
class RenderItemTank extends ItemStackTileEntityRenderer {

  lazy val tileTank = new TileTank()
  private final val modelWrapperMap = mutable.Map.empty[IBakedModel, TankModelWrapper]

  override def func_239207_a_(stack: ItemStack, cameraType: ItemCameraTransforms.TransformType, matrixStack: MatrixStack,
                              renderTypeBuffer: IRenderTypeBuffer, light: Int, otherLight: Int): Unit = {
    stack.getItem match {
      case tankItem: ItemBlockTank =>

        val state = tankItem.blockTank.getDefaultState
        val model = Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state)
        //          ForgeHooksClient.handleCameraTransforms(matrixStack, Minecraft.getInstance.getBlockRendererDispatcher.getModelForState(state),
        //          TransformType.FIXED, false)
        //val renderType = RenderTypeLookup.func_239219_a_(stack, true)
        //val b = ItemRenderer.getBuffer(renderTypeBuffer, renderType, true, stack.hasEffect)
        RenderSystem.enableCull()
        renderItemModel(Minecraft.getInstance().getItemRenderer, model, stack, light, otherLight, matrixStack, renderTypeBuffer)

        tileTank.tier = tankItem.blockTank.tier
        tileTank.internalTank.setTank(Tank.EMPTY)
        val compound = stack.getChildTag(TileTank.NBT_BlockTag)
        if (compound != null)
          tileTank.readNBTClient(compound)
        RenderHelper.disableStandardItemLighting()
        TileEntityRendererDispatcher.instance.renderItem(
          tileTank, matrixStack, renderTypeBuffer, light, otherLight
        )

      case _ => FluidTank.LOGGER.info(ModObjects.MARKER_RenderItemTank, "RenderItemTank is called for " + stack.getItem)
    }
  }

  // copy of ItemRenderer#func_229114_a_()
  def renderItemModel(renderer: ItemRenderer, model: IBakedModel, stack: ItemStack, light: Int, otherLight: Int, matrixStack: MatrixStack, renderTypeBuffer: IRenderTypeBuffer): Unit = {
    val tankModelWrapper = modelWrapperMap.getOrElseUpdate(model, new TankModelWrapper(model))
    matrixStack.push()
    matrixStack.translate(0.5D, 0.5D, 0.5D)
    renderer.renderItem(stack, ItemCameraTransforms.TransformType.NONE, false, matrixStack, renderTypeBuffer, light, otherLight, tankModelWrapper)
    matrixStack.pop()
  }

}
