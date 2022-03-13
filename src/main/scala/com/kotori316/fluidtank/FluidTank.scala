package com.kotori316.fluidtank

import com.kotori316.fluidtank.integration.ae2.TankAE2Plugin
import com.kotori316.fluidtank.integration.top.FluidTankTOPPlugin
import com.kotori316.fluidtank.network.{PacketHandler, SideProxy}
import com.kotori316.fluidtank.recipes.{CombineRecipe, FluidTankConditions, FluidTankDataProvider, ReservoirRecipe, TagCondition, TierRecipe}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.common.ForgeMod
import net.minecraftforge.common.crafting.CraftingHelper
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent
import org.apache.logging.log4j.Logger

@Mod(FluidTank.modID)
object FluidTank {
  final val MOD_NAME = "FluidTank"
  final val modID = "fluidtank"
  final val LOGGER: Logger = Utils.getLogger(MOD_NAME)
  final val proxy: SideProxy = SideProxy.get

  ModLoadingContext.get.registerConfig(ModConfig.Type.COMMON, Config.sync())
  ForgeMod.enableMilkFluid()
  FMLJavaModLoadingContext.get.getModEventBus.register(FluidTank.Register)
  FMLJavaModLoadingContext.get.getModEventBus.register(FluidTank.proxy)

  object Register {
    //noinspection ScalaUnusedSymbol
    @SubscribeEvent
    def init(event: FMLCommonSetupEvent): Unit = {
      PacketHandler.init()
      FluidTankTOPPlugin.sendIMC.apply(modID)
      TankAE2Plugin.onAPIAvailable()
    }

    @SubscribeEvent
    def registerBlocks(event: RegistryEvent.Register[Block]): Unit = {
      ModObjects.blockTanks.foreach(event.getRegistry.register)
      event.getRegistry.register(ModObjects.blockCat)
      event.getRegistry.register(ModObjects.blockFluidPipe)
      event.getRegistry.register(ModObjects.blockItemPipe)
      event.getRegistry.register(ModObjects.blockSource)
    }

    @SubscribeEvent def registerItems(event: RegistryEvent.Register[Item]): Unit = {
      ModObjects.blockTanks.map(_.itemBlock).foreach(event.getRegistry.register)
      event.getRegistry.register(ModObjects.blockCat.itemBlock)
      event.getRegistry.register(ModObjects.blockFluidPipe.itemBlock)
      event.getRegistry.register(ModObjects.blockItemPipe.itemBlock)
      event.getRegistry.register(ModObjects.blockSource.itemBlock)
      ModObjects.itemReservoirs.foreach(event.getRegistry.register)
    }

    @SubscribeEvent
    def registerTiles(event: RegistryEvent.Register[BlockEntityType[_]]): Unit = {
      ModObjects.getTileTypes.foreach(event.getRegistry.register)
    }

    @SubscribeEvent
    def registerSerializer(event: RegistryEvent.Register[RecipeSerializer[_]]): Unit = {
      event.getRegistry.register(CombineRecipe.SERIALIZER.setRegistryName(new ResourceLocation(CombineRecipe.LOCATION)))
      event.getRegistry.register(TierRecipe.SERIALIZER)
      event.getRegistry.register(ReservoirRecipe.SERIALIZER)
      CraftingHelper.register(new FluidTankConditions.ConfigCondition().serializer)
      CraftingHelper.register(new FluidTankConditions.EasyCondition().serializer)
      CraftingHelper.register(TagCondition.SERIALIZER)
    }

    @SubscribeEvent
    def registerContainerType(event: RegistryEvent.Register[MenuType[_]]): Unit = {
      event.getRegistry.register(ModObjects.CAT_CONTAINER_TYPE)
    }

    @SubscribeEvent
    def gatherData(event: GatherDataEvent): Unit = {
      FluidTankDataProvider.gatherData(event)
    }
  }
}
