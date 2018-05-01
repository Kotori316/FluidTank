package com.kotori316.fluidtank.tiles

import java.util.Collections

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.{IBakedModel, ItemOverrideList}
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class ItemModelTank extends IBakedModel {

    override def isGui3d = false

    override def getOverrides = ItemOverrideList.NONE

    override def isAmbientOcclusion = false

    override def getQuads(state: IBlockState, side: EnumFacing, rand: Long) = Collections.emptyList()

    override def isBuiltInRenderer = true

    override def getParticleTexture = null
}
