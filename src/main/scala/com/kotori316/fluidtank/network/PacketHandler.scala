package com.kotori316.fluidtank.network

import java.util.concurrent.atomic.AtomicInteger

import com.kotori316.fluidtank.FluidTank
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.fml.network.simple.SimpleChannel
import net.minecraftforge.fml.network.{NetworkRegistry, PacketDistributor}

object PacketHandler {
  private[this] final val PROTOCOL = "1"
  val INSTANCE: SimpleChannel =
    NetworkRegistry.ChannelBuilder.named(new ResourceLocation(FluidTank.modID, "main"))
      .networkProtocolVersion(() => PROTOCOL)
      .clientAcceptedVersions(PROTOCOL.equals)
      .serverAcceptedVersions(PROTOCOL.equals)
      .simpleChannel()

  def init(): Unit = {
    val counter = new AtomicInteger(1)
    INSTANCE.registerMessage[TileMessage](counter.getAndIncrement(), classOf[TileMessage], (m, b) => m.write(b), TileMessage.apply, (m, s) => m.onReceive(s))
    INSTANCE.registerMessage[FluidCacheMessage](counter.getAndIncrement(), classOf[FluidCacheMessage], (m, b) => m.write(b), FluidCacheMessage.apply, (m, s) => m.onReceive(s))
  }

  def sendToClient(message: TileMessage, world: World): Unit = {
    INSTANCE.send(PacketDistributor.DIMENSION.`with`(() => world.getDimension.getType), message)
  }

  def sendToClient(message: FluidCacheMessage, world: World): Unit = {
    INSTANCE.send(PacketDistributor.DIMENSION.`with`(() => world.getDimension.getType), message)
  }
}
