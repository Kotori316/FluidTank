package com.kotori316.fluidtank.network

import java.util.concurrent.atomic.AtomicInteger

import com.kotori316.fluidtank.FluidTank
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import net.minecraftforge.network.simple.SimpleChannel
import net.minecraftforge.network.{NetworkRegistry, PacketDistributor}

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
    INSTANCE.registerMessage[FluidCacheMessage](counter.getAndIncrement(), classOf[FluidCacheMessage], (m, b) => m.write(b), b => new FluidCacheMessage(b), (m, s) => m.onReceive(s))
  }

  def sendToClient(message: TileMessage, world: Level): Unit = {
    INSTANCE.send(PacketDistributor.DIMENSION.`with`(() => world.dimension()), message)
  }

  def sendToClient(message: FluidCacheMessage, world: Level): Unit = {
    INSTANCE.send(PacketDistributor.DIMENSION.`with`(() => world.dimension()), message)
  }
}
