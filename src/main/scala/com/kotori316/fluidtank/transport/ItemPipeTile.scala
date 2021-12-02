package com.kotori316.fluidtank.transport

import cats.Eval
import cats.data.OptionT
import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.HopperBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.wrapper.InvWrapper
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandler, ItemHandlerHelper}

final class ItemPipeTile(p: BlockPos, s: BlockState) extends PipeTileBase(ModObjects.ITEM_PIPE_TYPE, p, s) {
  private[this] val handler = new PipeItemHandler(this)
  private[this] var coolTime = ItemPipeTile.defaultCoolTime
  private[this] var repeated = 0

  def tick(): Unit = if (!level.isClientSide) {
    if (connection.isEmpty)
      makeConnection()
    import scala.jdk.CollectionConverters._
    coolTime -= 1
    if (coolTime <= 0) {
      val handlers = PipeBlock.FACING_TO_PROPERTY_MAP.asScala.flatMap { case (direction, value) =>
        if (getBlockState.getValue(value).isInput) {
          val sourcePos = getBlockPos.offset(direction)
          val c = findItemHandler(getLevel, sourcePos, direction)
          c.toList
        } else {
          List.empty
        }
      }
      if (handlers.nonEmpty) {
        val outputPoses = connection.outputs(getBlockPos)
        handlers.foreachEntry { (f, sourcePos) =>
          val func = (i: Int) => Option(f.extractItem(i, ItemPipeTile.transferItemCount, true)).filterNot(_.isEmpty).map(item => i -> item)
          for {
            p <- outputPoses
            (direction, pos) <- directions.map(f => f -> p.offset(f))
            if pos != sourcePos
            if getLevel.getBlockState(p).getValue(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput
            dest <- findItemHandler(getLevel, pos, direction).map { case (i, _) => i }.toList
            if f != dest
            (index, item) <- List.range(0, f.getSlots).collectFirst(Function.unlift(func))
          } {
            val transferSimulate = ItemHandlerHelper.insertItem(dest, item, true)
            if (!ItemStack.isSame(item, transferSimulate)) {
              val result = ItemHandlerHelper.insertItem(dest, item, false)
              f.extractItem(index, item.getCount - result.getCount, false)
            }
          }
        }
      }
      repeated = if (handlers.isEmpty) Math.max(repeated - 4, 0) else repeated + 1
      coolTime = ItemPipeTile.getCoolTime(repeated)
    }

  }

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      if (side != null &&
        (!hasLevel || getBlockState.getValue(PipeBlock.FACING_TO_PROPERTY_MAP.get(side)).is(PipeBlock.Connection.CONNECTED, PipeBlock.Connection.INPUT))) {
        LazyOptional.of(() => handler.asInstanceOf[T])
      } else {
        LazyOptional.empty()
      }
    } else {
      super.getCapability(cap, side)
    }
  }

  def findItemHandler(level: Level, pos: BlockPos, direction: Direction): OptionT[Eval, (IItemHandler, BlockPos)] = {
    def tileCap: OptionT[Eval, (IItemHandler, BlockPos)] = for {
      t <- Cap.make(level.getBlockEntity(pos))
      cap <- getCapFromCache(t, pos, direction.getOpposite, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
    } yield cap -> pos

    def entityCap: OptionT[Eval, (IItemHandler, BlockPos)] = for {
      inv <- Cap.make(HopperBlockEntity.getContainerAt(level, pos))
    } yield new InvWrapper(inv) -> pos

    tileCap orElse entityCap
  }

  override def load(compound: CompoundTag): Unit = {
    super.load(compound)
    this.coolTime = compound.getInt("coolTime")
    this.repeated = compound.getInt("repeated")
  }

  override def saveAdditional(compound: CompoundTag): Unit = {
    compound.putInt("coolTime", this.coolTime)
    compound.putInt("repeated", this.repeated)
    super.saveAdditional(compound)
  }
}

object ItemPipeTile {
  final val defaultCoolTime = 50
  final val transferItemCount = 16

  def getCoolTime(repeated: Int): Int = {
    Math.max(1, defaultCoolTime - repeated / 2)
  }
}
