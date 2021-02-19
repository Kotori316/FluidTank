package com.kotori316.fluidtank.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraftforge.client.model.BakedModelWrapper;

public class ModelWrapper extends BakedModelWrapper<IBakedModel> {
    public ModelWrapper(IBakedModel originalModel) {
        super(originalModel);
    }

    public IBakedModel getOriginalModel() {
        return this.originalModel;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
        return this;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }
}
