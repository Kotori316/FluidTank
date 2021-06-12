package com.kotori316.fluidtank;

import java.util.stream.Stream;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

import com.kotori316.fluidtank.render.RenderItemTank;
import com.kotori316.fluidtank.render.RenderTank;
import com.kotori316.fluidtank.tank.TileTankCreative;

@SuppressWarnings("unused")
public class ModTankClientInit implements ClientModInitializer {

    public static final SpriteIdentifier STILL_IDENTIFIER = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier(ModTank.modID, "blocks/milk_still"));
    public static final SpriteIdentifier FLOW_IDENTIFIER = new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier(ModTank.modID, "blocks/milk_flow"));
    private static final RenderItemTank RENDER_ITEM_TANK = new RenderItemTank();

    @SuppressWarnings("unchecked")
    @Override
    public void onInitializeClient() {
        ModTank.LOGGER.info("Client init is called. {} ", ModTank.modID);
        ModTank.Entries.ALL_TANK_BLOCKS.forEach(b -> BlockRenderLayerMap.INSTANCE.putBlock(b, RenderLayer.getCutoutMipped()));
        BlockEntityRendererRegistry.INSTANCE.register(ModTank.Entries.TANK_BLOCK_ENTITY_TYPE, RenderTank::new);
        BlockEntityRendererRegistry.INSTANCE.register(ModTank.Entries.CREATIVE_BLOCK_ENTITY_TYPE, d ->
            (BlockEntityRenderer<TileTankCreative>) ((BlockEntityRenderer<?>) new RenderTank(d)));
        Stream.of(STILL_IDENTIFIER, FLOW_IDENTIFIER).forEach(si ->
            ClientSpriteRegistryCallback.event(si.getAtlasId()).register((atlasTexture, registry) -> registry.register(si.getTextureId())));
        FluidRenderHandlerRegistry.INSTANCE.register(ModTank.Entries.MILK_FLUID, (view, pos, state) -> new Sprite[]{STILL_IDENTIFIER.getSprite(), FLOW_IDENTIFIER.getSprite()});
        ModTank.Entries.ALL_TANK_BLOCKS.forEach(b -> BuiltinItemRendererRegistry.INSTANCE.register(b, RENDER_ITEM_TANK));
        ModTank.LOGGER.info("Client init is finished. {} ", ModTank.modID);
    }
}
