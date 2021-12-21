package com.kotori316.fluidtank.recipe;

import java.util.List;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.TankConstant;
import com.kotori316.fluidtank.tank.Tiers;

public class TankRecipe extends CustomRecipe {
    public static final SimpleRecipeSerializer<TankRecipe> SERIALIZER = new SimpleRecipeSerializer<>(TankRecipe::new);
    public static final ResourceLocation LOCATION = new ResourceLocation(ModTank.modID, "metal_recipe");
    private final List<TierRecipe.Logic> logics;

    public TankRecipe(ResourceLocation id) {
        super(id);
        if (TankConstant.config.enableUpdateRecipe) {
            logics = Stream.of(Tiers.TIN, Tiers.LEAD, Tiers.BRONZE, Tiers.SILVER)
                .map(TierRecipe.Logic::new).toList();
        } else {
            logics = List.of(TierRecipe.EmptyLogic.INSTANCE);
        }
    }

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        return logics.stream().anyMatch(l -> l.matches(inv));
    }

    @Override
    public ItemStack assemble(CraftingContainer inv) {
        return logics.stream().filter(l -> l.matches(inv)).findFirst().map(l -> l.craft(inv)).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
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
