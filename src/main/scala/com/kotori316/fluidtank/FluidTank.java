package com.kotori316.fluidtank;

import java.util.function.Consumer;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.fluids.FabricFluidTankStorage;
import com.kotori316.fluidtank.network.PacketHandler;
import com.kotori316.fluidtank.recipes.CombineRecipe;
import com.kotori316.fluidtank.recipes.FluidTankConditions;
import com.kotori316.fluidtank.recipes.ReservoirRecipe;
import com.kotori316.fluidtank.recipes.TierRecipe;
import com.kotori316.fluidtank.tiles.CATContainer;

public class FluidTank implements ModInitializer {
    public static final String MOD_NAME = "FluidTank";
    public static final String modID = "fluidtank";
    public static final Logger LOGGER = Utils.getLogger(MOD_NAME);
    public static TankConfig config;

    @Override
    public void onInitialize() {
        FluidTank.LOGGER.debug("Universal init is called. {} ", FluidTank.modID);
        AutoConfig.register(TankConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(TankConfig.class).getConfig();
        PacketHandler.Server.initServer();
        Register.register();
        FabricFluidTankStorage.register();
    }

    public static class Register {
        public static void register() {
            register(Registry.BLOCK_REGISTRY, Register::registerBlocks);
            register(Registry.ITEM_REGISTRY, Register::registerItems);
            register(Registry.BLOCK_ENTITY_TYPE_REGISTRY, Register::registerTiles);
            register(Registry.MENU_REGISTRY, Register::registerContainerType);
            register(Registry.RECIPE_SERIALIZER_REGISTRY, Register::registerSerializer);
        }

        private static <T> void register(ResourceKey<Registry<T>> registry, Consumer<RegisterHelper<T>> adderMethod) {
            var helper = new RegisterHelper<>(registry);
            adderMethod.accept(helper);
        }

        private record RegisterHelper<T>(ResourceKey<Registry<T>> key) {
            @SuppressWarnings("unchecked")
            void register(ResourceLocation location, T t) {
                var registry = (Registry<T>) Registry.REGISTRY.get(key.location());
                assert registry != null;
                Registry.register(registry, location, t);
            }
        }

        public static void registerBlocks(RegisterHelper<Block> helper) {
            CollectionConverters.asJava(ModObjects.blockTanks()).forEach(b -> helper.register(b.registryName(), b));
            helper.register(ModObjects.blockCat().registryName(), ModObjects.blockCat());
            helper.register(ModObjects.blockFluidPipe().registryName, ModObjects.blockFluidPipe());
            helper.register(ModObjects.blockItemPipe().registryName, ModObjects.blockItemPipe());
            helper.register(ModObjects.blockSource().registryName(), ModObjects.blockSource());
        }

        public static void registerItems(RegisterHelper<Item> helper) {
            CollectionConverters.asJava(ModObjects.blockTanks()).stream().map(BlockTank::itemBlock).forEach(b -> helper.register(b.registryName(), b));
            helper.register(ModObjects.blockCat().registryName(), ModObjects.blockCat().itemBlock());
            helper.register(ModObjects.blockFluidPipe().registryName, ModObjects.blockFluidPipe().itemBlock());
            helper.register(ModObjects.blockItemPipe().registryName, ModObjects.blockItemPipe().itemBlock());
            helper.register(ModObjects.blockSource().registryName(), ModObjects.blockSource().itemBlock());
            CollectionConverters.asJava(ModObjects.itemReservoirs()).forEach(b -> helper.register(b.registryName(), b));
        }

        public static void registerTiles(RegisterHelper<BlockEntityType<?>> helper) {
            CollectionConverters.asJava(ModObjects.getTileTypes()).forEach(e -> helper.register(e.name(), e.t()));
        }

        public static void registerSerializer(RegisterHelper<RecipeSerializer<?>> helper) {
            helper.register(new ResourceLocation(CombineRecipe.LOCATION), CombineRecipe.SERIALIZER);
            helper.register(TierRecipe.Serializer.LOCATION, TierRecipe.SERIALIZER);
            helper.register(ReservoirRecipe.Serializer.LOCATION, ReservoirRecipe.SERIALIZER);
            ResourceConditions.register(FluidTankConditions.ConfigCondition.LOCATION, new FluidTankConditions.ConfigCondition());
            ResourceConditions.register(FluidTankConditions.EasyCondition.LOCATION, new FluidTankConditions.EasyCondition());
        }

        public static void registerContainerType(RegisterHelper<MenuType<?>> helper) {
            helper.register(new ResourceLocation(CATContainer.GUI_ID), ModObjects.CAT_CONTAINER_TYPE());
        }

    }

}
