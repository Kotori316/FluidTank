package com.kotori316.fluidtank.tank;

import java.util.List;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModTank;

public class TankBlockItem extends BlockItem {
    public final TankBlock blockTank;

    public TankBlockItem(TankBlock block) {
        super(block, new Item.Properties().tab(ModTank.CREATIVE_TAB));
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
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        CompoundTag nbt = stack.getTagElement(TankBlock.NBT_BlockTag);
        if (nbt != null) {
            CompoundTag tankNBT = nbt.getCompound(TankBlock.NBT_Tank);
            FluidAmount fluid = FluidAmount.fromNBT(tankNBT);
            long c = tankNBT.getInt(TankBlock.NBT_Capacity);
            tooltip.add(new TextComponent(fluid.getLocalizedName() + " : " + fluid.fluidVolume().amount().asLong(FluidAmount.AMOUNT_BUCKET()) + " mB / " + c + " mB"));
        } else {
            tooltip.add(new TextComponent("Capacity : " + blockTank.tiers.amount() + "mB"));
        }
    }
}
