package com.kotori316.fluidtank.tiles;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.FluidTank;

public class CATScreen extends ContainerScreen<CATContainer> {

    private final CATTile catTile;
    private final static ResourceLocation resourceLocation = new ResourceLocation(FluidTank.modID, "textures/gui/cat.png");

    public CATScreen(CATContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        catTile = screenContainer.catTile;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(i, j, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        String s = this.getTitle().getFormattedText();
        int x = this.xSize / 2 - this.font.getStringWidth(s) / 2;
        this.font.drawString(s, x, 6, 0x404040);
        this.font.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
        List<FluidAmount> stacks = catTile.fluidCache;
        for (int i = 0; i < stacks.size(); i++) {
            FluidAmount a = stacks.get(i);
            this.font.drawString(a.toString(), 8, 16 + 10 * i, 0x404040);
        }
    }

    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        renderBackground();
        super.render(p_render_1_, p_render_2_, p_render_3_);
        this.renderHoveredToolTip(p_render_1_, p_render_2_);
    }
}
