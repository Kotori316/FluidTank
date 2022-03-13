package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.fluids.{FluidAmount, FluidKey}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.Component
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.{Inventory, Player}
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler, IFluidHandlerItem}
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandlerModifiable}

import scala.collection.immutable
import scala.jdk.OptionConverters.RichOptional

class CATTile(pos: BlockPos, state: BlockState) extends BlockEntity(ModObjects.CAT_TYPE, pos, state) with MenuProvider {
  // The direction of FACING is facing to you, people expect to use itemBlock targeting an inventory so the chest exists on the opposite side of FACING.

  // Client only
  var fluidCache: Seq[FluidAmount] = Seq.empty

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    val direction = getBlockState.getValue(BlockStateProperties.FACING)
    if (side != direction && cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
      getFluidHandler(direction).map(t => LazyOptional.of(() => t)).getOrElse(LazyOptional.empty()).cast[T]()
    else
      super.getCapability(cap, side)
  }

  def getFluidHandler(direction: Direction): Option[CATTile.FluidHandlerWrapper] = {
    assert(level != null)
    val entity = level.getBlockEntity(getBlockPos.relative(direction))
    if (entity == null) Option.empty
    else
      entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite)
        .resolve.toScala
        .collect { case i: IItemHandlerModifiable => new CATTile.FluidHandlerWrapper(i) }
  }

  override def getDisplayName: Component = getBlockState.getBlock.getName

  override def createMenu(id: Int, inv: Inventory, player: Player) = new CATContainer(id, player, getBlockPos)

  def fluidAmountList: Seq[FluidAmount] = {
    val direction = getBlockState.getValue(BlockStateProperties.FACING)
    val opt = getFluidHandler(direction)
    opt.toSeq.flatMap(_.fluidList)
  }
}

object CATTile {
  class FluidHandlerWrapper(val inventory: IItemHandlerModifiable) extends IFluidHandler {
    def getFluidHandler(tank: Int): Option[IFluidHandlerItem] = {
      val stackInSlot = inventory.getStackInSlot(tank)
      if (stackInSlot.isEmpty) Option.empty
      else net.minecraftforge.fluids.FluidUtil.getFluidHandler(stackInSlot).resolve().toScala
    }

    override def getTanks: Int = inventory.getSlots

    override def getFluidInTank(tank: Int): FluidStack = getFluidHandler(tank).map(h => h.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE)).getOrElse(FluidStack.EMPTY)

    override def getTankCapacity(tank: Int): Int = getFluidHandler(tank).map(h => h.getTankCapacity(0)).getOrElse(0)

    override def isFluidValid(tank: Int, stack: FluidStack): Boolean = getFluidHandler(tank).exists(h => h.isFluidValid(0, stack))

    override def fill(resource: FluidStack, action: IFluidHandler.FluidAction): Int = {
      if (resource == null || resource.isEmpty) return 0
      val toFill = resource.copy
      var i = 0
      while (i < getTanks && !toFill.isEmpty) {
        getFluidHandler(i).foreach { handler =>
          val filled = handler.fill(toFill, action)
          toFill.setAmount(toFill.getAmount - filled)
          if (action.execute())
            inventory.setStackInSlot(i, handler.getContainer)
        }
        i += 1
      }

      resource.getAmount - toFill.getAmount
    }

    override def drain(resource: FluidStack, action: IFluidHandler.FluidAction): FluidStack = {
      if (resource == null || resource.isEmpty) return FluidStack.EMPTY
      val toDrain = resource.copy
      var i = 0
      while (i < getTanks && !toDrain.isEmpty) {
        getFluidHandler(i).foreach { handler =>
          val drained = handler.drain(toDrain, action)
          toDrain.setAmount(toDrain.getAmount - drained.getAmount)
          if (action.execute())
            inventory.setStackInSlot(i, handler.getContainer)
        }
        i += 1
      }

      new FluidStack(resource, resource.getAmount - toDrain.getAmount)
    }

    override def drain(maxDrain: Int, action: IFluidHandler.FluidAction): FluidStack = {
      if (maxDrain <= 0) {
        FluidStack.EMPTY
      } else {
        val first = getFluidAmountStream.map(_.toStack).headOption
        first.map(s => this.drain(new FluidStack(s, Math.min(maxDrain, s.getAmount)), action)).getOrElse(FluidStack.EMPTY)
      }
    }

    def fluidList: Seq[FluidAmount] = getFluidAmountStream.toSeq

    def getFluidAmountStream: immutable.Iterable[FluidAmount] = {
      (0 until this.getTanks).map(this.getFluidInTank)
        .groupMapReduce(FluidKey.from)(_.getAmount.toLong)(_ + _)
        .map { case (key, l) => key toAmount l }
        .filter(_.nonEmpty)
    }
  }
}
