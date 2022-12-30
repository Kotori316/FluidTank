package com.kotori316.fluidtank.fluids

import cats.implicits.catsSyntaxEq
import cats.{Hash, Monoid, Show}
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{ItemStack, Items}
import net.minecraft.world.level.material.{Fluid, Fluids}
import org.jetbrains.annotations.{NotNull, Nullable}

case class FluidAmount(@NotNull fluid: Fluid, fabricAmount: FabricAmount, @NotNull nbt: Option[CompoundTag]) {
  def amount: Long = fabricAmount.toForge

  def setAmount(newAmount: Long): FluidAmount = {
    if (newAmount === this.amount) this // No need to create new instance.
    else FluidAmount(fluid, FabricAmount.fromForge(newAmount), nbt)
  }

  def setAmountF(newAmount: FabricAmount): FluidAmount = {
    if (newAmount === this.fabricAmount) this // No need to create new instance.
    else FluidAmount(fluid, newAmount, nbt)
  }

  def write(tag: CompoundTag): CompoundTag = {
    import com.kotori316.fluidtank.fluids.FluidAmount._

    val fluidNBT = new CompoundTag()
    fluidNBT.putString(NBT_fluid, FluidAmount.registry.getKey(fluid).toString)
    fluidNBT.putLong(NBT_fabric_amount, fabricAmount.amount)
    this.nbt.foreach(fluidNBT.put(NBT_tag, _))

    tag merge fluidNBT
  }

  def nonEmpty: Boolean = fluid != Fluids.EMPTY && amount > 0

  def isEmpty: Boolean = !nonEmpty

  def isGaseous: Boolean = VariantUtil.isGaseous(this)

  def getLocalizedName: String = String.valueOf(FluidAmount.registry.getKey(fluid))

  def getDisplayName: Component = VariantUtil.getName(this)

  def +(that: FluidAmount): FluidAmount = {
    if (this.isEmpty) that
    else if (that.isEmpty) this
    else copy(fabricAmount = this.fabricAmount + that.fabricAmount)
  }

  def -(that: FluidAmount): FluidAmount = {
    val subtracted = this.fabricAmount - that.fabricAmount
    (this.fluid == Fluids.EMPTY, that.fluid == Fluids.EMPTY) match {
      case (true, _) => that.copy(fabricAmount = subtracted)
      case (false, true) => this.copy(fabricAmount = subtracted)
      case (false, false) if this.fluid == that.fluid => this.copy(fabricAmount = subtracted)
      case _ /*(false, false)*/ => FluidAmount.EMPTY
    }
  }

  def *(times: Long): FluidAmount = times match {
    case 0 => this.setAmount(0)
    case 1 => this
    case _ => this.setAmount(this.amount * times)
  }

  def fluidEqual(that: FluidAmount): Boolean = this.fluid == that.fluid && this.nbt == that.nbt

  override def toString: String = FluidAmount.registry.getKey(fluid).getPath + "@" + amount + "mB" + nbt.fold("")(" " + _.toString)

}

object FluidAmount {
  final val NBT_fluid = "fluid"
  final val NBT_amount = "amount"
  final val NBT_fabric_amount = "fabric_amount"
  final val NBT_tag = "tag"
  final val AMOUNT_BUCKET = 1000L
  val EMPTY: FluidAmount = FluidAmount(Fluids.EMPTY, FabricAmount(0), None)
  val BUCKET_LAVA: FluidAmount = FluidAmount(Fluids.LAVA, FabricAmount.BUCKET, None)
  val BUCKET_WATER: FluidAmount = FluidAmount(Fluids.WATER, FabricAmount.BUCKET, None)

  def fromItem(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case Items.LAVA_BUCKET => BUCKET_LAVA
      case Items.WATER_BUCKET => BUCKET_WATER
      case _ => VariantUtil.getFluidInItem(stack)
    }
  }

  def fromNBT(@Nullable tag: CompoundTag): FluidAmount = {
    if (tag == null || tag.isEmpty) return FluidAmount.EMPTY
    val name = new ResourceLocation(tag.getString(NBT_fluid))
    val amount = if (tag.contains(NBT_amount)) FabricAmount.fromForge(tag.getLong(NBT_amount))
    else FabricAmount(tag.getLong(NBT_fabric_amount))
    val nbt = if (tag.contains(NBT_tag)) Option(tag.getCompound(NBT_tag)) else None
    val fluid = registry.get(name)
    FluidAmount(fluid, amount, nbt)
  }

  def registry: Registry[Fluid] = BuiltInRegistries.FLUID

  implicit val showFA: Show[FluidAmount] = Show.fromToString
  implicit val hashFA: Hash[FluidAmount] = Hash.fromUniversalHashCode
  implicit val monoidFA: Monoid[FluidAmount] = new Monoid[FluidAmount] {
    override def empty: FluidAmount = FluidAmount.EMPTY

    override def combine(x: FluidAmount, y: FluidAmount): FluidAmount = x + y
  }

}
