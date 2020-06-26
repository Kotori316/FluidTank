package com.kotori316.fluidtank

import com.google.gson.JsonElement
import com.mojang.serialization.{DynamicOps, JsonOps, Dynamic}
import net.minecraft.nbt.{INBT, NBTDynamicOps}

trait DynamicSerializable[T] {
  def serialize[DataType](t: T)(ops: DynamicOps[DataType]): Dynamic[DataType]

  def deserialize[DataType](d: Dynamic[DataType]): T

  def deserializeFromNBT(nbt: INBT): T = deserialize(new Dynamic(NBTDynamicOps.INSTANCE, nbt))
}

object DynamicSerializable {
  def apply[T](implicit ev: DynamicSerializable[T]): DynamicSerializable[T] = ev

  implicit class DynamicSerializeOps[T](private val t: T) extends AnyVal {
    def serialize[DataType](ops: DynamicOps[DataType])(implicit d: DynamicSerializable[T]): Dynamic[DataType] = DynamicSerializable[T].serialize(t)(ops)

    def toNBT(implicit d: DynamicSerializable[T]): INBT = serialize(NBTDynamicOps.INSTANCE).getValue

    def toJson(implicit d: DynamicSerializable[T]): JsonElement = serialize(JsonOps.INSTANCE).getValue
  }

}
