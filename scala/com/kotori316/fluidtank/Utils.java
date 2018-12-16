package com.kotori316.fluidtank;

import java.util.Optional;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import scala.Option;

public class Utils {

    public static final Material MATERIAL = new TankMaterial();
    private static final double d = 1 / 16d;
    public static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(2 * d, 0, 2 * d, 14 * d, 1d, 14 * d);

    public static final CreativeTabs CREATIVE_TABS = new CreativeTabs(FluidTank.MOD_NAME) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(FluidTank.BLOCK_TANKS.get(0));
        }
    };

    public static int toInt(long l) {
        int i = (int) l;
        if (i == l) {
            return i;
        } else {
            return l > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }

    public static <T> Optional<T> toJava(Option<T> option) {
        if (option != null && option.isDefined()) {
            return Optional.ofNullable(option.get());
        } else {
            return Optional.empty();
        }
    }

    private static class TankMaterial extends Material {
        TankMaterial() {
            super(MapColor.AIR);
            setImmovableMobility();
        }

        @Override
        public boolean isOpaque() {
            return false;
        }
    }
}
