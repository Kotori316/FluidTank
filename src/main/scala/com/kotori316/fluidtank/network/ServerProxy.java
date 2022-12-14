package com.kotori316.fluidtank.network;

import java.util.Optional;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class ServerProxy extends SideProxy {

    @Override
    public Optional<Level> getLevel(NetworkEvent.Context context) {
        return Optional.ofNullable(context.getSender()).map(Entity::getCommandSenderWorld);
    }

    @Override
    public Item.Properties getTankProperties() {
        return new Item.Properties();
    }

    @Override
    public Item.Properties getReservoirProperties() {
        return new Item.Properties();
    }
}
