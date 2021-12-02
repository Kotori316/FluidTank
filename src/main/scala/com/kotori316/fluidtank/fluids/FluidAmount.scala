package com.kotori316.fluidtank.fluids

import cats._
import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{BucketItem, ItemStack, Items}
import net.minecraft.world.level.material.{Fluid, Fluids}
import net.minecraftforge.fluids.{FluidAttributes, FluidStack, FluidUtil}
import net.minecraftforge.registries.{ForgeRegistries, IForgeRegistry}
import org.jetbrains.annotations.{NotNull, Nullable}

import scala.util.chaining._

case class FluidAmount(@NotNull fluid: Fluid, amount: Long, @NotNull nbt: Option[CompoundTag]) {
  def setAmount(newAmount: Long): FluidAmount = {
    if (newAmount === this.amount) this // No need to create new instance.
    else FluidAmount(fluid, newAmount, nbt)
  }

  def write(tag: CompoundTag): CompoundTag = {
    import com.kotori316.fluidtank.fluids.FluidAmount._

    val fluidNBT = new CompoundTag()
    fluidNBT.putString(NBT_fluid, FluidAmount.registry.getKey(fluid).toString)
    fluidNBT.putLong(NBT_amount, amount)
    this.nbt.foreach(fluidNBT.put(NBT_tag, _))

    tag merge fluidNBT
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

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case Items.WATER_BUCKET => BUCKET_WATER
      case bucket: BucketItem =>
        bucket.pipe(_.getFluid).pipe(FluidAmount(_, AMOUNT_BUCKET, None))
      case _ => FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).pipe(fromStack)
    }
  }

  def fromNBT(@Nullable tag: CompoundTag): FluidAmount = {
    if (tag == null || tag.isEmpty) return FluidAmount.EMPTY
    val name = new ResourceLocation(tag.getString(NBT_fluid))
    val amount = tag.getLong(NBT_amount)
    val nbt = if (tag.contains(NBT_tag)) Option(tag.getCompound(NBT_tag)) else None
    val fluid = ForgeRegistries.FLUIDS.getValue(name)
    FluidAmount(fluid, amount, nbt)
  }

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

}
