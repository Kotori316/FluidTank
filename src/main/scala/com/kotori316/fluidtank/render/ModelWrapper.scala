package com.kotori316.fluidtank.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.client.resources.model.BakedModel
import net.minecraftforge.client.model.BakedModelWrapper

class ModelWrapper(o: BakedModel) extends BakedModelWrapper[BakedModel](o) {
  def getOriginalModel: BakedModel = this.originalModel

  override def handlePerspective(cameraTransformType: ItemTransforms.TransformType, mat: PoseStack): BakedModel = this

  override def isCustomRenderer = true
}
