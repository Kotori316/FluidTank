package com.kotori316.fluidtank.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.tiles.ItemModelTank;
import com.kotori316.fluidtank.tiles.RenderItemTank;
import com.kotori316.fluidtank.tiles.RenderTank;
import com.kotori316.fluidtank.tiles.TileTank;

@SideOnly(Side.CLIENT)
public class ClientProxy extends SideProxy {

    private static final RenderItemTank RENDER_ITEM_TANK = new RenderItemTank();
    public static final RenderTank RENDER_TANK = new RenderTank();
    private static final ItemModelTank MODEL_TANK = new ItemModelTank();
    public static final ModelResourceLocation MODEL_RESOURCE_LOCATION =
        new ModelResourceLocation(FluidTank.modID + ":fluidtankitem", "inventory");

    @Override
    public World getWorld(INetHandler handler) {
        return Minecraft.getMinecraft().world;
    }

    @Override
    public void registerTESR() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, RENDER_TANK);
        FluidTank.BLOCK_TANKS.stream().map(BlockTank::itemBlock).forEach(itemBlockTank ->
            itemBlockTank.setTileEntityItemStackRenderer(RENDER_ITEM_TANK));
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        FluidTank.BLOCK_TANKS.stream().map(BlockTank::itemBlock).forEach(item ->
            ModelLoader.setCustomMeshDefinition(item, stack -> MODEL_RESOURCE_LOCATION)
        );
    }

    @SubscribeEvent
    public void onBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(MODEL_RESOURCE_LOCATION, MODEL_TANK);
        FluidTank.BLOCK_TANKS.stream().map(BlockTank::itemBlock).flatMap(ItemBlockTank::itemStream).forEach(t -> {
            ModelResourceLocation modelLocation = t._1.getModelResouceLocation(t._2);
            IBakedModel model = event.getModelManager().getModel(modelLocation);
            event.getModelRegistry().putObject(modelLocation, model);
        });
    }
}
