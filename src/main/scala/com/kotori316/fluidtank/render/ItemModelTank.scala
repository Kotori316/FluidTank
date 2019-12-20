package com.kotori316.fluidtank.render

import java.util.{Collections, Random}

import net.minecraft.block.BlockState
import net.minecraft.client.renderer.model.{IBakedModel, ItemOverrideList}
import net.minecraft.util.Direction
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class ItemModelTank extends IBakedModel {

  override def isGui3d = false

  override def getOverrides = ItemOverrideList.EMPTY

  override def isAmbientOcclusion = false

  override def getQuads(state: BlockState, side: Direction, rand: Random) = Collections.emptyList()

  override def isBuiltInRenderer = true

  override def getParticleTexture = null

}
