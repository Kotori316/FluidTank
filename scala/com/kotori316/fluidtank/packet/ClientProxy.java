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
import scala.Option;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.blocks.AbstractTank;
import com.kotori316.fluidtank.items.ItemBlockTank;
import com.kotori316.fluidtank.render.ItemModelTank;
import com.kotori316.fluidtank.render.RenderItemTank;
import com.kotori316.fluidtank.render.RenderTank;
import com.kotori316.fluidtank.tiles.TileTank;

@SideOnly(Side.CLIENT)
public class ClientProxy extends SideProxy {

    private static final RenderItemTank RENDER_ITEM_TANK = new RenderItemTank();
    public static final RenderTank RENDER_TANK = new RenderTank();
    private static final ItemModelTank MODEL_TANK = new ItemModelTank();
    private static final ModelResourceLocation MESH_MODEL =
            new ModelResourceLocation(FluidTank.modID + ":fluidtankitem", "inventory");

    @Override
    public Option<World> getWorld(INetHandler handler) {
        return Option.apply(Minecraft.getMinecraft().world);
    }

    @Override
    public void registerTESR() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, RENDER_TANK);
        if (!Config.content().enableOldRender()) {
            FluidTank.BLOCK_TANKS.stream().map(AbstractTank::itemBlock).forEach(itemBlockTank ->
                    itemBlockTank.setTileEntityItemStackRenderer(RENDER_ITEM_TANK));
        }
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        if (!Config.content().enableOldRender()) {
            FluidTank.BLOCK_TANKS.stream().map(AbstractTank::itemBlock).forEach(item ->
                    ModelLoader.setCustomMeshDefinition(item, stack -> MESH_MODEL)
            );
        } else {
            FluidTank.BLOCK_TANKS.stream().map(AbstractTank::itemBlock).flatMap(ItemBlockTank::itemStream).forEach(t ->
                    ModelLoader.setCustomModelResourceLocation(t._1, t._2, t._1.getModelResourceLocation(t._2)));
        }
        FluidTank.BLOCK_TANKS.stream().map(AbstractTank::itemBlock).flatMap(ItemBlockTank::itemStream).forEach(t ->
                ModelLoader.setCustomModelResourceLocation(t._1, t._2 | 8, t._1.getModelResourceLocation(t._2)));
    }

    @SubscribeEvent
    public void onBake(ModelBakeEvent event) {
        if (!Config.content().enableOldRender()) {
            event.getModelRegistry().putObject(MESH_MODEL, MODEL_TANK);
            FluidTank.BLOCK_TANKS.stream().map(AbstractTank::itemBlock).flatMap(ItemBlockTank::itemStream).forEach(t -> {
                ModelResourceLocation modelLocation = t._1.getModelResourceLocation(t._2);
                IBakedModel model = event.getModelManager().getModel(modelLocation);
                event.getModelRegistry().putObject(modelLocation, model);
            });
        }
    }
}
