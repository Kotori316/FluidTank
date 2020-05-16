package com.kotori316.fluidtank.network;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import scala.Option;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.render.ItemModelTank;
import com.kotori316.fluidtank.render.RenderItemTank;
import com.kotori316.fluidtank.render.RenderPipe;
import com.kotori316.fluidtank.render.RenderTank;
import com.kotori316.fluidtank.tiles.CATScreen;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends SideProxy {

    private static final RenderItemTank RENDER_ITEM_TANK = new RenderItemTank();
    //    public static final RenderTank RENDER_TANK = new RenderTank();
    //    public static final RenderPipe RENDER_PIPE = new RenderPipe();
    private static final ItemModelTank MODEL_TANK = new ItemModelTank();
    private static final ModelResourceLocation MESH_MODEL =
        new ModelResourceLocation(FluidTank.modID + ":render.fluidtank.item", "inventory");
    public static TextureAtlasSprite whiteTexture;

    @Override
    public Option<World> getWorld(NetworkEvent.Context context) {
        Optional<World> world = LogicalSidedProvider.CLIENTWORLD.get(context.getDirection().getReceptionSide());
        return fromJava(world);
    }

    @Override
    public Item.Properties getTankProperties() {
        return new Item.Properties().group(ModObjects.CREATIVE_TABS()).setISTER(() -> () -> RENDER_ITEM_TANK);
    }

    @Mod.EventBusSubscriber(modid = FluidTank.modID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientEventHandlers {

        @SubscribeEvent
        public static void registerTESR(FMLClientSetupEvent event) {
            ClientRegistry.bindTileEntityRenderer(ModObjects.TANK_TYPE(), RenderTank::new);
            ClientRegistry.bindTileEntityRenderer(ModObjects.TANK_CREATIVE_TYPE(), RenderTank::new);
            ClientRegistry.bindTileEntityRenderer(ModObjects.FLUID_PIPE_TYPE(), d -> {
                RenderPipe renderPipe = new RenderPipe(d);
                renderPipe.useColor_$eq(!Config.content().enablePipeRainbowRenderer().get());
                return renderPipe;
            });
            ScreenManager.registerFactory(ModObjects.CAT_CONTAINER_TYPE(), CATScreen::new);

            RenderType rendertype = RenderType.getCutoutMipped();
            CollectionConverters.asJava(ModObjects.blockTanks())
                .forEach(tank -> RenderTypeLookup.setRenderLayer(tank, rendertype));
            CollectionConverters.asJava(ModObjects.blockTanksInvisible())
                .forEach(tank -> RenderTypeLookup.setRenderLayer(tank, rendertype));
            RenderTypeLookup.setRenderLayer(ModObjects.blockFluidPipe(), rendertype);
        }

        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
        /*if (!Config.content().enableOldRender().get()) {
            FluidTank.BLOCK_TANKS.stream().map(AbstractTank::itemBlock).forEach(item ->
                ModelLoader.setCustomMeshDefinition(item, stack -> MESH_MODEL)
            );
        }*/
        }

        @SubscribeEvent
        public static void onBake(ModelBakeEvent event) {
            event.getModelRegistry().put(MESH_MODEL, MODEL_TANK);
            CollectionConverters.asJava(ModObjects.blockTanks()).stream().map(BlockTank::itemBlock).forEach(itemBlockTank -> {
                ModelResourceLocation modelLocation = new ModelResourceLocation(Objects.toString(itemBlockTank.getRegistryName()), "inventory");
//            IBakedModel model = event.getModelManager().getModel(modelLocation);
                event.getModelRegistry().put(modelLocation, MODEL_TANK);
            });
        }

        @SubscribeEvent
        public static void registerTexture(TextureStitchEvent.Pre event) {
            if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
                event.addSprite(new ResourceLocation(FluidTank.modID, "blocks/white"));
            }
        }

        @SubscribeEvent
        public static void putTexture(TextureStitchEvent.Post event) {
            if (event.getMap().getTextureLocation().equals(PlayerContainer.LOCATION_BLOCKS_TEXTURE)) {
                whiteTexture = event.getMap().getSprite(new ResourceLocation(FluidTank.modID, "blocks/white"));
            }
        }
    }
}
