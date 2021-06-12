package com.kotori316.fluidtank.recipe;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.tank.Tiers;

public class TankRecipe extends SpecialCraftingRecipe {
    public static final SpecialRecipeSerializer<TankRecipe> SERIALIZER = new SpecialRecipeSerializer<>(TankRecipe::new);
    public static final Identifier LOCATION = new Identifier(ModTank.modID, "metal_recipe");
    private final List<TierRecipe.Logic> logics;

    public TankRecipe(Identifier id) {
        super(id);
        logics = Collections.unmodifiableList(Stream.of(Tiers.TIN, Tiers.LEAD, Tiers.BRONZE, Tiers.SILVER)
            .map(TierRecipe.Logic::new).collect(Collectors.toList()));
    }

    @Override
    public boolean matches(CraftingInventory inv, World world) {
        return logics.stream().anyMatch(l -> l.matches(inv));
    }

    @Override
    public ItemStack craft(CraftingInventory inv) {
        return logics.stream().filter(l -> l.matches(inv)).findFirst().map(l -> l.craft(inv)).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public String getGroup() {
        return TierRecipe.GROUP;
    }
}
