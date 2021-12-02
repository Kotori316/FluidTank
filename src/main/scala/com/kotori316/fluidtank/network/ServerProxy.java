package com.kotori316.fluidtank.network;

import java.util.Optional;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import scala.Option;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.ModObjects;

public class ServerProxy extends SideProxy {

    @Override
    public Option<Level> getLevel(NetworkEvent.Context context) {
        return OptionConverters.toScala(Optional.ofNullable(context.getSender()).map(Entity::getCommandSenderWorld));
    }

    @Override
    public Item.Properties getTankProperties() {
        return new Item.Properties().tab(ModObjects.CREATIVE_TABS());
    }

    @Override
    public Item.Properties getReservoirProperties() {
        return new Item.Properties().tab(ModObjects.CREATIVE_TABS());
    }
}
