package com.kotori316.fluidtank.recipes

import java.lang.{Boolean => JBool}

import com.google.gson.JsonElement
import com.kotori316.fluidtank.blocks.{BlockCAT, BlockTank, FluidSourceBlock, TankPos}
import net.minecraft.data.{BlockModelDefinition, BlockModelFields, BlockStateVariantBuilder, FinishedVariantBlockState, IFinishedBlockState, ModelsResourceUtil}
import net.minecraft.state.properties.BlockStateProperties
import net.minecraft.util.{Direction, ResourceLocation}

case class ModelSerializerHelper(location: ResourceLocation, stateSupplier: IFinishedBlockState) {
  def build: JsonElement = {
    stateSupplier.get()
  }
}

object ModelSerializerHelper {
  def getTankModel(block: BlockTank): ModelSerializerHelper = {
    val name = {
      val n = ModelsResourceUtil.func_240221_a_(block)
      new ResourceLocation(n.getNamespace,
        n.getPath
          .replace("invisible_", "")
          .replace("creative", "tank_creative")
      )
    }
    val model = BlockModelDefinition.getNewModelDefinition.replaceInfoValue(BlockModelFields.field_240202_c_, name)
    val variantBuilder = BlockStateVariantBuilder.func_240133_a_(TankPos.TANK_POS_PROPERTY)
      .func_240143_a_(TankPos.TOP, model)
      .func_240143_a_(TankPos.MIDDLE, model)
      .func_240143_a_(TankPos.BOTTOM, model)
      .func_240143_a_(TankPos.SINGLE, model)
    ModelSerializerHelper(block.getRegistryName, FinishedVariantBlockState.func_240119_a_(block).func_240125_a_(variantBuilder))
  }

  def getFluidSourceModel(block: FluidSourceBlock): ModelSerializerHelper = {
    val name = ModelsResourceUtil.func_240221_a_(block)
    val modelNonCheat = BlockModelDefinition.getNewModelDefinition.replaceInfoValue(BlockModelFields.field_240202_c_, name)
    val modelCheat = BlockModelDefinition.getNewModelDefinition.replaceInfoValue(BlockModelFields.field_240202_c_, ModelsResourceUtil.func_240222_a_(block, "_inf"))
    val variantBuilder = BlockStateVariantBuilder.func_240133_a_(FluidSourceBlock.CHEAT_MODE)
      .func_240143_a_(JBool.TRUE, modelCheat)
      .func_240143_a_(JBool.FALSE, modelNonCheat)
    ModelSerializerHelper(block.getRegistryName, FinishedVariantBlockState.func_240119_a_(block).func_240125_a_(variantBuilder))
  }

  def getCatModel(blockCAT: BlockCAT): ModelSerializerHelper = {
    val name = ModelsResourceUtil.func_240221_a_(blockCAT)
    val modelBase = () => BlockModelDefinition.getNewModelDefinition.replaceInfoValue(BlockModelFields.field_240202_c_, name)
    val variantBuilder = BlockStateVariantBuilder.func_240133_a_(BlockStateProperties.FACING)
      .func_240143_a_(Direction.NORTH, modelBase())
      .func_240143_a_(Direction.SOUTH, modelBase().replaceInfoValue(BlockModelFields.field_240201_b_, BlockModelFields.Rotation.R180))
      .func_240143_a_(Direction.EAST, modelBase().replaceInfoValue(BlockModelFields.field_240201_b_, BlockModelFields.Rotation.R90))
      .func_240143_a_(Direction.WEST, modelBase().replaceInfoValue(BlockModelFields.field_240201_b_, BlockModelFields.Rotation.R270))
      .func_240143_a_(Direction.UP, modelBase().replaceInfoValue(BlockModelFields.field_240200_a_, BlockModelFields.Rotation.R270))
      .func_240143_a_(Direction.DOWN, modelBase().replaceInfoValue(BlockModelFields.field_240200_a_, BlockModelFields.Rotation.R90))
    ModelSerializerHelper(blockCAT.getRegistryName, FinishedVariantBlockState.func_240119_a_(blockCAT).func_240125_a_(variantBuilder))
  }
}
