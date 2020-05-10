package com.kotori316.fluidtank;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.blocks.ContentTankSerializer;
import com.kotori316.fluidtank.integration.top.FluidTankTOPPlugin;
import com.kotori316.fluidtank.network.ClientProxy;
import com.kotori316.fluidtank.network.PacketHandler;
import com.kotori316.fluidtank.network.ServerProxy;
import com.kotori316.fluidtank.network.SideProxy;
import com.kotori316.fluidtank.recipes.ConfigCondition;
import com.kotori316.fluidtank.recipes.ConvertInvisibleRecipe;
import com.kotori316.fluidtank.recipes.EasyCondition;
import com.kotori316.fluidtank.recipes.TierRecipe;
import com.kotori316.fluidtank.tiles.CapabilityFluidTank;

@Mod(FluidTank.modID)
public class FluidTank {
    public static final String MOD_NAME = "FluidTank";
    public static final String modID = "fluidtank";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final SideProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public FluidTank() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.sync());
//        MinecraftForge.EVENT_BUS.addListener(BucketEventHandler::onBucketUsed);
//        MinecraftForge.EVENT_BUS.addListener(TileTankNoDisplay::makeConnectionOnChunkLoad);
    }

    @Mod.EventBusSubscriber(modid = modID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {
        @SubscribeEvent
        public static void init(FMLCommonSetupEvent event) {
            PacketHandler.init();
            CapabilityFluidTank.register();
            FluidTankTOPPlugin.sendIMC().apply(modID);
            LootFunctionManager.registerFunction(new ContentTankSerializer());
        }

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            CollectionConverters.asJava(ModObjects.blockTanks()).forEach(event.getRegistry()::register);
            CollectionConverters.asJava(ModObjects.blockTanksInvisible()).forEach(event.getRegistry()::register);
            event.getRegistry().register(ModObjects.blockCat());
            event.getRegistry().register(ModObjects.blockPipe());
            event.getRegistry().register(ModObjects.blockSource());
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            CollectionConverters.asJava(ModObjects.blockTanks()).stream().map(BlockTank::itemBlock).forEach(event.getRegistry()::register);
            CollectionConverters.asJava(ModObjects.blockTanksInvisible()).stream().map(BlockTank::itemBlock).forEach(event.getRegistry()::register);
            event.getRegistry().register(ModObjects.blockCat().itemBlock());
            event.getRegistry().register(ModObjects.blockPipe().itemBlock());
            event.getRegistry().register(ModObjects.blockSource().itemBlock());
        }

        @SubscribeEvent
        public static void registerFluids(RegistryEvent.Register<Fluid> event) {
            event.getRegistry().register(ModObjects.MILK_FLUID());
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
            CollectionConverters.asJava(ModObjects.getTileTypes()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerSerializer(RegistryEvent.Register<IRecipeSerializer<?>> event) {
            event.getRegistry().register(ConvertInvisibleRecipe.SERIALIZER.setRegistryName(new ResourceLocation(ConvertInvisibleRecipe.LOCATION)));
            event.getRegistry().register(TierRecipe.SERIALIZER);
            CraftingHelper.register(new ConfigCondition.Serializer());
            CraftingHelper.register(new EasyCondition.Serializer());
        }

        @SubscribeEvent
        public static void registerContainerType(RegistryEvent.Register<ContainerType<?>> event) {
            event.getRegistry().register(ModObjects.CAT_CONTAINER_TYPE());
        }
    }

}
