package com.kotori316.fluidtank

import cats._
import cats.implicits._
import com.kotori316.fluidtank.DynamicSerializable._
import com.mojang.datafixers
import com.mojang.datafixers.types.DynamicOps
import javax.annotation.Nonnull
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.item.{BucketItem, ItemStack, Items}
import net.minecraft.nbt.{CompoundNBT, NBTDynamicOps}
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.{FluidAttributes, FluidStack, FluidUtil}
import net.minecraftforge.registries.{ForgeRegistries, IForgeRegistry}

import scala.jdk.javaapi.CollectionConverters

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
    if (fluid == Fluids.EMPTY) that
    else setAmount(this.amount + that.amount)
  }

  def -(that: FluidAmount): FluidAmount = setAmount(this.amount - that.amount)

  def fluidEqual(that: FluidAmount): Boolean = this.fluid === that.fluid && this.nbt === that.nbt

  def toStack: FluidStack = if (this == FluidAmount.EMPTY) FluidStack.EMPTY else new FluidStack(fluid, Utils.toInt(amount))

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
      case Items.MILK_BUCKET => BUCKET_MILK
      case bucket: BucketItem =>
        bucket.pure[Id].map(_.getFluid).map(FluidAmount(_, AMOUNT_BUCKET, None))
      case _ => FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY).pure[Id].map(fromStack)
    }
  }

  def fromNBT(tag: CompoundNBT): FluidAmount = dynamicSerializableFA.deserializeFromNBT(tag)

  def fromStack(stack: FluidStack): FluidAmount = {
    val fluid = stack.getRawFluid
    if (fluid == null || fluid == Fluids.EMPTY) {
      FluidAmount.EMPTY
    } else {
      FluidAmount(fluid, stack.getAmount, Option(stack.getTag))
    }
  }

  def registry: IForgeRegistry[Fluid] = ForgeRegistries.FLUIDS

  trait Tank extends IFluidHandler {
    /**
     * @return Fluid that was accepted by the tank.
     */
    def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Int = 0): FluidAmount

    /**
     * @param fluidAmount the fluid representing the kind and maximum amount to drain.
     *                    Empty Fluid means fluid type can be anything.
     * @param doDrain     false means simulating.
     * @param min         minimum amount to drain.
     * @return the fluid and amount that is (or will be) drained.
     */
    def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Int = 0): FluidAmount

    override def getTanks = 1

    override def isFluidValid(tank: Int, stack: FluidStack): Boolean = true

    override def fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int = Utils.toInt(fill(FluidAmount.fromStack(resource), action.execute()).amount)

    override def drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = drain(fromStack(resource), action.execute()).toStack

    override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = drain(fromStack(getFluidInTank(0)).setAmount(maxDrain), action.execute()).toStack
  }

  def b2a(doAction: Boolean): IFluidHandler.FluidAction =
    if (doAction) IFluidHandler.FluidAction.EXECUTE
    else IFluidHandler.FluidAction.SIMULATE

  implicit val showFA: Show[FluidAmount] = Show.fromToString
  implicit val hashFA: Hash[FluidAmount] = Hash.fromUniversalHashCode

  implicit val dynamicSerializableFA: DynamicSerializable[FluidAmount] = new DynamicSerializable[FluidAmount] {
    override def serialize[DataType](t: FluidAmount)(ops: DynamicOps[DataType]): datafixers.Dynamic[DataType] = {
      val map = Map[String, DataType](
        NBT_fluid -> ops.createString(FluidAmount.registry.getKey(t.fluid).toString),
        NBT_amount -> ops.createLong(t.amount)
      ) ++ t.nbt.map(c => NBT_tag -> datafixers.Dynamic.convert(NBTDynamicOps.INSTANCE, ops, c))

      val data = map.map { case (key, data) => ops.createString(key) -> data }
      new datafixers.Dynamic[DataType](ops, ops.createMap(CollectionConverters.asJava(data)))
    }

    override def deserialize[DataType](d: datafixers.Dynamic[DataType]): FluidAmount = {
      val fluidName = d.get(NBT_fluid).asString().asScala
        .map(s => new ResourceLocation(s))
        .getOrElse(Fluids.EMPTY.getRegistryName)
      val fluid = registry.getValue(fluidName)
      if (fluid == null || fluid == EMPTY.fluid) {
        EMPTY
      } else {
        val amount = d.get(NBT_amount).asLong(0L)
        val nbt = d.getElement(NBT_tag).scalaMap(c => datafixers.Dynamic.convert(d.getOps, NBTDynamicOps.INSTANCE, c))
          .collect { case t: CompoundNBT if !t.isEmpty => t }
        FluidAmount(fluid, amount, nbt)
      }
    }
  }
}
