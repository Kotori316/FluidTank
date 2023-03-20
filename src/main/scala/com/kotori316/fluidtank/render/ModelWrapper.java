package com.kotori316.fluidtank.render;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.BakedModelWrapper;

public class ModelWrapper extends BakedModelWrapper<BakedModel> {
    public ModelWrapper(BakedModel originalModel) {
        super(Objects.requireNonNull(originalModel));
    }

    public BakedModel getOriginalModel() {
        return this.originalModel;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack mat, boolean applyLeftHandTransform) {
        return this;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }
}
