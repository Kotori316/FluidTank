package com.kotori316.fluidtank.recipes

import com.google.gson.JsonElement
import com.kotori316.fluidtank.blocks.{BlockTank, TankPos}
import com.kotori316.fluidtank.transport.PipeBlock
import net.minecraft.data.models.blockstates.{BlockStateGenerator, Condition, MultiPartGenerator, MultiVariantGenerator, PropertyDispatch, Variant, VariantProperties}
import net.minecraft.data.models.model.ModelLocationUtils
import net.minecraft.resources.ResourceLocation

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

  def getPipeModel(blockPipe: PipeBlock): ModelSerializerHelper = {
    val center = ModelLocationUtils.getModelLocation(blockPipe, "_" + "center")
    val side = ModelLocationUtils.getModelLocation(blockPipe, "_" + "side")
    val output = ModelLocationUtils.getModelLocation(blockPipe, "_" + "output")
    val input = ModelLocationUtils.getModelLocation(blockPipe, "_" + "input")
    getPipeModel(blockPipe, center, side, input, output)
  }

  private def getPipeModel(blockPipe: PipeBlock, center: ResourceLocation, side: ResourceLocation, input: ResourceLocation, output: ResourceLocation): ModelSerializerHelper = {
    import java.lang.{Boolean => JBool}
    val variant = (texture: ResourceLocation) => Variant.variant.`with`(VariantProperties.MODEL, texture).`with`(VariantProperties.UV_LOCK, JBool.TRUE)
    val generator = MultiPartGenerator.multiPart(blockPipe)
      .`with`(Variant.variant.`with`(VariantProperties.MODEL, center))
      // Connect
      .`with`(Condition.condition().term(PipeBlock.NORTH, PipeBlock.Connection.CONNECTED), variant(side))
      .`with`(Condition.condition().term(PipeBlock.EAST, PipeBlock.Connection.CONNECTED), variant(side).`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
      .`with`(Condition.condition().term(PipeBlock.SOUTH, PipeBlock.Connection.CONNECTED), variant(side).`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
      .`with`(Condition.condition().term(PipeBlock.WEST, PipeBlock.Connection.CONNECTED), variant(side).`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
      .`with`(Condition.condition().term(PipeBlock.UP, PipeBlock.Connection.CONNECTED), variant(side).`with`(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
      .`with`(Condition.condition().term(PipeBlock.DOWN, PipeBlock.Connection.CONNECTED), variant(side).`with`(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
      // Output
      .`with`(Condition.condition().term(PipeBlock.NORTH, PipeBlock.Connection.OUTPUT), variant(output))
      .`with`(Condition.condition().term(PipeBlock.EAST, PipeBlock.Connection.OUTPUT), variant(output).`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
      .`with`(Condition.condition().term(PipeBlock.SOUTH, PipeBlock.Connection.OUTPUT), variant(output).`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
      .`with`(Condition.condition().term(PipeBlock.WEST, PipeBlock.Connection.OUTPUT), variant(output).`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
      .`with`(Condition.condition().term(PipeBlock.UP, PipeBlock.Connection.OUTPUT), variant(output).`with`(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
      .`with`(Condition.condition().term(PipeBlock.DOWN, PipeBlock.Connection.OUTPUT), variant(output).`with`(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
      // Input
      .`with`(Condition.condition().term(PipeBlock.NORTH, PipeBlock.Connection.INPUT), variant(input))
      .`with`(Condition.condition().term(PipeBlock.EAST, PipeBlock.Connection.INPUT), variant(input).`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
      .`with`(Condition.condition().term(PipeBlock.SOUTH, PipeBlock.Connection.INPUT), variant(input).`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
      .`with`(Condition.condition().term(PipeBlock.WEST, PipeBlock.Connection.INPUT), variant(input).`with`(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
      .`with`(Condition.condition().term(PipeBlock.UP, PipeBlock.Connection.INPUT), variant(input).`with`(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
      .`with`(Condition.condition().term(PipeBlock.DOWN, PipeBlock.Connection.INPUT), variant(input).`with`(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
    ModelSerializerHelper(blockPipe.registryName, generator)
  }
}
