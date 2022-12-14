package com.kotori316.fluidtank.network;

import java.util.Objects;
import java.util.Optional;

import cats.Eval;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.FluidSourceBlock;
import com.kotori316.fluidtank.render.ModelWrapper;
import com.kotori316.fluidtank.render.RenderItemTank;
import com.kotori316.fluidtank.render.RenderPipe;
import com.kotori316.fluidtank.render.RenderReservoirItem;
import com.kotori316.fluidtank.render.RenderTank;
import com.kotori316.fluidtank.tiles.CATScreen;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends SideProxy {

    public static final Eval<RenderItemTank> RENDER_ITEM_TANK = Eval.later(RenderItemTank::new);
    public static final Eval<RenderReservoirItem> RENDER_ITEM_RESERVOIR = Eval.later(RenderReservoirItem::new);
    public static TextureAtlasSprite whiteTexture;

    @Override
    public Optional<Level> getLevel(NetworkEvent.Context context) {
        var serverWorld = Optional.ofNullable(context.getSender()).map(Entity::getCommandSenderWorld);
        return serverWorld.or(() -> LogicalSidedProvider.CLIENTWORLD.get(context.getDirection().getReceptionSide()));
    }

    @Override
    public Item.Properties getTankProperties() {
        return new Item.Properties();
    }

    @Override
    public Item.Properties getReservoirProperties() {
        return new Item.Properties();
    }


    @SubscribeEvent
    public void registerTESR(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(ModObjects.TANK_TYPE(), RenderTank::new);
        BlockEntityRenderers.register(ModObjects.TANK_CREATIVE_TYPE(), RenderTank::new);
        BlockEntityRenderers.register(ModObjects.FLUID_PIPE_TYPE(), ClientProxy::createPipeRenderer);
        BlockEntityRenderers.register(ModObjects.ITEM_PIPE_TYPE(), ClientProxy::createPipeRenderer);
        MenuScreens.register(ModObjects.CAT_CONTAINER_TYPE(), CATScreen::new);

        // Item Properties Override
        ItemProperties.register(ModObjects.blockSource().itemBlock(),
            new ResourceLocation(FluidTank.modID, "source_cheat"), (stack, world, entity, i) -> FluidSourceBlock.isCheatStack(stack) ? 1f : 0f);
    }

    private static RenderPipe createPipeRenderer(BlockEntityRendererProvider.Context d) {
        RenderPipe renderPipe = new RenderPipe();
        renderPipe.useColor_$eq(!Config.content().enablePipeRainbowRenderer().get());
        return renderPipe;
    }

    @SubscribeEvent
    public void registerModels(ModelEvent.RegisterAdditional event) {
    }

    @SubscribeEvent
    public void onBake(ModelEvent.ModifyBakingResult event) {
        CollectionConverters.asJava(ModObjects.itemReservoirs()).stream()
            .map(ForgeRegistries.ITEMS::getKey)
            .filter(Objects::nonNull)
            .map(n -> new ModelResourceLocation(n, "inventory"))
            .forEach(n -> event.getModels().put(n, new ModelWrapper(event.getModels().get(n))));
    }

    @SubscribeEvent
    public void putTexture(TextureStitchEvent.Post event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            whiteTexture = event.getAtlas().getSprite(new ResourceLocation(FluidTank.modID, "block/white"));
        }
    }
}
