package com.kotori316.fluidtank;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.integration.ae2.TankAE2Plugin;
import com.kotori316.fluidtank.integration.top.FluidTankTOPPlugin;
import com.kotori316.fluidtank.network.PacketHandler;
import com.kotori316.fluidtank.network.SideProxy;
import com.kotori316.fluidtank.recipes.CombineRecipe;
import com.kotori316.fluidtank.recipes.FluidTankConditions;
import com.kotori316.fluidtank.recipes.FluidTankDataProvider;
import com.kotori316.fluidtank.recipes.ReservoirRecipe;
import com.kotori316.fluidtank.recipes.TagCondition;
import com.kotori316.fluidtank.recipes.TierRecipe;
import com.kotori316.fluidtank.tiles.CATContainer;

@Mod(FluidTank.modID)
public class FluidTank {
    public static final String MOD_NAME = "FluidTank";
    public static final String modID = "fluidtank";
    public static final Logger LOGGER = Utils.getLogger(MOD_NAME);
    public static final SideProxy proxy = SideProxy.get();

    public FluidTank() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.sync(new ForgeConfigSpec.Builder()));
        ForgeMod.enableMilkFluid();
        FMLJavaModLoadingContext.get().getModEventBus().register(Register.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(proxy);
    }

    public static class Register {
        @SubscribeEvent
        public static void init(FMLCommonSetupEvent event) {
            PacketHandler.init();
            FluidTankTOPPlugin.sendIMC().apply(modID);
            TankAE2Plugin.onAPIAvailable();
        }

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(Registry.BLOCK_REGISTRY, Register::registerBlocks);
            event.register(Registry.ITEM_REGISTRY, Register::registerItems);
            event.register(Registry.BLOCK_ENTITY_TYPE_REGISTRY, Register::registerTiles);
            event.register(Registry.MENU_REGISTRY, Register::registerContainerType);
            event.register(Registry.RECIPE_SERIALIZER_REGISTRY, Register::registerSerializer);
        }

        public static void registerBlocks(RegisterEvent.RegisterHelper<Block> helper) {
            CollectionConverters.asJava(ModObjects.blockTanks()).forEach(b -> helper.register(b.registryName(), b));
            helper.register(ModObjects.blockCat().registryName(), ModObjects.blockCat());
            helper.register(ModObjects.blockFluidPipe().registryName, ModObjects.blockFluidPipe());
            helper.register(ModObjects.blockItemPipe().registryName, ModObjects.blockItemPipe());
            helper.register(ModObjects.blockSource().registryName(), ModObjects.blockSource());
        }

        public static void registerItems(RegisterEvent.RegisterHelper<Item> helper) {
            CollectionConverters.asJava(ModObjects.blockTanks()).stream().map(BlockTank::itemBlock).forEach(b -> helper.register(b.registryName(), b));
            helper.register(ModObjects.blockCat().registryName(), ModObjects.blockCat().itemBlock());
            helper.register(ModObjects.blockFluidPipe().registryName, ModObjects.blockFluidPipe().itemBlock());
            helper.register(ModObjects.blockItemPipe().registryName, ModObjects.blockItemPipe().itemBlock());
            helper.register(ModObjects.blockSource().registryName(), ModObjects.blockSource().itemBlock());
            CollectionConverters.asJava(ModObjects.itemReservoirs()).forEach(b -> helper.register(b.registryName(), b));
        }

        public static void registerTiles(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
            CollectionConverters.asJava(ModObjects.getTileTypes()).forEach(e -> helper.register(e.name(), e.t()));
        }

        public static void registerSerializer(RegisterEvent.RegisterHelper<RecipeSerializer<?>> helper) {
            helper.register(new ResourceLocation(CombineRecipe.LOCATION), CombineRecipe.SERIALIZER);
            helper.register(TierRecipe.Serializer.LOCATION, TierRecipe.SERIALIZER);
            helper.register(ReservoirRecipe.Serializer.LOCATION, ReservoirRecipe.SERIALIZER);
            CraftingHelper.register(new FluidTankConditions.ConfigCondition().serializer);
            CraftingHelper.register(new FluidTankConditions.EasyCondition().serializer);
            CraftingHelper.register(TagCondition.Serializer.INSTANCE);
        }

        public static void registerContainerType(RegisterEvent.RegisterHelper<MenuType<?>> helper) {
            helper.register(new ResourceLocation(CATContainer.GUI_ID), ModObjects.CAT_CONTAINER_TYPE());
        }

        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            FluidTankDataProvider.gatherData(event);
        }
    }

}
