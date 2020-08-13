package com.kotori316.fluidtank.transport

import cats.implicits._
import com.kotori316.fluidtank.network.{PacketHandler, TileMessage}
import com.kotori316.fluidtank.{FluidTank, _}
import javax.annotation.Nonnull
import net.minecraft.block.BlockState
import net.minecraft.item.DyeColor
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity, TileEntityType}
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional

import scala.ref.WeakReference

abstract class PipeTileBase(t: TileEntityType[_ <: PipeTileBase]) extends TileEntity(t) with ITickableTileEntity {
  var connection: PipeConnection2[BlockPos] = getEmptyConnection
  private[this] final var color = Int.unbox(Config.content.pipeColor.get())
  private[this] val weakValueMap = scala.collection.mutable.Map.empty[(BlockPos, Direction, Capability[_]), WeakReference[LazyOptional[_]]]

  private def getEmptyConnection: PipeConnection2[BlockPos] = PipeConnection2.empty { p =>
    val state = getWorld.getBlockState(p)
    if (PipeBlock.FACING_TO_PROPERTY_MAP.values().stream().allMatch(pr => state.hasProperty(pr))) {
      PipeBlock.FACING_TO_PROPERTY_MAP.values().stream().anyMatch(pr => state.get(pr).isOutput)
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
        state = getWorld.getBlockState(pos)
        if state.getBlock == this.getBlockState.getBlock
        if state.get(PipeBlock.FACING_TO_PROPERTY_MAP.get(d.getOpposite)) == PipeBlock.Connection.CONNECTED
        pos2 <- pos :: makePosList(pos)
      } yield pos2
    }

    val poses: List[BlockPos] = makePosList(getPos)
    val lastConnection = if (poses.isEmpty) {
      PipeConnection2.add(getEmptyConnection, getPos)
    } else {
      poses.foldl(getEmptyConnection)(PipeConnection2.add)
    }
    applyToAllPipe(tile => tile.connection = lastConnection, c = lastConnection)
    FluidTank.LOGGER.debug(s"PipeConnection2 by ${getClass.getName}, fromPos: $pos, made: $lastConnection")
  }

  def connectorUpdate(): Unit =
    applyToAllPipe(tile => tile.connection = tile.getEmptyConnection)

  /**
   * read nbt
   */
  override def read(state: BlockState, compound: CompoundNBT): Unit = {
    super.read(state, compound)
    this.color = compound.getInt("color")
    read(compound)
  }

  def read(compound: CompoundNBT): Unit = ()

  override def write(compound: CompoundNBT): CompoundNBT = {
    compound.putInt("color", this.color)
    super.write(compound)
  }

  override def getUpdateTag: CompoundNBT = super.serializeNBT()

  def changeColor(color: DyeColor): Unit =
    applyToAllPipe(_.setColor(color.getColorValue | 0xF0000000))

  def setColor(c: Int): Unit = {
    this.color = c
    if (world != null && !world.isRemote) {
      PacketHandler.sendToClient(TileMessage(this), world)
    }
  }

  def getColor: Int = this.color

  private def applyToAllPipe(consumer: PipeTileBase => Unit, c: PipeConnection2[BlockPos] = this.connection): Unit = {
    c.foreach { p =>
      getWorld.getTileEntity(p) match {
        case tile: PipeTileBase => consumer.apply(tile)
        case _ =>
      }
    }
  }

  protected def getCapFromCache[CapType](@Nonnull t: TileEntity, pos: BlockPos, dOfTile: Direction, cap: Capability[CapType]): Cap[CapType] = {
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
