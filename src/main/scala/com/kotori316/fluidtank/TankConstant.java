package com.kotori316.fluidtank;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TankConstant {

    public static final Material MATERIAL = new Material(MaterialColor.NONE, false, true, true, false,
        true, false, PushReaction.BLOCK);
    public static final double d = 1 / 16d;
    public static final AABB BOUNDING_BOX = new AABB(2 * d, 0, 2 * d, 14 * d, 1d, 14 * d);
    public static final VoxelShape TANK_SHAPE = Shapes.create(BOUNDING_BOX);
    public static TankConfig config = null;
}
