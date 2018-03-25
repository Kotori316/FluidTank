package com.kotori316.fluidtank;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.packet.PacketHandler;
import com.kotori316.fluidtank.packet.SideProxy;
import com.kotori316.fluidtank.recipes.TankRecipe;
import com.kotori316.fluidtank.tiles.Tiers;
import com.kotori316.fluidtank.tiles.TileTank;

@Mod(name = FluidTank.MOD_NAME, modid = FluidTank.modID, version = "${version}")
public class FluidTank {

    public static final FluidTank instance;
    public static final String MOD_NAME = "FluidTank";
    public static final String modID = "fluidtank";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    @SidedProxy(clientSide = "com.kotori316.fluidtank.packet.ClientProxy", serverSide = "com.kotori316.fluidtank.packet.ServerProxy")
    public static SideProxy proxy;

    public static final List<BlockTank> BLOCK_TANKS =
        Arrays.asList(BlockTank.blockTank1(), BlockTank.blockTank2(), BlockTank.blockTank3(), BlockTank.blockTank4(), BlockTank.blockTank5(), BlockTank.blockTank6());

    static {
        instance = new FluidTank();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(instance);
        proxy.registerTESR();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        PacketHandler.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(BLOCK_TANKS.toArray(new BlockTank[BLOCK_TANKS.size()]));
        TileEntity.register(modID + ":tiletank", TileTank.class);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(BLOCK_TANKS.stream().map(BlockTank::itemBlock).toArray(Item[]::new));
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        List<TankRecipe> tankRecipes = Arrays.asList(
            new TankRecipe(modID + ":tankstone", Tiers.STONE(), "stone"),
            new TankRecipe(modID + ":tankcopper", Tiers.COPPER(), "ingotCopper"),
            new TankRecipe(modID + ":tanktin", Tiers.TIN(), "ingotTin"),
            new TankRecipe(modID + ":tankiron", Tiers.IRON(), "ingotIron"),
            new TankRecipe(modID + ":tankbronze", Tiers.BRONZE(), "ingotBronze"),
            new TankRecipe(modID + ":tanklead", Tiers.LEAD(), "ingotLead"),
            new TankRecipe(modID + ":tanksilver", Tiers.SILVER(), "ingotSilver"),
            new TankRecipe(modID + ":tankgold", Tiers.GOLD(), "ingotGold"),
            new TankRecipe(modID + ":tankdiamond", Tiers.DIAMOND(), "gemDiamond"),
            new TankRecipe(modID + ":tankemerald", Tiers.EMERALD(), "gemEmerald"));
        event.getRegistry().registerAll(tankRecipes.stream().filter(TankRecipe::isValid).toArray(IRecipe[]::new));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        BLOCK_TANKS.stream().map(BlockTank::itemBlock).flatMap(i -> i.itemList().stream()).forEach(t ->
            ModelLoader.setCustomModelResourceLocation(t._1, t._2, t._1.getModelResouceLocation(t._2)));
    }

    @Mod.InstanceFactory
    public static FluidTank getInstance() {
        return instance;
    }

}
