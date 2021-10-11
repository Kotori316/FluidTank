package com.kotori316.fluidtank;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.ForgeMod;
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
import com.kotori316.fluidtank.integration.top.FluidTankTOPPlugin;
import com.kotori316.fluidtank.network.ClientProxy;
import com.kotori316.fluidtank.network.PacketHandler;
import com.kotori316.fluidtank.network.ServerProxy;
import com.kotori316.fluidtank.network.SideProxy;
import com.kotori316.fluidtank.recipes.CombineRecipe;
import com.kotori316.fluidtank.recipes.FluidTankConditions;
import com.kotori316.fluidtank.recipes.ReservoirRecipe;
import com.kotori316.fluidtank.recipes.TagCondition;
import com.kotori316.fluidtank.recipes.TierRecipe;

@Mod(FluidTank.modID)
public class FluidTank {
    public static final String MOD_NAME = "FluidTank";
    public static final String modID = "fluidtank";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final SideProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public FluidTank() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.sync());
        ForgeMod.enableMilkFluid();
    }

    @Mod.EventBusSubscriber(modid = modID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {
        @SubscribeEvent
        public static void init(FMLCommonSetupEvent event) {
            PacketHandler.init();
            FluidTankTOPPlugin.sendIMC().apply(modID);
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
    }

}
