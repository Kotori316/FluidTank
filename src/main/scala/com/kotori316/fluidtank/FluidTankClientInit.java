package com.kotori316.fluidtank;

import java.util.stream.Stream;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.blocks.FluidSourceBlock;
import com.kotori316.fluidtank.network.PacketHandler;
import com.kotori316.fluidtank.render.RenderItemTank;
import com.kotori316.fluidtank.render.RenderTank;
import com.kotori316.fluidtank.tiles.TileTankCreative;

@SuppressWarnings("unused")
public class FluidTankClientInit implements ClientModInitializer {

    public static final Material STILL_IDENTIFIER = new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(FluidTank.modID, "blocks/milk_still"));
    public static final Material FLOW_IDENTIFIER = new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(FluidTank.modID, "blocks/milk_flow"));
    private static final RenderItemTank RENDER_ITEM_TANK = new RenderItemTank();
    public static final Sprites SPRITES = new Sprites();

    @SuppressWarnings("unchecked")
    @Override
    public void onInitializeClient() {
        FluidTank.LOGGER.info("Client init is called. {} ", FluidTank.modID);
        PacketHandler.Client.initClient();
        ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register(SPRITES);
        var renderType = RenderType.cutoutMipped();
        CollectionConverters.asJava(ModObjects.blockTanks()).forEach(b -> BlockRenderLayerMap.INSTANCE.putBlock(b, renderType));
        BlockRenderLayerMap.INSTANCE.putBlock(ModObjects.blockFluidPipe(), renderType);
        BlockRenderLayerMap.INSTANCE.putBlock(ModObjects.blockItemPipe(), renderType);

        BlockEntityRendererRegistry.register(ModObjects.TANK_TYPE(), RenderTank::new);
        BlockEntityRendererRegistry.register(ModObjects.TANK_CREATIVE_TYPE(), d ->
            (BlockEntityRenderer<TileTankCreative>) ((BlockEntityRenderer<?>) new RenderTank(d)));
        Stream.of(STILL_IDENTIFIER, FLOW_IDENTIFIER).forEach(si ->
            ClientSpriteRegistryCallback.event(si.atlasLocation()).register((atlasTexture, registry) -> registry.register(si.texture())));
        // FluidRenderHandlerRegistry.INSTANCE.register(ModTank.Entries.MILK_FLUID, (view, pos, state) -> new TextureAtlasSprite[]{STILL_IDENTIFIER.sprite(), FLOW_IDENTIFIER.sprite()});
        CollectionConverters.asJava(ModObjects.blockTanks()).forEach(b -> BuiltinItemRendererRegistry.INSTANCE.register(b, RENDER_ITEM_TANK));
        ItemProperties.register(ModObjects.blockSource().itemBlock(),
            new ResourceLocation(FluidTank.modID, "source_cheat"), (stack, world, entity, i) -> FluidSourceBlock.isCheatStack(stack) ? 1f : 0f);
        FluidTank.LOGGER.info("Client init is finished. {} ", FluidTank.modID);
    }

    public static class Sprites implements ClientSpriteRegistryCallback {

        @Override
        public void registerSprites(TextureAtlas atlasTexture, Registry registry) {
            registry.register(new ResourceLocation(FluidTank.modID, "blocks/white"));
        }

        public TextureAtlasSprite getWhite() {
            return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation(FluidTank.modID, "blocks/white"));
        }
    }
}
