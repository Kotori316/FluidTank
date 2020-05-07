package com.kotori316.fluidtank.transport

import cats.Eval
import cats.data.OptionT
import cats.implicits._
import com.kotori316.fluidtank.network.{PacketHandler, TileMessage}
import com.kotori316.fluidtank.tiles.{CapabilityFluidTank, Tiers}
import com.kotori316.fluidtank.{FluidTank, ModObjects, _}
import net.minecraft.item.DyeColor
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.{ITickableTileEntity, TileEntity}
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.CapabilityFluidHandler

class PipeTile extends TileEntity(ModObjects.PIPE_TYPE) with ITickableTileEntity {
  var connection: PipeConnection2[BlockPos] = getEmptyConnection
  val handler = new PipeFluidHandler(this)
  private[this] final var color = Int.unbox(Config.content.pipeColor.get())

  private def getEmptyConnection: PipeConnection2[BlockPos] = PipeConnection2.empty(p =>
    getWorld.getBlockState(p) match {
      case s if s.getBlock == ModObjects.blockPipe =>
        PipeBlock.FACING_TO_PROPERTY_MAP.values().stream().anyMatch(pr => s.get(pr).isOutput)
      case _ => false
    }
  )

  override def tick(): Unit = if (!world.isRemote) {
    if (connection.isEmpty)
      makeConnection()
    require(!connection.isEmpty, "Is connection still empty?")
    import scala.jdk.CollectionConverters._
    val pullFrom = PipeBlock.FACING_TO_PROPERTY_MAP.asScala.toSeq.flatMap { case (direction, property) =>
      if (getBlockState.get(property).isInput) {
        val sourcePos = pos.offset(direction)
        val c = for {
          t <- OptionT.fromOption[Eval](Option(getWorld.getTileEntity(sourcePos)))
          cap <- t.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite).asScala
        } yield cap -> sourcePos
        c.toList
      } else {
        List.empty
      }
    }
    pullFrom.foreach { case (f, sourcePos) =>
      for {
        p <- connection.outputs(sourcePos)
        (direction, pos) <- directions.map(f => f -> p.offset(f))
        if pos != sourcePos
        if getWorld.getBlockState(p).get(PipeBlock.FACING_TO_PROPERTY_MAP.get(direction)).isOutput
        dest <- OptionT.fromOption[Eval](Option(getWorld.getTileEntity(pos)))
          .flatMap(_.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite).asScala)
          .toList
        if f != dest
      } {
        val transferSimulate = FluidUtil.tryFluidTransfer(dest, f, PipeTile.amountPerTick, false)
        if (!transferSimulate.isEmpty) {
          FluidUtil.tryFluidTransfer(dest, f, transferSimulate, true)
        }
      }
    }
  }

  def makeConnection(): Unit = {
    val checked = scala.collection.mutable.Set.empty[BlockPos]

    def makePosList(start: BlockPos): List[BlockPos] = {
      for {
        d <- directions
        pos <- start.offset(d).pure[List]
        if checked.add(pos) // True means it's first time to check the pos. False means the pos already checked.
        state <- getWorld.getBlockState(pos).pure[List]
        if state.getBlock == ModObjects.blockPipe
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
    FluidTank.LOGGER.debug(s"PipeConnection2, fromPos: $pos, made: $lastConnection")
  }

  def connectorUpdate(): Unit =
    applyToAllPipe(tile => tile.connection = tile.getEmptyConnection)

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    Cap.asJava(
      Cap.make(handler.asInstanceOf[T])
        .filter(_ => cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || cap == CapabilityFluidTank.cap)
        .filter(_ => side != null && getBlockState.get(PipeBlock.FACING_TO_PROPERTY_MAP.get(side)).is(PipeBlock.Connection.CONNECTED, PipeBlock.Connection.INPUT))
        .orElse(super.getCapability(cap, side).asScala)
    )
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

  def changeColor(color: DyeColor): Unit =
    applyToAllPipe(_.setColor(color.getColorValue | 0xF0000000))

  def setColor(c: Int): Unit = {
    this.color = c
    if (world != null && !world.isRemote) {
      PacketHandler.sendToClient(TileMessage(this), world)
    }
  }

  def getColor: Int = this.color

  private def applyToAllPipe(consumer: PipeTile => Unit, c: PipeConnection2[BlockPos] = this.connection): Unit = {
    c.foreach { p =>
      getWorld.getTileEntity(p) match {
        case tile: PipeTile => consumer.apply(tile)
        case _ =>
      }
    }
  }
}

object PipeTile {
  final val amountPerTick = Utils.toInt(Tiers.STONE.amount)
}
