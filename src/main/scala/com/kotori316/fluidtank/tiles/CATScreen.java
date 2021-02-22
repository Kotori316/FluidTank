package com.kotori316.fluidtank.tiles;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.fluids.FluidAmount;

public class CATScreen extends ContainerScreen<CATContainer> {

    private final CATTile catTile;
    private final static ResourceLocation resourceLocation = new ResourceLocation(FluidTank.modID, "textures/gui/cat.png");

    public CATScreen(CATContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        catTile = screenContainer.catTile;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        // background
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        int i = (this.width - this.xSize) / 2; // width
        int j = (this.height - this.ySize) / 2; // height
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        //Foreground
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
//        String s = this.getTitle().getString(); // getTitle
//        int x = this.xSize / 2 - this.font.getStringWidth(s) / 2; // font
//        this.font.func_238405_a_(matrixStack, s, x, 6, 0x404040);
//        this.font.func_238405_a_(matrixStack, I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
        List<FluidAmount> stacks = catTile.fluidCache;
        for (int i = 0; i < stacks.size(); i++) {
            FluidAmount a = stacks.get(i);
            this.font.drawString(matrixStack, a.toString(), 8, 16 + 10 * i, 0x404040);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // render
        this.renderBackground(matrixStack); // back ground
        super.render(matrixStack, mouseX, mouseY, partialTicks); // super.render
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY); // render tooltip
    }
}
