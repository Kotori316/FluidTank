package com.kotori316.fluidtank.tank

import alexiil.mc.lib.attributes._
import alexiil.mc.lib.attributes.fluid.FluidInvTankChangeListener
import alexiil.mc.lib.attributes.fluid.amount.{FluidAmount => BCAmount}
import alexiil.mc.lib.attributes.fluid.volume.{FluidKey, FluidVolume}
import com.kotori316.fluidtank.render.Box
import com.kotori316.fluidtank.tank.TileTank.getFluidHeight
import com.kotori316.fluidtank.{FluidAmount, ModTank}
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.util.Mth
import net.minecraft.world.Nameable
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState

import scala.math.Ordering.Implicits.infixOrderingOps

class TileTank(var tier: Tiers, t: BlockEntityType[_ <: TileTank], pos: BlockPos, state: BlockState)
  extends BlockEntity(t, pos, state)
    with Nameable
    with BlockEntityClientSerializable
    with AttributeProviderBlockEntity {
  self =>

  def this(pos: BlockPos, state: BlockState) = {
    this(Tiers.Invalid, ModTank.Entries.TANK_BLOCK_ENTITY_TYPE, pos, state)
  }

  def this(t: Tiers, pos: BlockPos, state: BlockState) = {
    this(t, ModTank.Entries.TANK_BLOCK_ENTITY_TYPE, pos, state)
  }

  val tank = new Tank
  var connection: Connection = Connection.invalid
  var loading = false
  var stackName: Component = _

  override def save(compound: CompoundTag): CompoundTag = {
    compound.put(TileTank.NBT_Tank, tank.writeToNBT(new CompoundTag))
    compound.put(TileTank.NBT_Tier, tier.toNBTTag)
    getStackName.foreach(t => compound.putString(TileTank.NBT_StackName, Component.Serializer.toJson(t)))
    super.save(compound)
  }

  def getBlockTag: CompoundTag = {
    val nbt = save(new CompoundTag)
    Seq("x", "y", "z", "id").foreach(nbt.remove)
    nbt
  }

  override def load(compound: CompoundTag): Unit = {
    super.load(compound)
    tank.readFromNBT(compound.getCompound(TileTank.NBT_Tank))
    tier = Tiers.fromNBT(compound.getCompound(TileTank.NBT_Tier))
    if (compound.contains(TileTank.NBT_StackName)) {
      stackName = Component.Serializer.fromJson(compound.getString(TileTank.NBT_StackName))
    }
    loading = true
  }

  def readNBTClient(compound: CompoundTag): Unit = {
    tank.readFromNBT(compound.getCompound(TileTank.NBT_Tank))
    tier = Tiers.fromNBT(compound.getCompound(TileTank.NBT_Tier))
    if (compound.contains(TileTank.NBT_StackName)) {
      stackName = Component.Serializer.fromJson(compound.getString(TileTank.NBT_StackName))
    } else {
      stackName = null
    }
  }

  /*
    override def onDataPacket(net: NetworkManager, pkt: SUpdateTileEntityPacket): Unit = handleUpdateTag(pkt.getNbtCompound)
  */
  private def sendPacket(): Unit = {
    if (hasLevel && !level.isClientSide) sync()
  }

  def hasContent: Boolean = tank.getFluidAmount > 0

  def getComparatorLevel: Int = connection.getComparatorLevel

  def onBlockPlacedBy(): Unit = {
    val downTank = Option(getLevel.getBlockEntity(getBlockPos.below())).collect { case t: TileTank => t }
    val upTank = Option(getLevel.getBlockEntity(getBlockPos.above())).collect { case t: TileTank => t }
    val newSeq = (downTank, upTank) match {
      case (Some(dT), Some(uT)) => (dT.connection.seq :+ this) ++ uT.connection.seq
      case (None, Some(uT)) => this +: uT.connection.seq
      case (Some(dT), None) => dT.connection.seq :+ this
      case (None, None) => Seq(this)
    }
    Connection.createAndInit(newSeq)
  }

  def onDestroy(): Unit = {
    this.connection.remove(this)
  }

  def getStackName: Option[Component] = Option(stackName)

  override def getName: Component = getStackName
    .getOrElse(new TextComponent(tier.toString + " Tank"))

  override def hasCustomName: Boolean = stackName != null

  override def getCustomName: Component = getStackName.orNull

  class Tank extends FluidAmount.Tank {
    var box: Box = _
    var fluid: FluidAmount = FluidAmount.EMPTY
    var capacity: Int = com.kotori316.fluidtank.Utils.toInt(tier.amount)
    var listeners = Map.empty[FluidInvTankChangeListener, ListenerRemovalToken]

    def onContentsChanged(previous: FluidAmount): Unit = {
      sendPacket()
      if (!loading)
        connection.updateNeighbors()
      if ((!hasLevel || self.getLevel.isClientSide) && capacity != 0) {
        if (getFluidAmount > 0) {
          val d = 1d / 16d
          val lowerBound = 0.001d
          val upperBound = 0.999d
          val (minY, maxY) = getFluidHeight(capacity.toDouble, getFluidAmount.toDouble, lowerBound, upperBound, 0.003, getFluid.isGaseous)
          box = Box(d * 8, minY, d * 8, d * 8, maxY, d * 8, d * 12 - 0.01, maxY - minY, d * 12 - 0.01, firstSide = true, endSide = true)
        } else {
          box = null
        }
      }
      listeners.keys.foreach(_.onChange(this, 0, previous.fluidVolume, fluid.fluidVolume))
    }

    def readFromNBT(nbt: CompoundTag): Tank = {
      capacity = nbt.getInt(TileTank.NBT_Capacity)
      val fluid = FluidAmount.fromNBT(nbt)
      setFluid(fluid)
      onContentsChanged(FluidAmount.EMPTY)
      this
    }

    def writeToNBT(nbt: CompoundTag): CompoundTag = {
      fluid.write(nbt)
      nbt.putInt(TileTank.NBT_Capacity, capacity)
      nbt
    }

    override def toString: String = {
      val fluid = getFluid
      if (fluid == null) "Tank : no fluid : Capacity = " + capacity
      else "Tank : " + fluid.getLocalizedName + " " + getFluidAmount + "mB : Capacity = " + capacity
    }

    def canFillFluidType(fluid: FluidAmount): Boolean = {
      val fluidType = connection.getFluidStack
      fluidType.isEmpty || fluidType.exists(fluid.fluidEqual)
    }

    // Util methods
    def getFluidAmount: Long = fluid.fluidVolume.amount().asLong(FluidAmount.AMOUNT_BUCKET)

    def getFluid: FluidAmount = fluid

    def setFluid(fluidAmount: FluidAmount): Unit = {
      if (fluidAmount == null) fluid = FluidAmount.EMPTY
      else fluid = fluidAmount
    }

    // Change content

    /**
     * @return Fluid that was accepted by the tank.
     */
    override def fill(fluidAmount: FluidAmount, doFill: Boolean, min: Long = 0): FluidAmount = {
      if (canFillFluidType(fluidAmount) && fluidAmount.nonEmpty) {
        val previous = fluid
        val newAmount = fluid.fluidVolume.amount() add fluidAmount.fluidVolume.amount()
        if (BCAmount.of(capacity, FluidAmount.AMOUNT_BUCKET) >= newAmount) {
          if (doFill) {
            fluid = fluidAmount.setAmount(newAmount)
            onContentsChanged(previous)
          }
          fluidAmount
        } else {
          val accept = BCAmount.of(capacity, FluidAmount.AMOUNT_BUCKET) sub fluid.fluidVolume.amount()
          if (accept >= BCAmount.of(min, FluidAmount.AMOUNT_BUCKET)) {
            if (doFill) {
              fluid = fluidAmount.setAmount(capacity)
              onContentsChanged(previous)
            }
            fluidAmount.setAmount(accept)
          } else {
            FluidAmount.EMPTY
          }
        }
      } else {
        FluidAmount.EMPTY
      }
    }

    /**
     * @param fluidAmount the fluid representing the kind and maximum amount to drain.
     *                    Empty Fluid means fluid type can be anything.
     * @param doDrain     false means simulating.
     * @param min         minimum amount to drain.
     * @return the fluid and amount that is (or will be) drained.
     */
    override def drain(fluidAmount: FluidAmount, doDrain: Boolean, min: Long = 0): FluidAmount = {
      if ((canFillFluidType(fluidAmount) || FluidAmount.EMPTY.fluidEqual(fluidAmount)) && fluid.nonEmpty) {
        val previous = fluid
        val drain = fluid.fluidVolume.amount() min fluidAmount.fluidVolume.amount()
        if (drain >= BCAmount.of(min, FluidAmount.AMOUNT_BUCKET)) {
          val newAmount = fluid.fluidVolume.amount() sub drain
          if (doDrain) {
            fluid = fluid.setAmount(newAmount)
            onContentsChanged(previous)
          }
          fluid.setAmount(drain)
        } else {
          FluidAmount.EMPTY
        }
      } else {
        FluidAmount.EMPTY
      }
    }

    override def isFluidValidForTank(tank: Int, fluid: FluidKey): Boolean = canFillFluidType(FluidAmount(fluid.withAmount(BCAmount.BUCKET)))

    override def setInvFluid(tank: Int, to: FluidVolume, simulation: Simulation): Boolean = {
      if (simulation.isAction) {
        setFluid(FluidAmount(to))
      }
      true
    }

    override def getInvFluid(tank: Int): FluidVolume = getFluid.fluidVolume

    override def addListener(listener: FluidInvTankChangeListener, removalToken: ListenerRemovalToken): ListenerToken = {
      this.listeners = this.listeners + ((listener, removalToken))
      /*return*/ () => {
        this.listeners.get(listener).filter(_ == removalToken) match {
          case Some(value) =>
            this.listeners = this.listeners.removed(listener)
            value.onListenerRemoved()
          case None =>
        }
      }
    }

    override def getMaxAmount_F(tank: Int): BCAmount = BCAmount.of(capacity, FluidAmount.AMOUNT_BUCKET)
  }

  override def fromClientTag(tag: CompoundTag): Unit = self.load(tag)

  override def toClientTag(tag: CompoundTag): CompoundTag = self.save(tag)

  override def addAllAttributes(to: AttributeList[_]): Unit = {
    to.offer(this.connection.handler)
  }
}

