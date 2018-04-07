package com.kotori316.fluidtank.recipes;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.tiles.Tiers;

public class TankRecipe extends ShapedRecipes {

    private static final Function<ItemStack, IFluidHandlerItem> itemHanlder =
        s -> s.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    private final Tiers tiers;
    private final boolean valid;

    public TankRecipe(Tiers tiers) {
        super("", 3, 3, NonNullList.withSize(9, Ingredient.EMPTY), ItemStack.EMPTY);

        setRegistryName(new ResourceLocation(FluidTank.modID + ":tank" + tiers.toString().toLowerCase(Locale.US)));
        this.tiers = tiers;
        OreIngredient oreIngredient = new OreIngredient(Config.content().oreNameMap().apply(tiers));
        valid = tiers.rank() > 1 && OreDictionary.doesOreNameExist(Config.content().oreNameMap().apply(tiers));
        if (valid) {
            TierIngredient tierIngredient = new TierIngredient(tiers.rank() - 1);
            IntStream.of(0, 2, 6, 8).forEach(value -> recipeItems.set(value, oreIngredient));
            IntStream.of(1, 3, 5, 7).forEach(value -> recipeItems.set(value, tierIngredient));
        }
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
        for (int x = 0; x <= inv.getWidth() - 3; x++) {
            for (int y = 0; y <= inv.getHeight() - 3; ++y) {
                if (checkMatch(inv, x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Based on {@link net.minecraft.item.crafting.ShapedRecipes#checkMatch(InventoryCrafting, int, int, boolean)}
     */
    protected boolean checkMatch(InventoryCrafting inv, int startX, int startY) {
        FluidStack stack = null;
        for (int x = 0; x < inv.getWidth(); x++) {
            for (int y = 0; y < inv.getHeight(); y++) {
                int subX = x - startX;
                int subY = y - startY;
                Ingredient target = Ingredient.EMPTY;

                if (subX >= 0 && subY >= 0 && subX < 3 && subY < 3) {
                    target = recipeItems.get(subX + subY * 3);
                }

                ItemStack stackInRowAndColumn = inv.getStackInRowAndColumn(x, y);
                if (target.apply(stackInRowAndColumn)) {
                    if (target instanceof TierIngredient) {
                        FluidStack fluidStack = Optional.of(stackInRowAndColumn).map(itemHanlder)
                            .map(h -> h.getTankProperties()[0]).map(IFluidTankProperties::getContents).orElse(null);
                        if (stack == null) {
                            if (fluidStack != null) {
                                stack = fluidStack;
                            }
                        } else {
                            if (fluidStack != null && !stack.isFluidEqual(fluidStack)) {
                                return false;
                            }
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        FluidStack stack = IntStream.of(1, 3, 5, 7).mapToObj(inv::getStackInSlot)
            .map(itemHanlder)
            .map(IFluidHandler::getTankProperties)
            .flatMap(WrapFluid::newStreamWithValidStack)
            .reduce(WrapFluid::combine)
            .map(WrapFluid::getStack).orElse(null);
        ItemStack copy = getRecipeOutput().copy();
        Optional.of(copy).map(itemHanlder).ifPresent(h -> h.fill(stack, true));
        return copy;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return valid ? new ItemStack(FluidTank.BLOCK_TANKS.get(tiers.rank() - 1), 1, tiers.meta()) : super.getRecipeOutput();
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        return "Recipe of " + tiers + " valid = " + valid;
    }

    private static class WrapFluid {
        private FluidStack stack;

        private WrapFluid(FluidStack stack) {
            this.stack = stack;
        }

        private static WrapFluid combine(WrapFluid w1, WrapFluid w2) {
            w1.stack.amount += w2.stack.amount;
            return w1;
        }

        private static Stream<WrapFluid> newStreamWithValidStack(IFluidTankProperties[] properties) {
            if (properties.length > 0) {
                FluidStack stack = properties[0].getContents();
                if (stack != null) {
                    return Stream.of(new WrapFluid(stack));
                }
            }
            return Stream.empty();
        }

        public FluidStack getStack() {
            return stack;
        }
    }
}
