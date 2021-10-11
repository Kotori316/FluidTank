package com.kotori316.fluidtank.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.fluidtank.items.ReservoirItem;

@OnlyIn(Dist.CLIENT)
public class RenderReservoirItem extends BlockEntityWithoutLevelRenderer {
    private static final float d = 1f / 16f;

    public RenderReservoirItem() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType cameraType, PoseStack matrixStack,
                             MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (stack.getItem() instanceof ReservoirItem) {
            BakedModel itemModel = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
            if (itemModel instanceof ModelWrapper) {
                matrixStack.pushPose();
                matrixStack.translate(0.5D, 0.5D, 0.5D);
                BakedModel original = ((ModelWrapper) itemModel).getOriginalModel();
                Minecraft.getInstance().getItemRenderer().render(stack, cameraType,
                    false, matrixStack, buffer, combinedLight, combinedOverlay, original);
                matrixStack.popPose();

                if (cameraType == ItemTransforms.TransformType.GUI) {
                    matrixStack.pushPose();
                    stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                        .map(h -> Pair.of(h.getFluidInTank(0), h.getTankCapacity(0)))
                        .filter(p -> !p.getLeft().isEmpty() && p.getLeft().getAmount() > 0)
                        .ifPresent(p -> renderFluid(p.getLeft(), p.getRight(), matrixStack, buffer, combinedLight, combinedOverlay));
                    matrixStack.popPose();
                }
            }
        }
    }

    private static void renderFluid(FluidStack stack, int capacity, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay) {
        ResourceLocation textureName = stack.getFluid().getAttributes().getStillTexture(stack);
        TextureAtlasSprite texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(textureName);
        int color = stack.getFluid().getAttributes().getColor(stack);
        Wrapper wrapper = new Wrapper(buffer.getBuffer(RenderType.translucent()));
        int alpha = (color >> 24 & 0xFF) > 0 ? color >> 24 & 0xFF : 0xFF;
        int red = color >> 16 & 0xFF, green = color >> 8 & 0xFF, blue = color & 0xFF;

        float height = Math.max(0.01f, (float) stack.getAmount() / capacity);
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