object TileTank {
  final val NBT_Tank = TankBlock.NBT_Tank
  final val NBT_Tier = TankBlock.NBT_Tier
  final val NBT_Capacity = TankBlock.NBT_Capacity
  final val NBT_BlockTag = TankBlock.NBT_BlockTag
  final val NBT_StackName = TankBlock.NBT_StackName
  final val bcId = "buildcraftcore"
  final val ae2id = "appliedenergistics2"

  def tick(world: Level, pos: BlockPos, state: BlockState, tile: TileTank): Unit = {
    if (tile.loading && !world.isClientSide) {
      world.getProfiler.push("Connection Loading")
      if (tile.connection == Connection.invalid)
        Connection.load(world, pos)
      tile.loading = false
      world.getProfiler.pop()
    }
  }

  /**
   *
   * @param capacity   the capacity of tank. Must not be 0.
   * @param amount     the amount in the tank, assumed to be grater than 0. (amount > 0)
   * @param lowerBound the minimum of fluid position.
   * @param upperBound the maximum of fluid position.
   * @param isGaseous  whether the fluid is gas or not.
   * @return (minY, maxY)
   */
  def getFluidHeight(capacity: Double, amount: Double, lowerBound: Double, upperBound: Double, minRatio: Double, isGaseous: Boolean): (Double, Double) = {
    val ratio = Mth.clamp(amount / capacity, minRatio, 1)
    val height = (upperBound - lowerBound) * ratio
    if (isGaseous) {
      (upperBound - height, upperBound)
    } else {
      (lowerBound, lowerBound + height)
    }
  }
}
