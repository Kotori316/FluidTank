package com.kotori316.fluidtank;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;

import com.kotori316.fluidtank.render.RenderTank;
import com.kotori316.fluidtank.tank.TileTankCreative;

@SuppressWarnings("unused")
public class ModTankClientInit implements ClientModInitializer {

    @SuppressWarnings("unchecked")
    @Override
    public void onInitializeClient() {
        System.out.println(ModTank.modID + " is called client init.");
        ModTank.Entries.ALL_TANK_BLOCKS.forEach(b -> BlockRenderLayerMap.INSTANCE.putBlock(b, RenderLayer.getCutoutMipped()));
        BlockEntityRendererRegistry.INSTANCE.register(ModTank.Entries.TANK_BLOCK_ENTITY_TYPE, RenderTank::new);
        BlockEntityRendererRegistry.INSTANCE.register(ModTank.Entries.CREATIVE_BLOCK_ENTITY_TYPE, d -> (BlockEntityRenderer<TileTankCreative>) ((BlockEntityRenderer<?>) new RenderTank(d)));
    }
}
