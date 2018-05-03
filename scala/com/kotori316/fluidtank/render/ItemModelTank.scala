package com.kotori316.fluidtank.render

import java.util.Collections
import javax.vecmath.{Matrix4f, Vector3f}

import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType
import net.minecraft.client.renderer.block.model.{IBakedModel, ItemOverrideList}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.model.TRSRTransformation
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.apache.commons.lang3.tuple.Pair

@SideOnly(Side.CLIENT)
class ItemModelTank extends IBakedModel {

    val transMap = Map(
        TransformType.GUI -> new TRSRTransformation(null, fromDegrees(30, 135, 0), vec(0.625), null),
        TransformType.FIXED -> new TRSRTransformation(null, fromDegrees(0, 90, 0), vec(0.5), null),
        TransformType.FIRST_PERSON_LEFT_HAND -> new TRSRTransformation(null, fromDegrees(0, 45, 0), vec(0.4), null),
        TransformType.FIRST_PERSON_RIGHT_HAND -> new TRSRTransformation(null, fromDegrees(0, 225, 0), vec(0.4), null),
        TransformType.THIRD_PERSON_LEFT_HAND -> new TRSRTransformation(new Vector3f(0, 2.5f / 16, 0), fromDegrees(75, 45, 0), new Vector3f(-.375f, .375f, .375f), null),
        TransformType.THIRD_PERSON_RIGHT_HAND -> new TRSRTransformation(new Vector3f(0, 2.5f / 16, 0), fromDegrees(75, 45, 0), vec(0.375), null),
        TransformType.GROUND -> new TRSRTransformation(new Vector3f(0, 3f / 16, 0), null, vec(0.25), null)).map {
        case (t, trans) => (t, trans)
    }

    private def fromDegrees(x: Int, y: Int, z: Int) = TRSRTransformation.quatFromXYZDegrees(new Vector3f(x.toFloat, y.toFloat, z.toFloat))

    private def vec(t: Double) = new Vector3f(t.toFloat, t.toFloat, t.toFloat)

    override def isGui3d = false

    override def getOverrides = ItemOverrideList.NONE

    override def isAmbientOcclusion = false

    override def getQuads(state: IBlockState, side: EnumFacing, rand: Long) = Collections.emptyList()

    override def isBuiltInRenderer = true

    override def getParticleTexture = null

    override def handlePerspective(cameraTransformType: TransformType): Pair[_ <: IBakedModel, Matrix4f] = {
        //        PerspectiveMapWrapper.handlePerspective(this, transMap, cameraTransformType)
        val tr = transMap.getOrElse(cameraTransformType, TRSRTransformation.identity())
        Pair.of(this, if (!tr.isIdentity) tr.getMatrix else null)
    }
}
