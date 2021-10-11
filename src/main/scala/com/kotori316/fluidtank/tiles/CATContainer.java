package com.kotori316.fluidtank.tiles;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeContainerType;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.network.FluidCacheMessage;
import com.kotori316.fluidtank.network.PacketHandler;

public class CATContainer extends AbstractContainerMenu {
    public static final String GUI_ID = FluidTank.modID + ":gui_chest_as_tank";
    public final CATTile catTile;

    /**
     * Only for internal use. Use instance in {@link com.kotori316.fluidtank.ModObjects}.
     */
    public static MenuType<CATContainer> makeType() {
        MenuType<CATContainer> t = IForgeContainerType.create((windowId1, inv, data) -> new CATContainer(windowId1, inv.player, data.readBlockPos()));
        t.setRegistryName(GUI_ID);
        return t;
    }

    public CATContainer(int id, Player player, BlockPos pos) {
        super(ModObjects.CAT_CONTAINER_TYPE(), id);
        catTile = (CATTile) player.getCommandSenderWorld().getBlockEntity(pos);
        Objects.requireNonNull(catTile);

        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.getInventory(), v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.getInventory(), vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return catTile.getBlockPos().distSqr(player.position(), true) < 64;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        PacketHandler.sendToClient(new FluidCacheMessage(catTile), catTile.getLevel());
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }
}
