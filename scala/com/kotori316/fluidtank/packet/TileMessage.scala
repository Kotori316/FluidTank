package com.kotori316.fluidtank.packet

import com.kotori316.fluidtank.FluidTank
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}

/**
  * To both client and server.
  */
class TileMessage(tile: TileEntity) extends IMessage {
  def this() {
    this(null)
  }

  var pos: BlockPos = _
  var nbt: NBTTagCompound = _
  var dim: Int = _

  override def toBytes(buf: ByteBuf): Unit = {
    val b = new PacketBuffer(buf)
    pos = tile.getPos
    nbt = tile.writeToNBT(new NBTTagCompound)
    dim = tile.getWorld.provider.getDimension
    b.writeBlockPos(pos).writeCompoundTag(nbt).writeInt(dim)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    val b = new PacketBuffer(buf)
    pos = b.readBlockPos()
    nbt = b.readCompoundTag()
    dim = b.readInt()
  }
}

object TileMessage extends IMessageHandler[TileMessage, IMessage] {
  val instance: IMessageHandler[TileMessage, IMessage] = this

  def apply(tile: TileEntity): TileMessage = new TileMessage(tile)

  override def onMessage(message: TileMessage, ctx: MessageContext): IMessage = {
    for (world <- FluidTank.proxy.getWorld(ctx.netHandler) if world.provider.getDimension == message.dim;
         tile <- Option(world.getTileEntity(message.pos))) {
      FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(new Runnable {
        override def run(): Unit = {
          tile.readFromNBT(message.nbt)
        }
      })
    }
    null
  }
}
