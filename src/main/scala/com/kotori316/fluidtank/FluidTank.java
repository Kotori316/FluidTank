package com.kotori316.fluidtank;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
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
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            CollectionConverters.asJava(ModObjects.blockTanks()).forEach(event.getRegistry()::register);
            event.getRegistry().register(ModObjects.blockCat());
            event.getRegistry().register(ModObjects.blockFluidPipe());
            event.getRegistry().register(ModObjects.blockItemPipe());
            event.getRegistry().register(ModObjects.blockSource());
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            CollectionConverters.asJava(ModObjects.blockTanks()).stream().map(BlockTank::itemBlock).forEach(event.getRegistry()::register);
            event.getRegistry().register(ModObjects.blockCat().itemBlock());
            event.getRegistry().register(ModObjects.blockFluidPipe().itemBlock());
            event.getRegistry().register(ModObjects.blockItemPipe().itemBlock());
            event.getRegistry().register(ModObjects.blockSource().itemBlock());
            CollectionConverters.asJava(ModObjects.itemReservoirs()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<BlockEntityType<?>> event) {
            CollectionConverters.asJava(ModObjects.getTileTypes()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerSerializer(RegistryEvent.Register<RecipeSerializer<?>> event) {
            event.getRegistry().register(CombineRecipe.SERIALIZER.setRegistryName(new ResourceLocation(CombineRecipe.LOCATION)));
            event.getRegistry().register(TierRecipe.SERIALIZER);
            event.getRegistry().register(ReservoirRecipe.SERIALIZER);
            CraftingHelper.register(new FluidTankConditions.ConfigCondition().serializer);
            CraftingHelper.register(new FluidTankConditions.EasyCondition().serializer);
            CraftingHelper.register(TagCondition.Serializer.INSTANCE);
        }

        @SubscribeEvent
        public static void registerContainerType(RegistryEvent.Register<MenuType<?>> event) {
            event.getRegistry().register(ModObjects.CAT_CONTAINER_TYPE());
        }

        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            FluidTankDataProvider.gatherData(event);
        }
    }

}
