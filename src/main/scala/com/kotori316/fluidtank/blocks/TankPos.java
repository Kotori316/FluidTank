package com.kotori316.fluidtank.blocks;

import java.util.Locale;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public enum TankPos implements IStringSerializable {
    TOP, MIDDLE, BOTTOM, SINGLE;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getString() {
        return toString();
    }

    public static final EnumProperty<TankPos> TANK_POS_PROPERTY = EnumProperty.create("tank_pos", TankPos.class);
}
