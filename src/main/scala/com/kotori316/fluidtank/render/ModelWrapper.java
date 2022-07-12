package com.kotori316.fluidtank.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

public class ModelWrapper extends BakedModelWrapper<BakedModel> {
    public ModelWrapper(BakedModel originalModel) {
        super(originalModel);
    }

    public BakedModel getOriginalModel() {
        return this.originalModel;
    }

    @Override
    public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, PoseStack mat, boolean applyLeftHandTransform) {
        return this;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }
}
