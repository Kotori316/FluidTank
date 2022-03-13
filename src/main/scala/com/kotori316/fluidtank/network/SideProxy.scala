package com.kotori316.fluidtank.network

import java.util.function.Supplier

import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.network.NetworkEvent

object SideProxy {
  def isServer(entity: BlockEntity): Boolean = {
    if (entity == null) {
      false
    } else {
      val world = entity.getLevel
      world != null && !world.isClientSide
    }
  }

  def get: SideProxy = FMLEnvironment.dist match {
    case Dist.DEDICATED_SERVER => new SideProxy.Server().get
    case Dist.CLIENT => new SideProxy.Client().get
  }

  private class Client extends Supplier[SideProxy] {
    /**
     * Do not forget to specify the return type.
     * [[ClientProxy]] is client only class so it will cause crash of loading in invalid side.
     */
    @OnlyIn(Dist.CLIENT)
    override def get: SideProxy = new ClientProxy
  }

  private class Server extends Supplier[SideProxy] {
    override def get: SideProxy = new ServerProxy
  }
}

abstract class SideProxy {
  def getLevel(context: NetworkEvent.Context): Option[Level]

  def getTankProperties: Item.Properties

  def getReservoirProperties: Item.Properties
}
