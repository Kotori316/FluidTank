package com.kotori316.fluidtank.recipes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.testutil.GameTestUtil;

import static com.kotori316.testutil.GameTestUtil.EMPTY_STRUCTURE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@GameTestHolder(value = FluidTank.modID)
@PrefixGameTestTemplate(value = false)
public final class RecipeGameTest {
    static final String BATCH = "recipeTestBatch";

    @GameTestGenerator
    public List<TestFunction> tierRecipeSerialize() {
        return Arrays.stream(Tier.values()).filter(Tier::hasTagRecipe)
            .map(t -> GameTestUtil.create(FluidTank.modID, BATCH, "tierRecipeSerialize" + t, g -> {
                new TierRecipeTest().serializeJson(t, GameTestUtil.getContext(g));
                g.succeed();
            })).toList();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void tanksForStone(GameTestHelper helper) {
        var tanks = TierRecipe.getTankSet(Tier.STONE, GameTestUtil.getContext(helper));
        assertEquals(Set.of(ModObjects.tierToBlock().apply(Tier.WOOD)), tanks);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void tanksForIronUnavailableOK(GameTestHelper helper) {
        try {
            Config.content().usableUnavailableTankInRecipe().set(true);
            var tanks = TierRecipe.getTankSet(Tier.IRON, GameTestUtil.getContext(helper));
            assertEquals(Set.of(
                ModObjects.tierToBlock().apply(Tier.STONE),
                ModObjects.tierToBlock().apply(Tier.COPPER),
                ModObjects.tierToBlock().apply(Tier.TIN)
            ), tanks);
            helper.succeed();
        } finally {
            Config.content().usableUnavailableTankInRecipe().set(true);
        }
    }

    @GameTest(template = EMPTY_STRUCTURE, batch = BATCH)
    public void tanksForIronUnavailableNG(GameTestHelper helper) {
        try {
            Config.content().usableUnavailableTankInRecipe().set(false);
            var tanks = TierRecipe.getTankSet(Tier.IRON, GameTestUtil.getContext(helper));
            assertEquals(Set.of(
                ModObjects.tierToBlock().apply(Tier.STONE),
                ModObjects.tierToBlock().apply(Tier.COPPER)
            ), tanks);
            helper.succeed();
        } finally {
            Config.content().usableUnavailableTankInRecipe().set(true);
        }
    }

    @GameTestGenerator
    public List<TestFunction> reservoirRecipeSerialize() {
        return ReservoirRecipeSerializeTest.tierAndIngredient()
            .map(o -> GameTestUtil.create(FluidTank.modID, BATCH, "reservoirRecipeSerialize" + o[0], g -> {
                new ReservoirRecipeSerializeTest().serializeJson((Tier) o[0], (Ingredient) o[1], GameTestUtil.getContext(g));
                g.succeed();
            })).toList();
    }
}
