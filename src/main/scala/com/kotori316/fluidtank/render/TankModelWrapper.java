package com.kotori316.fluidtank.render;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

class TankModelWrapper extends BakedModelWrapper<IBakedModel> {
    public TankModelWrapper(IBakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }
}
