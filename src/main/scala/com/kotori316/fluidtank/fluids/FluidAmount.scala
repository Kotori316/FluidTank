package com.kotori316.fluidtank.fluids

import java.lang
import java.util.Optional

import cats._
import cats.implicits._
import com.kotori316.fluidtank.DynamicSerializable._
import com.kotori316.fluidtank._
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.mojang.serialization.{Codec, DataResult, DynamicOps, Dynamic => SerializeDynamic}
import javax.annotation.Nonnull
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.item.{BucketItem, ItemStack, Items}
import net.minecraft.nbt.{CompoundNBT, NBTDynamicOps}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.{FluidAttributes, FluidStack, FluidUtil}
import net.minecraftforge.registries.{ForgeRegistries, IForgeRegistry}

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import scala.util.chaining._

case class FluidAmount(@Nonnull fluid: Fluid, amount: Long, @Nonnull nbt: Option[CompoundNBT]) {
  def setAmount(newAmount: Long): FluidAmount = {
    if (newAmount === this.amount) this // No need to create new instance.
    else FluidAmount(fluid, newAmount, nbt)
  }

  def write(tag: CompoundNBT): CompoundNBT = {
    tag merge this.asInstanceOf[FluidAmount].toNBT.asInstanceOf[CompoundNBT]
  }

  def nonEmpty: Boolean = fluid != Fluids.EMPTY && amount > 0

  def isEmpty: Boolean = !nonEmpty

  def isGaseous: Boolean = fluid.getAttributes.isGaseous

  def getLocalizedName: String = String.valueOf(FluidAmount.registry.getKey(fluid))

  def +(that: FluidAmount): FluidAmount = {
    if (this.isEmpty) that
    else if (that.isEmpty) this
    else setAmount(this.amount + that.amount)
  }

  def -(that: FluidAmount): FluidAmount = {
    val subtracted = this.amount |-| that.amount
    (this.fluid === Fluids.EMPTY, that.fluid === Fluids.EMPTY) match {
      case (true, _) => that.copy(amount = subtracted)
      case (false, true) => this.copy(amount = subtracted)
      case (false, false) if this.fluid === that.fluid => this.copy(amount = subtracted)
      case _ /*(false, false)*/ => FluidAmount.EMPTY
    }
  }

  def *(times: Long): FluidAmount = times match {
    case 0 => this.setAmount(0)
    case 1 => this
    case _ => this.setAmount(this.amount * times)
  }

  def fluidEqual(that: FluidAmount): Boolean = this.fluid === that.fluid && this.nbt === that.nbt

  def toStack: FluidStack = if (this == FluidAmount.EMPTY) FluidStack.EMPTY else new FluidStack(fluid, Utils.toInt(amount), nbt.orNull)

  override def toString: String = FluidAmount.registry.getKey(fluid).getPath + "@" + amount + "mB" + nbt.fold("")(" " + _.toString)
}

