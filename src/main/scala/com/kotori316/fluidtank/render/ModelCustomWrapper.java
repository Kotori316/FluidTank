package com.kotori316.fluidtank.render;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.resources.model.BakedModel;

public class ModelCustomWrapper extends ForwardingBakedModel {
    public ModelCustomWrapper(BakedModel originalModel) {
        this.wrapped = originalModel;
    }

    public BakedModel getOriginalModel() {
        return this.wrapped;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }
}
