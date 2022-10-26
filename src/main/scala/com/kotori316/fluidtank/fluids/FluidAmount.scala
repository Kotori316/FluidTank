package com.kotori316.fluidtank.fluids

import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{BucketItem, ItemStack, Items}
import net.minecraft.world.level.material.{Fluid, Fluids}
import net.minecraftforge.fluids.{FluidAttributes, FluidStack, FluidUtil}
import net.minecraftforge.registries.{ForgeRegistries, IForgeRegistry}
import org.jetbrains.annotations.Nullable

import scala.util.chaining._

//
//class FluidAmount(@NotNull val fluid: Fluid, override val amount: Long, @NotNull override val nbt: Option[CompoundTag]) extends GenericAmount[Fluid, FluidAmount](fluid, amount, nbt) {
//  override type StackType = FluidStack
//
//  override def setAmount(newAmount: Long): FluidAmount = {
//    if (newAmount === this.amount) this // No need to create new instance.
//    else FluidAmount(fluid, newAmount, nbt)
//  }
//
//  override def empty: FluidAmount = FluidAmount.EMPTY
//
//  override def write(tag: CompoundTag): CompoundTag = {
//    import com.kotori316.fluidtank.fluids.FluidAmount._
//
//    val fluidNBT = new CompoundTag()
//    fluidNBT.putString(NBT_fluid, FluidAmount.registry.getKey(fluid).toString)
//    fluidNBT.putLong(NBT_amount, amount)
//    this.nbt.foreach(fluidNBT.put(NBT_tag, _))
//
//    tag merge fluidNBT
//  }
//
//  def fluidEqual(that: FluidAmount): Boolean = this.contentEqual(that)
//
//  def toStack: FluidStack = if (this == FluidAmount.EMPTY) FluidStack.EMPTY else new FluidStack(fluid, Utils.toInt(amount), nbt.orNull)
//}

object FluidAmount {
  final val NBT_fluid = "fluid"
  final val NBT_amount = "amount"
  final val NBT_tag = "tag"
  final val AMOUNT_BUCKET = FluidAttributes.BUCKET_VOLUME
  val EMPTY: FluidAmount = FluidAmount(Fluids.EMPTY, 0, None)
  val BUCKET_LAVA: FluidAmount = FluidAmount(Fluids.LAVA, AMOUNT_BUCKET, None)
  val BUCKET_WATER: FluidAmount = FluidAmount(Fluids.WATER, AMOUNT_BUCKET, None)

  def apply(fluid: Fluid, amount: Long, nbt: Option[CompoundTag]): FluidAmount = new FluidAmount(fluid, amount, nbt)

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

  def toStack(amount: FluidAmount): FluidStack = if (amount === FluidAmount.EMPTY) FluidStack.EMPTY else new FluidStack(amount.c, Utils.toInt(amount.amount), amount.nbt.orNull)

  def registry: IForgeRegistry[Fluid] = ForgeRegistries.FLUIDS
}
