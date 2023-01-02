package com.kotori316.fluidtank;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.VisibleForTesting;
import scala.jdk.javaapi.CollectionConverters;

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
        registerConfig(false);
        ForgeMod.enableMilkFluid();
        FMLJavaModLoadingContext.get().getModEventBus().register(Register.class);
        FMLJavaModLoadingContext.get().getModEventBus().register(proxy);
    }

    @VisibleForTesting
    static void registerConfig(boolean inJUnitTest) {
        var configSpec = Config.sync(new ForgeConfigSpec.Builder());
        if (inJUnitTest || ForgeGameTestHooks.isGametestServer()) {
            // In game test. Use in-memory config.
            final CommentedConfig commentedConfig = CommentedConfig.inMemory();
            configSpec.correct(commentedConfig);
            configSpec.acceptConfig(commentedConfig);
        } else {
            ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configSpec);
        }
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
            event.register(Registries.BLOCK, Register::registerBlocks);
            event.register(Registries.ITEM, Register::registerItems);
            event.register(Registries.BLOCK_ENTITY_TYPE, Register::registerTiles);
            event.register(Registries.MENU, Register::registerContainerType);
            event.register(Registries.RECIPE_SERIALIZER, Register::registerSerializer);
        }

        public static void registerBlocks(RegisterEvent.RegisterHelper<Block> helper) {
            CollectionConverters.asJava(ModObjects.blockTanks()).forEach(b -> helper.register(b.registryName(), b));
            helper.register(ModObjects.blockCat().registryName(), ModObjects.blockCat());
            helper.register(ModObjects.blockFluidPipe().registryName, ModObjects.blockFluidPipe());
            helper.register(ModObjects.blockItemPipe().registryName, ModObjects.blockItemPipe());
            helper.register(ModObjects.blockSource().registryName(), ModObjects.blockSource());
            CollectionConverters.asJava(ModObjects.gasTanks()).forEach(b -> helper.register(b.registryName(), b));
        }

        public static void registerItems(RegisterEvent.RegisterHelper<Item> helper) {
            CollectionConverters.asJava(ModObjects.allItems()).forEach(e ->
                helper.register(e.name(), e.t())
            );
        }

        public static void registerTiles(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
            CollectionConverters.asJava(ModObjects.getTileTypes()).forEach(e -> helper.register(e.name(), e.t()));
        }

        public static void registerSerializer(RegisterEvent.RegisterHelper<RecipeSerializer<?>> helper) {
            helper.register(new ResourceLocation(CombineRecipe.LOCATION), CombineRecipe.SERIALIZER);
            helper.register(TierRecipe.Serializer.LOCATION, TierRecipe.SERIALIZER);
            helper.register(ReservoirRecipe.Serializer.LOCATION, ReservoirRecipe.SERIALIZER);
            CraftingHelper.register(new FluidTankConditions.TankConfigCondition().serializer);
            CraftingHelper.register(new FluidTankConditions.PipeConfigCondition().serializer);
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

        @SubscribeEvent
        public static void registerCreativeTab(CreativeModeTabEvent.Register event) {
            event.registerCreativeModeTab(new ResourceLocation(FluidTank.modID, "tab"), ModObjects::createTab);
        }
    }

}
