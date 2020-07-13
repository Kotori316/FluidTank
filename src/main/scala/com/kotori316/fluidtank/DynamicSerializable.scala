package com.kotori316.fluidtank

import com.google.gson.JsonElement
import com.mojang.datafixers.util.Pair
import com.mojang.serialization._
import net.minecraft.nbt.{INBT, NBTDynamicOps}

trait DynamicSerializable[T] {
  def serialize[DataType](t: T)(ops: DynamicOps[DataType]): Dynamic[DataType]

  def deserialize[DataType](d: Dynamic[DataType]): T

  def deserializeFromNBT(nbt: INBT): T = deserialize(new Dynamic(NBTDynamicOps.INSTANCE, nbt))

  def asCodec: Codec[T] = new DynamicSerializable.DynamicSerializableCodec(this)
}

object DynamicSerializable {
  def apply[T](implicit ev: DynamicSerializable[T]): DynamicSerializable[T] = ev

  implicit class DynamicSerializeOps[T](private val t: T) extends AnyVal {
    def serialize[DataType](ops: DynamicOps[DataType])(implicit d: DynamicSerializable[T]): Dynamic[DataType] = d.serialize(t)(ops)

    def toNBT(implicit d: DynamicSerializable[T]): INBT = serialize(NBTDynamicOps.INSTANCE).getValue

    def toJson(implicit d: DynamicSerializable[T]): JsonElement = serialize(JsonOps.INSTANCE).getValue
  }

  class DynamicSerializableCodec[A](dynamicSerializable: DynamicSerializable[A]) extends Codec[A] {
    override def decode[T](ops: DynamicOps[T], input: T): DataResult[Pair[A, T]] = {
      val a = dynamicSerializable.deserialize(new Dynamic[T](ops, input))
      DataResult.success(Pair.of(a, ops.empty()))
    }

    override def encode[T](input: A, ops: DynamicOps[T], prefix: T): DataResult[T] = {
      ops.mergeToPrimitive(prefix, dynamicSerializable.serialize(input)(ops).getValue)
    }
  }

  class DynamicSerializableFromCodec[T](private val codec: Codec[T], private val empty: T) extends DynamicSerializable[T] {
    override def serialize[DataType](t: T)(ops: DynamicOps[DataType]): Dynamic[DataType] =
      codec.encodeStart(ops, t).map[Dynamic[DataType]](r => new Dynamic(ops, r)).result().get()

    override def deserialize[DataType](d: Dynamic[DataType]): T = d.read(codec).result().orElse(empty)

    override def asCodec: Codec[T] = codec
  }

}
