package com.kotori316.fluidtank;

import java.util.stream.Stream;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import com.kotori316.fluidtank.packet.PacketHandler;
import com.kotori316.fluidtank.render.RenderItemTank;
import com.kotori316.fluidtank.render.RenderTank;
import com.kotori316.fluidtank.tank.TileTankCreative;

@SuppressWarnings("unused")
public class ModTankClientInit implements ClientModInitializer {

    public static final Material STILL_IDENTIFIER = new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(ModTank.modID, "blocks/milk_still"));
    public static final Material FLOW_IDENTIFIER = new Material(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(ModTank.modID, "blocks/milk_flow"));
    private static final RenderItemTank RENDER_ITEM_TANK = new RenderItemTank();

    @SuppressWarnings("unchecked")
    @Override
    public void onInitializeClient() {
        ModTank.LOGGER.info("Client init is called. {} ", ModTank.modID);
        PacketHandler.Client.initClient();
        ModTank.Entries.ALL_TANK_BLOCKS.forEach(b -> BlockRenderLayerMap.INSTANCE.putBlock(b, RenderType.cutoutMipped()));
        BlockEntityRendererRegistry.register(ModTank.Entries.TANK_BLOCK_ENTITY_TYPE, RenderTank::new);
        BlockEntityRendererRegistry.register(ModTank.Entries.CREATIVE_BLOCK_ENTITY_TYPE, d ->
            (BlockEntityRenderer<TileTankCreative>) ((BlockEntityRenderer<?>) new RenderTank(d)));
        Stream.of(STILL_IDENTIFIER, FLOW_IDENTIFIER).forEach(si ->
            ClientSpriteRegistryCallback.event(si.atlasLocation()).register((atlasTexture, registry) -> registry.register(si.texture())));
        FluidRenderHandlerRegistry.INSTANCE.register(ModTank.Entries.MILK_FLUID, (view, pos, state) -> new TextureAtlasSprite[]{STILL_IDENTIFIER.sprite(), FLOW_IDENTIFIER.sprite()});
        ModTank.Entries.ALL_TANK_BLOCKS.forEach(b -> BuiltinItemRendererRegistry.INSTANCE.register(b, RENDER_ITEM_TANK));
        ModTank.LOGGER.info("Client init is finished. {} ", ModTank.modID);
    }
}
