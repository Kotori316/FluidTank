package com.kotori316.fluidtank.render

import net.minecraft.client.resources.model.BakedModel
import net.minecraftforge.client.model.BakedModelWrapper

class TankModelWrapper(originalModel: BakedModel) extends BakedModelWrapper[BakedModel](originalModel) {
  override def isCustomRenderer = false
}