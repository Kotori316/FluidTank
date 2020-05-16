package com.kotori316.fluidtank.transport

import cats.implicits._
import com.kotori316.fluidtank.network.{PacketHandler, TileMessage}
import com.kotori316.fluidtank.{FluidTank, _}
import net.minecraft.item.DyeColor
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity, TileEntityType}
import net.minecraft.util.math.BlockPos

abstract class PipeTileBase(t: TileEntityType[_ <: PipeTileBase]) extends TileEntity(t) with ITickableTileEntity {
  var connection: PipeConnection[BlockPos] = getEmptyConnection
  private[this] final var color = Int.unbox(Config.content.pipeColor.get())

  private def getEmptyConnection: PipeConnection[BlockPos] = PipeConnection.empty({ case (p, c) =>
    getWorld.getTileEntity(p) match {
      case pipeTile: PipeTileBase => pipeTile.connection = c
      case _ =>
    }
  }, p => {
    val state = getWorld.getBlockState(p)
    if (PipeBlock.FACING_TO_PROPERTY_MAP.values().stream().allMatch(pr => state.has(pr))) {
      PipeBlock.FACING_TO_PROPERTY_MAP.values().stream().anyMatch(pr => state.get(pr).isOutput)
    } else {
      false
    }
  }
  )

  def makeConnection(): Unit = {
    val checked = scala.collection.mutable.Set.empty[BlockPos]

    def makePosList(start: BlockPos): List[BlockPos] = {
      for {
        d <- directions
        pos <- start.offset(d).pure[List]
        if checked.add(pos) // True means it's first time to check the pos. False means the pos already checked.
        state <- getWorld.getBlockState(pos).pure[List]
        if PipeBlock.FACING_TO_PROPERTY_MAP.values().stream().allMatch(pr => state.has(pr))
        if state.get(PipeBlock.FACING_TO_PROPERTY_MAP.get(d.getOpposite)) == PipeBlock.Connection.CONNECTED
        pos2 <- pos :: makePosList(pos)
      } yield pos2
    }

    val poses: List[BlockPos] = makePosList(getPos)
    val lastConnection = if (poses.isEmpty) getEmptyConnection.add(getPos) else poses.foldl(getEmptyConnection) { case (c, p) => c add p }
    FluidTank.LOGGER.debug(s"PipeConnection, fromPos: $pos, made: $lastConnection")
  }

  def connectorUpdate(): Unit = {
    connection.reset()
  }

  override def read(compound: CompoundNBT): Unit = {
    super.read(compound)
    this.color = compound.getInt("color")
  }

  override def write(compound: CompoundNBT): CompoundNBT = {
    compound.putInt("color", this.color)
    super.write(compound)
  }

  override def getUpdateTag: CompoundNBT = super.serializeNBT()

  def changeColor(color: DyeColor): Unit = {
    this.connection.poses.map(world.getTileEntity).foreach {
      case tile: PipeTileBase => tile.setColor(color.getColorValue | 0xF0000000)
      case _ =>
    }
  }

  def setColor(c: Int): Unit = {
    this.color = c
    if (world != null && !world.isRemote) {
      PacketHandler.sendToClient(TileMessage(this), world)
    }
  }

  def getColor: Int = this.color
}
