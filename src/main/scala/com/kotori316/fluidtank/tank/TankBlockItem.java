package com.kotori316.fluidtank.tank;

import java.util.List;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModTank;

public class TankBlockItem extends BlockItem {
    public final TankBlock blockTank;

    public TankBlockItem(TankBlock block) {
        super(block, new Item.Settings().group(ModTank.CREATIVE_TAB));
        blockTank = block;
    }

    public boolean hasRecipe() {
        return true;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        if (stack.hasTag()) {
            if (stack.getTag() != null && stack.getTag().contains(TankBlock.NBT_BlockTag)) {
                return Rarity.RARE;
            }
        }
        return Rarity.COMMON;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        CompoundTag nbt = stack.getSubTag(TankBlock.NBT_BlockTag);
        if (nbt != null) {
            CompoundTag tankNBT = nbt.getCompound(TankBlock.NBT_Tank);
            FluidAmount fluid = FluidAmount.fromNBT(tankNBT);
            long c = tankNBT.getInt(TankBlock.NBT_Capacity);
            tooltip.add(new LiteralText(fluid.getLocalizedName() + " : " + fluid.amount() + " mB / " + c + " mB"));
        } else {
            tooltip.add(new LiteralText("Capacity : " + blockTank.tiers.amount() + "mB"));
        }
    }
}
