package com.kotori316.fluidtank;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class Utils {

    public static final String BLOCK_ENTITY_TAG = "BlockEntityTag";

    public static int toInt(long l) {
        int i = (int) l;
        if (i == l) {
            return i;
        } else {
            return l > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }

    private static final AtomicInteger inDev = new AtomicInteger(-1);

    public static boolean isInDev() {
        int i = inDev.get();
        if (i == -1) {
            inDev.set(!FMLLoader.isProduction() || Config.content().debug().get() ? 1 : 0);
            return inDev.get() == 1;
        }
        return i == 1;
    }

    @VisibleForTesting
    public static void setInDev(boolean inDev) {
        Utils.inDev.set(inDev ? 1 : 0);
    }

    @SuppressWarnings({"unused", "SpellCheckingInspection"})
    private static void dummy() {
        /*
        Cheat code to get filled tank.
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 16000L, fluid: "silents_mechanisms:diesel", capacity: 16000L}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 8000L, fluid: "silents_mechanisms:ethane", capacity: 16000L}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 8000L, fluid: "minecraft:water", capacity: 16000L}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 10L, fluid: "minecraft:lava", capacity: 16000L}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 10L, fluid: "minecraft:lava", capacity: 16000L}}}
        /give @p fluidtank:tank_wood{BlockEntityTag:{tank:{amount:3000L,capacity:4000L,fluid:"minecraft:lava"},tier:"wood"}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: "stone", tank: {amount: 8000L, fluid: "minecraft:water", capacity: 16000L}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 10000L, fluid: "fluidtank:vanilla_milk", capacity: 16000L}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: "Stone", id: "fluidtank:tiletank", tank: {amount: 10000L, fluid: "fluidtank:vanilla_milk", capacity: 16000L}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier:"stone",tank:{amount:10000L,fluid:"fluidtank:vanilla_milk",capacity:16000L}}}
        */
        /*
        /setblock ~ ~-1 ~ fluidtank:tank_stone{tier: "stone", id: "fluidtank:tiletank", tank: {amount: 3000L, fluid: "minecraft:lava", capacity: 4000L}}
        /data merge block ~ ~-1 ~ {tank: {fluid: "minecraft:lava"}}
        */
    }

    /**
     * Replacement of {@link net.minecraft.world.item.BlockItem#setBlockEntityData(ItemStack, BlockEntityType, CompoundTag)}.
     * The method requires {@link BlockEntityType} as parameter to add "id", but it is not available in serializing tile data in item.
     * Then, the nbt of crafted tank and of removed tank will be different, which makes items un-stackable.
     * <p>
     * To solve this issue, the "id" should not be saved in item nbt. This is why I created this method.
     * <p>
     * This method will remove "BlockEntityTag" in stack tag if the {@code tileTag} is {@code null} or empty.
     *
     * @param stack   the stack where the nbt saved
     * @param tileTag The nbt provided by {@link BlockEntity#saveWithoutMetadata()}.
     *                If {@code null} or empty, the nbt in stack will be removed instead of putting empty tag.
     */
    public static void setTileTag(@NotNull ItemStack stack, @Nullable CompoundTag tileTag) {
        if (tileTag == null || tileTag.isEmpty()) {
            stack.removeTagKey(BLOCK_ENTITY_TAG);
        } else {
            stack.addTagElement(BLOCK_ENTITY_TAG, tileTag);
        }
    }

    /**
     * Helper method copied from {@link net.minecraft.world.level.block.BaseEntityBlock}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> checkType(BlockEntityType<A> type1, BlockEntityType<E> exceptedType, BlockEntityTicker<? super E> ticker) {
        return exceptedType == type1 ? (BlockEntityTicker<A>) ticker : null;
    }

    public static OptionalInt getItemColor(ItemStack stack) {
        DyeColor color = DyeColor.getColor(stack);
        if (color != null)
            return OptionalInt.of(color.getMaterialColor().col);
        if (stack.getItem() instanceof DyeableLeatherItem item) {
            return OptionalInt.of(item.getColor(stack));
        }
        return OptionalInt.empty();
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        try {
            var field = Class.forName("net.minecraftforge.fml.ModLoader").getDeclaredField("LOGGER");
            field.setAccessible(true);
            var loaderLogger = (org.apache.logging.log4j.core.Logger) field.get(null);
            return loaderLogger.getContext().getLogger(name);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Can't access to LOGGER in loader.", e);
        }
    }

    public static <FROM, TO extends FROM> BiConsumer<FROM, Consumer<TO>> cast(Class<TO> toClass) {
        Objects.requireNonNull(toClass);
        return (from, toConsumer) -> {
            if (toClass.isInstance(from)) {
                toConsumer.accept(toClass.cast(from));
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> getTagElements(TagKey<T> tag) {
        return BuiltInRegistries.REGISTRY.entrySet().stream()
            .filter(e -> tag.isFor(e.getKey()))
            .map(Map.Entry::getValue)
            .findFirst()
            .map(r -> (Registry<T>) r) // Unchecked, but it must pass.
            .flatMap(r -> r.getTag(tag))
            .stream()
            .flatMap(HolderSet.Named::stream)
            .map(Holder::value)
            .collect(Collectors.toSet());
    }

    @SuppressWarnings("deprecation")
    public static boolean isInTag(Fluid fluid, TagKey<Fluid> tagKey) {
        return fluid.is(tagKey);
    }

    public static String convertIngredientToString(Collection<Ingredient> ingredients) {
        return ingredients.stream()
            .map(Ingredient::toJson)
            .map(JsonElement::toString)
            .collect(Collectors.joining(", ", "[", "]"));
    }

    public static String convertIngredientToString(Ingredient ingredient) {
        return "[%s]".formatted(ingredient.toJson());
    }
}
