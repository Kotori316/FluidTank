package com.kotori316.fluidtank.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.items.ReservoirItem;
import com.kotori316.fluidtank.items.TankItemFluidHandler;

@Environment(EnvType.CLIENT)
public class RenderReservoirItem implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final float d = 1f / 16f;

    public RenderReservoirItem() {
        super();
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext cameraType, PoseStack matrixStack,
                       MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (stack.getItem() instanceof ReservoirItem reservoirItem) {
            BakedModel itemModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
            if (itemModel instanceof ModelCustomWrapper) {
                matrixStack.pushPose();
                matrixStack.translate(0.5D, 0.5D, 0.5D);
                BakedModel original = ((ModelCustomWrapper) itemModel).getOriginalModel();
                Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE,
                    false, matrixStack, buffer, combinedLight, combinedOverlay, original);
                matrixStack.popPose();

                if (cameraType == ItemDisplayContext.GUI) {
                    matrixStack.pushPose();
                    var handler = new TankItemFluidHandler(reservoirItem.tier(), stack);
                    var fluid = handler.getFluid();
                    var capacity = handler.getCapacity();
                    if (fluid.nonEmpty()) {
                        renderFluid(fluid, capacity, matrixStack, buffer, combinedLight, combinedOverlay);
                    }
                    matrixStack.popPose();
                }
            }
        }
    }

    private static void renderFluid(FluidAmount stack, long capacity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        TextureAtlasSprite texture = RenderResourceHelper.getSprite(stack);
        if (texture == null)
            return;
        int color = RenderResourceHelper.getColor(stack);
        Wrapper wrapper = new Wrapper(buffer.getBuffer(RenderType.translucent()));
        int alpha = (color >> 24 & 0xFF) > 0 ? color >> 24 & 0xFF : 0xFF;
        int red = color >> 16 & 0xFF, green = color >> 8 & 0xFF, blue = color & 0xFF;

        float height = Math.max(0.01f, (float) stack.amount() / capacity);
        float maxV = texture.getV1(); //texture.getV(height / d);
        float minV = texture.getV0();
        float maxU = texture.getU1();
        float minU = texture.getU0();
        wrapper.pos(2 * d, height, 0.5, matrixStack).color(red, green, blue, alpha).tex(minU, maxV).lightMap(light, overlay).endVertex();
        wrapper.pos(2 * d, 0, 0.5, matrixStack).color(red, green, blue, alpha).tex(minU, minV).lightMap(light, overlay).endVertex();
        wrapper.pos(14 * d, 0, 0.5, matrixStack).color(red, green, blue, alpha).tex(maxU, minV).lightMap(light, overlay).endVertex();
        wrapper.pos(14 * d, height, 0.5, matrixStack).color(red, green, blue, alpha).tex(maxU, maxV).lightMap(light, overlay).endVertex();
    }

}
