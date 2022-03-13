package com.kotori316.fluidtank.network

import com.kotori316.fluidtank.ModObjects
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraftforge.network.NetworkEvent

class ServerProxy extends SideProxy {
  override def getLevel(context: NetworkEvent.Context): Option[Level] = Option(context.getSender).map(_.getCommandSenderWorld)

  override def getTankProperties: Item.Properties = new Item.Properties().tab(ModObjects.CREATIVE_TABS)

  override def getReservoirProperties: Item.Properties = new Item.Properties().tab(ModObjects.CREATIVE_TABS)
}
