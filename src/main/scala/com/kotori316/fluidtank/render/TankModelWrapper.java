package com.kotori316.fluidtank.render;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

class TankModelWrapper extends BakedModelWrapper<BakedModel> {
    public TankModelWrapper(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }
}
