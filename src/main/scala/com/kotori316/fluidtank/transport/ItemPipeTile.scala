package com.kotori316.fluidtank.transport

import cats.Eval
import cats.data.OptionT
import cats.implicits._
import com.kotori316.fluidtank._
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.HopperTileEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.wrapper.InvWrapper
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandler, ItemHandlerHelper}

class ItemPipeTile extends PipeTileBase(ModObjects.ITEM_PIPE_TYPE) {
  val handler = new PipeItemHandler(this)

  override def tick(): Unit = if (!world.isRemote) {
    if (connection.isEmpty)
      makeConnection()
    import scala.jdk.CollectionConverters._
    PipeBlock.FACING_TO_PROPERTY_MAP.asScala.toSeq.flatMap { case (direction, value) =>
      if (getBlockState.get(value).isInput) {
        val sourcePos = pos.offset(direction)
        val c = findItemHandler(getWorld, sourcePos, direction)
        c.toList
      } else {
        List.empty
      }
    }.foreach { case (f, sourcePos) =>
      for {
        p <- connection.outputs
        (direction, pos) <- directions.map(f => f -> p.offset(f))
        if pos != sourcePos
        if getWorld.getBlockState(p).get(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput
        dest <- findItemHandler(getWorld, pos, direction).map { case (i, _) => i }.toList
        if f != dest
        index <- List.range(0, f.getSlots)
        item <- f.extractItem(index, Int.MaxValue, true).pure[List] if !item.isEmpty
      } {
        val transferSimulate = ItemHandlerHelper.insertItem(dest, item, true)
        if (!ItemStack.areItemsEqual(item, transferSimulate)) {
          val result = ItemHandlerHelper.insertItem(dest, item, false)
          f.extractItem(index, item.getCount - result.getCount, false)
        }
      }
    }
  }

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    Cap.asJava(
      Cap.make(handler.asInstanceOf[T])
        .filter(_ => cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        .filter(_ => side != null && getBlockState.get(PipeBlock.FACING_TO_PROPERTY_MAP.get(side)).is(PipeBlock.Connection.CONNECTED, PipeBlock.Connection.INPUT))
        .orElse(super.getCapability(cap, side).asScala)
    )
  }

  def findItemHandler(world: World, pos: BlockPos, direction: Direction): OptionT[Eval, (IItemHandler, BlockPos)] = {
    def tileCap: OptionT[Eval, (IItemHandler, BlockPos)] = for {
      t <- OptionT.fromOption[Eval](Option(world.getTileEntity(pos)))
      cap <- t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite).asScala
    } yield cap -> pos

    def entityCap: OptionT[Eval, (IItemHandler, BlockPos)] = for {
      inv <- OptionT(Eval.later(Option(HopperTileEntity.getInventoryAtPosition(world, pos))))
    } yield new InvWrapper(inv) -> pos

    tileCap orElse entityCap
  }
}
