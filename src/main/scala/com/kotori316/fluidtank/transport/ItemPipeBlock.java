package com.kotori316.fluidtank.transport;

import java.util.Objects;

import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.kotori316.fluidtank.ModObjects;

public class ItemPipeBlock extends PipeBlock {
    @Nonnull
    @Override
    protected String getRegName() {
        return "item_pipe";
    }

    @Override
    protected boolean isHandler(IBlockReader world, BlockPos pos, EnumProperty<Connection> property) {
        Direction d = FACING_TO_PROPERTY_MAP.inverse().get(property);
        BlockPos maybeTilePos = pos.offset(d);
        TileEntity maybeTile = world.getTileEntity(maybeTilePos);
        if (maybeTile != null) {
            LazyOptional<IItemHandler> cap = maybeTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
            if (cap.isPresent()) return true;
        }
        // Technique to get world instance.
        TileEntity shouldBePipe = world.getTileEntity(pos);
        if (shouldBePipe != null) {
            IInventory maybeInv = HopperTileEntity.getInventoryAtPosition(Objects.requireNonNull(shouldBePipe.getWorld()), maybeTilePos);
            return maybeInv != null;
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModObjects.ITEM_PIPE_TYPE().create();
    }

    @Nonnull
    @Override
    protected Connection getConnection(Direction direction, @Nonnull TileEntity entity) {
        if (entity instanceof IInventory ||
            entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent())
            return Connection.CONNECTED;
        else
            return Connection.NO_CONNECTION;
    }
}
