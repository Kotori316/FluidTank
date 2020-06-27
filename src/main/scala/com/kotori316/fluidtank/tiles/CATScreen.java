package com.kotori316.fluidtank.tiles;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
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
    protected void func_230450_a_(MatrixStack matrixStack, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        int i = (this.field_230708_k_ - this.xSize) / 2; // width
        int j = (this.field_230709_l_ - this.ySize) / 2; // height
        this.func_238474_b_(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
    }

    @Override
    @SuppressWarnings("NoTranslation")
    protected void func_230451_b_(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.func_230451_b_(matrixStack, mouseX, mouseY);
//        String s = this.func_231171_q_().getString(); // getTitle
//        int x = this.xSize / 2 - this.field_230712_o_.getStringWidth(s) / 2; // font
//        this.field_230712_o_.func_238405_a_(matrixStack, s, x, 6, 0x404040);
//        this.field_230712_o_.func_238405_a_(matrixStack, I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
        List<FluidAmount> stacks = catTile.fluidCache;
        for (int i = 0; i < stacks.size(); i++) {
            FluidAmount a = stacks.get(i);
            this.field_230712_o_.func_238421_b_(matrixStack, a.toString(), 8, 16 + 10 * i, 0x404040);
        }
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // render
        this.func_230446_a_(matrixStack); // back ground
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks); // super.render
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
    }
}
