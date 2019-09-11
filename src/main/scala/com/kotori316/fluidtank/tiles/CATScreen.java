package com.kotori316.fluidtank.tiles;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;

@OnlyIn(Dist.CLIENT)
public class CATScreen extends GuiContainer {

    private final CATTile catTile;
    private final ITextComponent title;
    private final static ResourceLocation resourceLocation = new ResourceLocation(FluidTank.modID, "textures/gui/cat.png");

    public CATScreen(CATContainer screenContainer, InventoryPlayer inv, ITextComponent titleIn) {
        super(screenContainer);
        catTile = screenContainer.catTile;
        this.title = titleIn;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(resourceLocation);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        String s = this.title.getFormattedText();
        int x = this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2;
        this.fontRenderer.drawString(s, x, 6, 0x404040);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
        List<FluidAmount> stacks = catTile.fluidCache;
        for (int i = 0; i < stacks.size(); i++) {
            FluidAmount a = stacks.get(i);
            this.fontRenderer.drawString(a.toString(), 8, 16 + 10 * i, 0x404040);
        }
    }

    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        this.drawDefaultBackground();
        super.render(p_render_1_, p_render_2_, p_render_3_);
        this.renderHoveredToolTip(p_render_1_, p_render_2_);
    }

    public static GuiScreen create(FMLPlayMessages.OpenContainer packet) {
        BlockPos pos = packet.getAdditionalData().readBlockPos();
        TileEntity entity = Minecraft.getInstance().world.getTileEntity(pos);
        EntityPlayerSP player = Minecraft.getInstance().player;
        if (entity instanceof CATTile) {
            return new CATScreen(new CATContainer(0, player, pos), player.inventory, ModObjects.blockCat().getNameTextComponent());
        }
        return null;
    }
}
