package com.kotori316.fluidtank.render;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

@OnlyIn(Dist.CLIENT)
public class ItemModelTank implements IDynamicBakedModel {
    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean func_230044_c_() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    @Deprecated
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    @SuppressWarnings("deprecation")
    public net.minecraft.client.renderer.model.ItemCameraTransforms getItemCameraTransforms() {
        return Constant.transform;
    }

    @SuppressWarnings("deprecation")
    private static class Constant {
        private static final net.minecraft.client.renderer.model.ItemCameraTransforms transform = new net.minecraft.client.renderer.model.ItemCameraTransforms(
            new net.minecraft.client.renderer.model.ItemTransformVec3f(new Vector3f(75, 45, 0), new Vector3f(0, 2.5f / 16, 0), vec(0.375)), //third person_leftIn,
            new net.minecraft.client.renderer.model.ItemTransformVec3f(new Vector3f(75, 45, 0), new Vector3f(0, 2.5f / 16, 0), vec(0.375)), //third person_rightIn,
            new net.minecraft.client.renderer.model.ItemTransformVec3f(new Vector3f(0, 45, 0), net.minecraft.client.renderer.model.ItemTransformVec3f.Deserializer.TRANSLATION_DEFAULT, vec(0.4)), //first person_leftIn,
            new net.minecraft.client.renderer.model.ItemTransformVec3f(new Vector3f(0, 225, 0), net.minecraft.client.renderer.model.ItemTransformVec3f.Deserializer.TRANSLATION_DEFAULT, vec(0.4)), //first person_rightIn,
            net.minecraft.client.renderer.model.ItemTransformVec3f.DEFAULT, //headIn,
            new net.minecraft.client.renderer.model.ItemTransformVec3f(new Vector3f(30, 135, 0), net.minecraft.client.renderer.model.ItemTransformVec3f.Deserializer.TRANSLATION_DEFAULT, vec(0.625)), //guiIn,
            new net.minecraft.client.renderer.model.ItemTransformVec3f(net.minecraft.client.renderer.model.ItemTransformVec3f.Deserializer.ROTATION_DEFAULT, new Vector3f(0, 3f / 16, 0), vec(0.25)), //groundIn,
            new net.minecraft.client.renderer.model.ItemTransformVec3f(new Vector3f(0, 90, 0), net.minecraft.client.renderer.model.ItemTransformVec3f.Deserializer.TRANSLATION_DEFAULT, vec(0.5)) //fixedIn
        );

        private static Vector3f vec(double d) {
            return new Vector3f((float) d, (float) d, (float) d);
        }
    }
}