object FluidAmount {
  final val NBT_fluid = "fluid"
  final val NBT_amount = "amount"
  final val NBT_tag = "tag"
  final val AMOUNT_BUCKET = FluidAttributes.BUCKET_VOLUME
  val EMPTY: FluidAmount = FluidAmount(Fluids.EMPTY, 0, None)
  val BUCKET_LAVA: FluidAmount = FluidAmount(Fluids.LAVA, AMOUNT_BUCKET, None)
  val BUCKET_WATER: FluidAmount = FluidAmount(Fluids.WATER, AMOUNT_BUCKET, None)
  val BUCKET_MILK: FluidAmount = FluidAmount(ModObjects.MILK_FLUID, AMOUNT_BUCKET, None)

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case Items.WATER_BUCKET => BUCKET_WATER
      case Items.MILK_BUCKET if !Utils.isVanillaMilkEnabled => BUCKET_MILK
      case bucket: BucketItem =>
        bucket.pipe(_.getFluid).pipe(FluidAmount(_, AMOUNT_BUCKET, None))
      case _ => FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).pipe(fromStack)
    }
  }

  def fromNBT(tag: CompoundNBT): FluidAmount = codecFA.parse(NBTDynamicOps.INSTANCE, tag).result().orElse(EMPTY)

  def fromStack(stack: FluidStack): FluidAmount = {
    val fluid = stack.getRawFluid
    if (fluid == null || fluid == Fluids.EMPTY) {
      FluidAmount.EMPTY
    } else {
      FluidAmount(fluid, stack.getAmount, Option(stack.getTag))
    }
  }

  def registry: IForgeRegistry[Fluid] = ForgeRegistries.FLUIDS

  implicit val showFA: Show[FluidAmount] = Show.fromToString
  implicit val hashFA: Hash[FluidAmount] = Hash.fromUniversalHashCode
  implicit val monoidFA: Monoid[FluidAmount] = new Monoid[FluidAmount] {
    override def empty: FluidAmount = FluidAmount.EMPTY

    override def combine(x: FluidAmount, y: FluidAmount): FluidAmount = x + y
  }

  val dynamicSerializableFA: DynamicSerializable[FluidAmount] = new DynamicSerializable[FluidAmount] {
    override def serialize[DataType](t: FluidAmount)(ops: DynamicOps[DataType]): SerializeDynamic[DataType] = {
      val map = Map[String, DataType](
        NBT_fluid -> ops.createString(FluidAmount.registry.getKey(t.fluid).toString),
        NBT_amount -> ops.createLong(t.amount)
      ) ++ t.nbt.map(c => NBT_tag -> SerializeDynamic.convert(NBTDynamicOps.INSTANCE, ops, c))

      val data = map.map { case (key, data) => ops.createString(key) -> data }
      new SerializeDynamic[DataType](ops, ops.createMap(data.asJava))
    }

    override def deserialize[DataType](d: SerializeDynamic[DataType]): FluidAmount = {
      val fA = for {
        name <- d.get(NBT_fluid).asString().result().toScala
        registryName = new ResourceLocation(name)
        fluid <- Option(registry.getValue(registryName))
        if fluid != EMPTY.fluid
        amount = d.get(NBT_amount).asLong(0L)
        nbt = d.getElement(NBT_tag).result().toScala
          .map(c => SerializeDynamic.convert(d.getOps, NBTDynamicOps.INSTANCE, c))
          .collect { case t: CompoundNBT if !t.isEmpty => t }
      } yield FluidAmount(fluid, amount, nbt)
      fA.getOrElse(EMPTY)
    }
  }

  implicit val codecFA: Codec[FluidAmount] = RecordCodecBuilder.create[FluidAmount] { inst =>
    inst.group(
      ResourceLocation.CODEC.comapFlatMap[Fluid](
        name => {
          val mappedName = Utils.mapMilkName(name)
          if (ForgeRegistries.FLUIDS.containsKey(mappedName)) DataResult.success(ForgeRegistries.FLUIDS.getValue(mappedName)) else DataResult.error(s"No fluid for $mappedName.")
        },
        fluid => fluid.getRegistryName
      ).fieldOf(NBT_fluid).forGetter(_.fluid),
      Codec.LONG.fieldOf(NBT_amount).forGetter(_.amount),
      CompoundNBT.CODEC.optionalFieldOf(NBT_tag).forGetter(_.nbt.toJava),
    ).apply(inst, inst.stable[com.mojang.datafixers.util.Function3[Fluid, lang.Long, Optional[CompoundNBT], FluidAmount]](
      (f, l, o) => FluidAmount(f, l, o.toScala)
    ))
  }

  implicit val dynamicSerializableFromCodecFA: DynamicSerializable[FluidAmount] =
    new DynamicSerializable.DynamicSerializableFromCodec(codecFA, EMPTY)

}
