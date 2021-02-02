package com.kotori316.fluidtank

import com.google.gson.JsonElement
import com.mojang.datafixers.util.Pair
import com.mojang.serialization._
import net.minecraft.nbt.{CompoundNBT, INBT, NBTDynamicOps}

trait DynamicSerializable[T] {
  def serialize[DataType](t: T)(ops: DynamicOps[DataType]): Dynamic[DataType]

  def deserialize[DataType](d: Dynamic[DataType]): T

  def deserializeFromNBT(nbt: INBT): T = deserialize(new Dynamic(NBTDynamicOps.INSTANCE, nbt))

  def asCodec: Codec[T] = new DynamicSerializable.DynamicSerializableCodec(this)
}

object DynamicSerializable {
  private final val MARKER = ModObjects.MARKER_DynamicSerializable

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
      codec.encodeStart(ops, t).map[Dynamic[DataType]](r => new Dynamic(ops, r)).resultOrPartial(FluidTank.LOGGER.error(MARKER, _)).get()

    override def deserialize[DataType](d: Dynamic[DataType]): T = d.read(codec).resultOrPartial(FluidTank.LOGGER.error(MARKER, _)).orElse(empty)

    override def asCodec: Codec[T] = codec
  }

  implicit object NBTDynamicSerialize extends DynamicSerializable[CompoundNBT] {
    override def deserializeFromNBT(nbt: INBT): CompoundNBT = super.deserializeFromNBT(nbt)

    override def serialize[DataType](t: CompoundNBT)(ops: DynamicOps[DataType]): Dynamic[DataType] =
      new Dynamic(NBTDynamicOps.INSTANCE, t).convert(ops)

    override def deserialize[DataType](d: Dynamic[DataType]): CompoundNBT = d.convert(NBTDynamicOps.INSTANCE).getValue.asInstanceOf[CompoundNBT]
  }

}
