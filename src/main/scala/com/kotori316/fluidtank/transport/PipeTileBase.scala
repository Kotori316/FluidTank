package com.kotori316.fluidtank.transport

import cats.implicits._
import com.kotori316.fluidtank.network.{PacketHandler, TileMessage}
import com.kotori316.fluidtank.transport.NeighborInstance._
import com.kotori316.fluidtank.{FluidTank, _}
import javax.annotation.Nonnull
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional

import scala.ref.WeakReference

abstract class PipeTileBase(t: BlockEntityType[_ <: PipeTileBase], p: BlockPos, s: BlockState) extends BlockEntity(t, p, s) {
  var connection: PipeConnection2[BlockPos] = getEmptyConnection
  private[this] final var color = Int.unbox(Config.content.pipeColor.get())
  private[this] val weakValueMap = scala.collection.mutable.Map.empty[(BlockPos, Direction, Capability[_]), WeakReference[LazyOptional[_]]]

  private def getEmptyConnection: PipeConnection2[BlockPos] = PipeConnection2.empty { p =>
    val state = getLevel.getBlockState(p)
    if (PipeBlock.FACING_TO_PROPERTY_MAP.values().stream().allMatch(pr => state.hasProperty(pr))) {
      PipeBlock.FACING_TO_PROPERTY_MAP.values().stream().anyMatch(pr => state.getValue(pr).isOutput)
    } else {
      false
    }
  }

  def makeConnection(): Unit = {
    val checked = scala.collection.mutable.Set.empty[BlockPos]

    def makePosList(start: BlockPos): List[BlockPos] = {
      for {
        d <- directions
        pos = start.offset(d)
        if checked.add(pos) // True means it's first time to check the pos. False means the pos already checked.
        state = getLevel.getBlockState(pos)
        if state.getBlock == this.getBlockState.getBlock
        if state.getValue(PipeBlock.FACING_TO_PROPERTY_MAP.get(d.getOpposite)) == PipeBlock.Connection.CONNECTED
        pos2 <- pos :: makePosList(pos)
      } yield pos2
    }

    val poses: List[BlockPos] = makePosList(getBlockPos)
    val lastConnection = if (poses.isEmpty) {
      PipeConnection2.add(getEmptyConnection, getBlockPos)
    } else {
      poses.foldl(getEmptyConnection)(PipeConnection2.add)
    }
    applyToAllPipe(tile => tile.connection = lastConnection, c = lastConnection)
    if (Utils.isInDev)
      FluidTank.LOGGER.debug(ModObjects.MARKER_PipeTileBase, "PipeConnection2 by {}, fromPos: {}, made: {}",
        getClass.getSimpleName, worldPosition.show, lastConnection.show)
  }

  def connectorUpdate(): Unit =
    applyToAllPipe(tile => tile.connection = tile.getEmptyConnection)

  override def load(compound: CompoundTag): Unit = {
    super.load(compound)
    this.color = compound.getInt("color")
  }

  override def saveAdditional(compound: CompoundTag): Unit = {
    compound.putInt("color", this.color)
    super.saveAdditional(compound)
  }

  override final def save(compound: CompoundTag): CompoundTag = {
    saveAdditional(compound)
    super.save(compound)
  }

  override def getUpdateTag: CompoundTag = super.serializeNBT()

  def changeColor(color: DyeColor): Unit = changeColor(colorInt = color.getMaterialColor.col)

  def changeColor(colorInt: Int): Unit =
    applyToAllPipe(_.setColor(colorInt | 0xF0000000))

  def setColor(c: Int): Unit = {
    this.color = c
    if (level != null && !level.isClientSide) {
      PacketHandler.sendToClient(TileMessage(this), level)
    }
  }

  def getColor: Int = this.color

  private def applyToAllPipe(consumer: PipeTileBase => Unit, c: PipeConnection2[BlockPos] = this.connection): Unit = {
    c.foreach { p =>
      getLevel.getBlockEntity(p) match {
        case tile: PipeTileBase => consumer.apply(tile)
        case _ =>
      }
    }
  }

  protected def getCapFromCache[CapType](@Nonnull t: BlockEntity, pos: BlockPos, dOfTile: Direction, cap: Capability[CapType]): Cap[CapType] = {
    def getAndCacheCap(): LazyOptional[CapType] = {
      val o = t.getCapability(cap, dOfTile)
      if (o.isPresent)
        weakValueMap.put((pos, dOfTile, cap), WeakReference(o))
      o
    }

    weakValueMap.get((pos, dOfTile, cap)).map {
      case WeakReference(opt) => if (opt.isPresent) opt else getAndCacheCap() // Invalid Cap
      case _ => getAndCacheCap() // Instance gc-ed.
    }.getOrElse(getAndCacheCap() /*Not cached*/).asScala.asInstanceOf[Cap[CapType]]
  }

  def removeCapCache(tilePos: BlockPos): Unit = {
    weakValueMap.partition { case ((p, _, _), _) => p == tilePos }._2.values.foreach(_.underlying.clear())
  }
}
