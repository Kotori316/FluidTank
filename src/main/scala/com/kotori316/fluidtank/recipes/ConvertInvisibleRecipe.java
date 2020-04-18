package com.kotori316.fluidtank.recipes;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.block.Block;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.tiles.Tiers;

public class ConvertInvisibleRecipe extends SpecialRecipe {
    private static final Logger LOGGER = LogManager.getLogger(ConvertInvisibleRecipe.class);
    public static final String LOCATION = "fluidtank:crafting_convert_invisible";
    public static final IRecipeSerializer<ConvertInvisibleRecipe> SERIALIZER =
        new SpecialRecipeSerializer<>(ConvertInvisibleRecipe::new);
    public static final Predicate<ItemStack> NON_EMPTY = ((Predicate<ItemStack>) ItemStack::isEmpty).negate();

    public ConvertInvisibleRecipe(ResourceLocation idIn) {
        super(idIn);
        LOGGER.debug("Recipe instance of ConvertInvisibleRecipe({}) was created.", idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        List<ItemStack> stacks = IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
            .filter(NON_EMPTY)
            .collect(Collectors.toList());
        return stacks.size() == 1
            && stacks.get(0).getItem() instanceof ItemBlockTank
            && ((ItemBlockTank) stacks.get(0).getItem()).hasRecipe();
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        Optional<ItemStack> stackOptional = IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot)
            .filter(NON_EMPTY)
            .findFirst();
        BlockTank block = stackOptional
            .map(ItemStack::getItem)
            .map(Block::getBlockFromItem)
            .filter(BlockTank.class::isInstance)
            .map(BlockTank.class::cast)
            .orElse(null);
        if (block == null) return ItemStack.EMPTY;
        if (ModObjects.blockTanks().contains(block)) {
            return getInvertedTank(block.tier(), stackOptional.get().getTag(), ModObjects.blockTanksInvisible());
        } else if (ModObjects.blockTanksInvisible().contains(block)) {
            return getInvertedTank(block.tier(), stackOptional.get().getTag(), ModObjects.blockTanks());
        }
        LOGGER.debug("No result item for inventory: {}",
            IntStream.range(0, inv.getSizeInventory()).mapToObj(inv::getStackInSlot).collect(Collectors.toList()));
        return ItemStack.EMPTY;
    }

    private static ItemStack getInvertedTank(Tiers tiers, CompoundNBT tag, scala.collection.immutable.List<BlockTank> list) {
        return Utils.toJava(list.find(b -> b.tier() == tiers))
            .map(ItemStack::new)
            .map(i -> {
                i.setTag(tag);
                return i;
            }).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
