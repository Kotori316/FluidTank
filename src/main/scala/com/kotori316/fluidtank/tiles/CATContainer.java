package com.kotori316.fluidtank.tiles;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.network.FluidCacheMessage;
import com.kotori316.fluidtank.network.PacketHandler;

public class CATContainer extends Container {
    public static final String GUI_ID = FluidTank.modID + ":gui_chest_as_tank";
    public final CATTile catTile;

    public CATContainer(int id, EntityPlayer player, BlockPos pos) {
//        super(ModObjects.CAT_CONTAINER_TYPE(), id);
        catTile = (CATTile) player.getEntityWorld().getTileEntity(pos);
        Objects.requireNonNull(catTile);

        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSq(catTile.getPos()) < 64;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        PacketHandler.sendToClient(FluidCacheMessage.apply(catTile), catTile.getWorld());
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return ItemStack.EMPTY;
    }
}
