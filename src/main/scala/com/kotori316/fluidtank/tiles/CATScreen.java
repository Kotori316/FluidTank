package com.kotori316.fluidtank.tiles;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.fluids.FluidAmount;

public class CATScreen extends AbstractContainerScreen<CATContainer> {

    private final CATTile catTile;
    private final static ResourceLocation resourceLocation = new ResourceLocation(FluidTank.modID, "textures/gui/cat.png");

    public CATScreen(CATContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        catTile = screenContainer.catTile;
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, resourceLocation);
        this.blit(matrices, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        List<FluidAmount> stacks = catTile.fluidCache;
        for (int i = 0; i < stacks.size(); i++) {
            FluidAmount a = stacks.get(i);
            this.font.draw(matrices, a.toString(), 8, 16 + 10 * i, 0x404040);
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }
}
