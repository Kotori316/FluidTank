package com.kotori316.fluidtank.network;

import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import scala.Option;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.ModObjects;

public class ServerProxy extends SideProxy {

    @Override
    public Option<World> getWorld(NetworkEvent.Context context) {
        return OptionConverters.toScala(Optional.ofNullable(context.getSender()).map(Entity::getEntityWorld));
    }

    @Override
    public Item.Properties getTankProperties() {
        return new Item.Properties().group(ModObjects.CREATIVE_TABS());
    }
}
