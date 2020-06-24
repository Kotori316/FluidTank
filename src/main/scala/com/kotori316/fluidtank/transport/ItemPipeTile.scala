package com.kotori316.fluidtank.transport

import cats.Eval
import cats.data.OptionT
import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.HopperTileEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.wrapper.InvWrapper
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandler, ItemHandlerHelper}

final class ItemPipeTile extends PipeTileBase(ModObjects.ITEM_PIPE_TYPE) {
  private[this] val handler = new PipeItemHandler(this)
  private[this] var coolTime = ItemPipeTile.defaultCoolTime
  private[this] var repeated = 0

  override def tick(): Unit = if (!world.isRemote) {
    if (connection.isEmpty)
      makeConnection()
    import scala.jdk.CollectionConverters._
    coolTime -= 1
    if (coolTime <= 0) {
      val handlers = PipeBlock.FACING_TO_PROPERTY_MAP.asScala.toSeq.flatMap { case (direction, value) =>
        if (getBlockState.get(value).isInput) {
          val sourcePos = pos.offset(direction)
          val c = findItemHandler(getWorld, sourcePos, direction)
          c.toList
        } else {
          List.empty
        }
      }
      if (handlers.nonEmpty) {
        val outputPoses = connection.outputs(getPos)
        handlers.foreach { case (f, sourcePos) =>
          val func = (i: Int) => Option(f.extractItem(i, ItemPipeTile.transferItemCount, true)).filterNot(_.isEmpty).map(item => i -> item)
          for {
            p <- outputPoses
            (direction, pos) <- directions.map(f => f -> p.offset(f))
            if pos != sourcePos
            if getWorld.getBlockState(p).get(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput
            dest <- findItemHandler(getWorld, pos, direction).map { case (i, _) => i }.toList
            if f != dest
            (index, item) <- List.range(0, f.getSlots).collectFirst(Function.unlift(func))
          } {
            val transferSimulate = ItemHandlerHelper.insertItem(dest, item, true)
            if (!ItemStack.areItemsEqual(item, transferSimulate)) {
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
    Cap.asJava(
      CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.make[T](cap, handler)
        .filter(_ => side != null && getBlockState.get(PipeBlock.FACING_TO_PROPERTY_MAP.get(side)).is(PipeBlock.Connection.CONNECTED, PipeBlock.Connection.INPUT))
        .orElse(super.getCapability(cap, side).asScala)
    )
  }

  def findItemHandler(world: World, pos: BlockPos, direction: Direction): OptionT[Eval, (IItemHandler, BlockPos)] = {
    def tileCap: OptionT[Eval, (IItemHandler, BlockPos)] = for {
      t <- Cap.make(world.getTileEntity(pos))
      cap <- getCapFromCache(t, pos, direction.getOpposite, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
    } yield cap -> pos

    def entityCap: OptionT[Eval, (IItemHandler, BlockPos)] = for {
      inv <- Cap.make(HopperTileEntity.getInventoryAtPosition(world, pos))
    } yield new InvWrapper(inv) -> pos

    tileCap orElse entityCap
  }

  override def read(compound: CompoundNBT): Unit = {
    super.read(compound)
    this.coolTime = compound.getInt("coolTime")
    this.repeated = compound.getInt("repeated")
  }

  override def write(compound: CompoundNBT): CompoundNBT = {
    compound.putInt("coolTime", this.coolTime)
    compound.putInt("repeated", this.repeated)
    super.write(compound)
  }
}

object ItemPipeTile {
  final val defaultCoolTime = 50
  final val transferItemCount = 16

  def getCoolTime(repeated: Int): Int = {
    Math.max(1, defaultCoolTime - repeated / 2)
  }
}
