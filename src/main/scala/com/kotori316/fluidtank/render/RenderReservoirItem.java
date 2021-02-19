package com.kotori316.fluidtank.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.fluidtank.items.ReservoirItem;

@OnlyIn(Dist.CLIENT)
public class RenderReservoirItem extends ItemStackTileEntityRenderer {
    private static final float d = 1f / 16f;

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType cameraType, MatrixStack matrixStack,
                               IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if (stack.getItem() instanceof ReservoirItem) {
            IBakedModel itemModel = Minecraft.getInstance().getItemRenderer().getItemModelMesher().getItemModel(stack);
            if (itemModel instanceof ModelWrapper) {
                matrixStack.push();
                matrixStack.translate(0.5D, 0.5D, 0.5D);
                IBakedModel original = ((ModelWrapper) itemModel).getOriginalModel();
                Minecraft.getInstance().getItemRenderer().renderItem(stack, cameraType,
                    false, matrixStack, buffer, combinedLight, combinedOverlay, original);
                matrixStack.pop();

                if (cameraType == ItemCameraTransforms.TransformType.GUI) {
                    matrixStack.push();
                    stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                        .map(h -> Pair.of(h.getFluidInTank(0), h.getTankCapacity(0)))
                        .filter(p -> !p.getLeft().isEmpty() && p.getLeft().getAmount() > 0)
                        .ifPresent(p -> renderFluid(p.getLeft(), p.getRight(), matrixStack, buffer, combinedLight, combinedOverlay));
                    matrixStack.pop();
                }
            }
        }
    }

    private static void renderFluid(FluidStack stack, int capacity, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay) {
        ResourceLocation textureName = stack.getFluid().getAttributes().getStillTexture(stack);
        TextureAtlasSprite texture = Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(textureName);
        int color = stack.getFluid().getAttributes().getColor(stack);
        Wrapper wrapper = new Wrapper(buffer.getBuffer(RenderType.getTranslucent()));
        int alpha = (color >> 24 & 0xFF) > 0 ? color >> 24 & 0xFF : 0xFF;
        int red = color >> 16 & 0xFF, green = color >> 8 & 0xFF, blue = color & 0xFF;

        float height = Math.max(0.01f, (float) stack.getAmount() / capacity);
        float maxV = texture.getMaxV(); //texture.getInterpolatedV(height / d);
        float minV = texture.getMinV(), maxU = texture.getMaxU(), minU = texture.getMinU();
        wrapper.pos(2 * d, height, 0.5, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightMap(light, overlay).endVertex();
        wrapper.pos(2 * d, 0, 0.5, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightMap(light, overlay).endVertex();
        wrapper.pos(14 * d, 0, 0.5, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightMap(light, overlay).endVertex();
        wrapper.pos(14 * d, height, 0.5, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightMap(light, overlay).endVertex();
    }

}
