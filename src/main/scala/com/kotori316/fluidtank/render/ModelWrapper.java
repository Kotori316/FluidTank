package com.kotori316.fluidtank.render;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.resources.model.BakedModel;

public class ModelWrapper extends ForwardingBakedModel {
    public ModelWrapper(BakedModel originalModel) {
        this.wrapped = originalModel;
    }

    public void setModel(BakedModel newModel) {
        this.wrapped = newModel;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }
}
