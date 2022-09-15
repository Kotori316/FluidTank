package com.kotori316.fluidtank.render;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.fluids.VariantUtil;

@SuppressWarnings("UnstableApiUsage")
final class RenderResourceHelper {
    static TextureAtlasSprite getSprite(FluidAmount fluid) {
        return FluidVariantRendering.getSprite(VariantUtil.convert(fluid));
    }

    static int getColor(FluidAmount fluid) {
        return FluidVariantRendering.getColor(VariantUtil.convert(fluid));
    }

    static int getColorWithPos(FluidAmount fluid, @Nullable BlockAndTintGetter view, BlockPos pos) {
        return FluidVariantRendering.getColor(VariantUtil.convert(fluid), view, pos);
    }

    static int getLuminance(FluidAmount fluid) {
        return FluidVariantAttributes.getLuminance(VariantUtil.convert(fluid));
    }
}
