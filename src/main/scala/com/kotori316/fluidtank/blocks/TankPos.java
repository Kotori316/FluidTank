package com.kotori316.fluidtank.blocks;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum TankPos implements StringRepresentable {
    TOP, MIDDLE, BOTTOM, SINGLE;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return toString();
    }

    public static final EnumProperty<TankPos> TANK_POS_PROPERTY = EnumProperty.create("tank_pos", TankPos.class);
}
