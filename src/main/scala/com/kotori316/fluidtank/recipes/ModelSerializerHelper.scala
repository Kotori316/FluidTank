package com.kotori316.fluidtank.recipes

import java.lang.{Boolean => JBool}

import com.google.gson.JsonElement
import com.kotori316.fluidtank.blocks.{BlockCAT, BlockTank, FluidSourceBlock, TankPos}
import net.minecraft.core.Direction
import net.minecraft.data.models.blockstates.{BlockStateGenerator, MultiVariantGenerator, PropertyDispatch, Variant, VariantProperties}
import net.minecraft.data.models.model.ModelLocationUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.properties.BlockStateProperties

case class ModelSerializerHelper(location: ResourceLocation, stateSupplier: BlockStateGenerator) {
  def build: JsonElement = {
    stateSupplier.get()
  }
}

object ModelSerializerHelper {
  def getTankModel(block: BlockTank): ModelSerializerHelper = {
    val name = {
      val n = ModelLocationUtils.getModelLocation(block)
      new ResourceLocation(n.getNamespace,
        n.getPath
          .replace("invisible_", "")
          .replace("creative", "tank_creative")
      )
    }
    val model = Variant.variant.`with`(VariantProperties.MODEL, name)
    val variantBuilder = PropertyDispatch.property(TankPos.TANK_POS_PROPERTY)
      .select(TankPos.TOP, model)
      .select(TankPos.MIDDLE, model)
      .select(TankPos.BOTTOM, model)
      .select(TankPos.SINGLE, model)
    ModelSerializerHelper(block.registryName, MultiVariantGenerator.multiVariant(block).`with`(variantBuilder))
  }

  def getFluidSourceModel(block: FluidSourceBlock): ModelSerializerHelper = {
    val name = ModelLocationUtils.getModelLocation(block)
    val modelNonCheat = Variant.variant.`with`(VariantProperties.MODEL, name)
    val modelCheat = Variant.variant.`with`(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_inf"))
    val variantBuilder = PropertyDispatch.property(FluidSourceBlock.CHEAT_MODE)
      .select(JBool.TRUE, modelCheat)
      .select(JBool.FALSE, modelNonCheat)
    ModelSerializerHelper(block.registryName, MultiVariantGenerator.multiVariant(block).`with`(variantBuilder))
  }

  def getCatModel(blockCAT: BlockCAT): ModelSerializerHelper = {
    val name = ModelLocationUtils.getModelLocation(blockCAT)
    val modelBase = () => Variant.variant.`with`(VariantProperties.MODEL, name)
    val variantBuilder = PropertyDispatch.property(BlockStateProperties.FACING)
      .select(Direction.NORTH, modelBase())
      .select(Direction.SOUTH, modelBase().`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
      .select(Direction.EAST, modelBase().`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
      .select(Direction.WEST, modelBase().`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
      .select(Direction.UP, modelBase().`with`(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
      .select(Direction.DOWN, modelBase().`with`(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
    ModelSerializerHelper(blockCAT.registryName, MultiVariantGenerator.multiVariant(blockCAT).`with`(variantBuilder))
  }
}
