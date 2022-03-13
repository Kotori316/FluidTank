package com.kotori316.fluidtank.recipes

import com.google.gson.JsonObject
import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.GsonHelper
import net.minecraftforge.common.crafting.conditions.{ICondition, IConditionSerializer, TagEmptyCondition}

object TagCondition {
  val LOCATION = new ResourceLocation(FluidTank.modID, "tag")
  val SERIALIZER = new TagCondition.Serializer

  class Serializer extends IConditionSerializer[TagCondition] {
    override def write(json: JsonObject, value: TagCondition): Unit = json.addProperty("tag", value.tagName.toString)

    override def read(json: JsonObject): TagCondition = new TagCondition(GsonHelper.getAsString(json, "tag"))

    override def getID: ResourceLocation = TagCondition.LOCATION
  }
}

case class TagCondition(tagName: ResourceLocation) extends ICondition {
  private final val condition = new TagEmptyCondition(tagName)

  def this(tagName: String) = {
    this(new ResourceLocation(tagName))
  }

  override def getID: ResourceLocation = TagCondition.LOCATION

  override def test: Boolean = {
    // return !condition.test();
    // FIXME the tag is loaded AFTER recipe loading so tags are not available in this context.
    Tier.values.find(_.tagName == tagName.toString).forall(_.isAvailableInVanilla)
  }
}
