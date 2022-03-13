package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import com.kotori316.fluidtank.{Config, FluidTank}
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.common.crafting.conditions.{ICondition, IConditionSerializer}

object FluidTankConditions {
  final class ConfigCondition extends FluidTankConditions[FluidTankConditions.ConfigCondition](new ResourceLocation(FluidTank.modID, "config")) {
    override def test: Boolean = !Config.content.removeRecipe.get()
  }

  final class EasyCondition extends FluidTankConditions[FluidTankConditions.EasyCondition](new ResourceLocation(FluidTank.modID, "easy")) {
    override def test: Boolean = Config.content.easyRecipe.get()
  }

  final class Serializer[T <: FluidTankConditions[T]](recipe: FluidTankConditions[T]) extends IConditionSerializer[FluidTankConditions[T]] {
    override def write(json: JsonObject, value: FluidTankConditions[T]): Unit = {
    }

    override def read(json: JsonObject): FluidTankConditions[T] = recipe

    override def getID: ResourceLocation = recipe.getID
  }
}

abstract class FluidTankConditions[T <: FluidTankConditions[T]](location: ResourceLocation) extends ICondition {
  final val serializer = new FluidTankConditions.Serializer(this)

  override def getID: ResourceLocation = this.location

  override def test: Boolean
}
