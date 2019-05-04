package com.kotori316.fluidtank;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.collection.JavaConverters;

import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.network.ClientProxy;
import com.kotori316.fluidtank.network.PacketHandler;
import com.kotori316.fluidtank.network.ServerProxy;
import com.kotori316.fluidtank.network.SideProxy;

@Mod(FluidTank.modID)
public class FluidTank {
    public static final String MOD_NAME = "FluidTank";
    public static final String modID = "fluidtank";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final SideProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public FluidTank() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.sync());
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(proxy));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FluidRegistry.enableUniversalBucket();
    }

    @SuppressWarnings("unused")
    public void init(FMLCommonSetupEvent event) {
        PacketHandler.init();
        Config.content().assertion();
    }

    @SuppressWarnings("unused")
    public void clientInit(FMLClientSetupEvent event) {
        proxy.registerTESR();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {
        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            JavaConverters.seqAsJavaList(ModObjects.blockTanks()).forEach(event.getRegistry()::register);
            JavaConverters.seqAsJavaList(ModObjects.blockTanksInvisible()).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            JavaConverters.seqAsJavaList(ModObjects.blockTanks()).stream().map(BlockTank::itemBlock).forEach(event.getRegistry()::register);
            JavaConverters.seqAsJavaList(ModObjects.blockTanksInvisible()).stream().map(BlockTank::itemBlock).forEach(event.getRegistry()::register);
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
            event.getRegistry().register(ModObjects.TANK_TYPE());
            event.getRegistry().register(ModObjects.TANK_NO_DISPLAY_TYPE());
            event.getRegistry().register(ModObjects.TANK_CREATIVE_TYPE());
        }
    }
}
