package com.kotori316.fluidtank.transport;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;

public class ItemPipeBlock extends PipeBlock {
    @Nonnull
    @Override
    protected String getRegName() {
        return "item_pipe";
    }

    @Override
    protected boolean isHandler(BlockGetter level, BlockPos pos, EnumProperty<Connection> property) {
        Direction d = FACING_TO_PROPERTY_MAP.inverse().get(property);
        BlockPos maybeTilePos = pos.relative(d);
        BlockEntity maybeTile = level.getBlockEntity(maybeTilePos);
        if (maybeTile != null) {
            LazyOptional<IItemHandler> cap = maybeTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
            if (cap.isPresent()) return true;
        }
        // Technique to get world instance.
        BlockEntity shouldBePipe = level.getBlockEntity(pos);
        if (shouldBePipe != null) {
            Container maybeInv = HopperBlockEntity.getContainerAt(Objects.requireNonNull(shouldBePipe.getLevel()), maybeTilePos);
            return maybeInv != null;
        }
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModObjects.ITEM_PIPE_TYPE().create(pos, state);
    }

    @Nonnull
    @Override
    protected Connection getConnection(Direction direction, @Nonnull BlockEntity entity) {
        if (entity instanceof Container ||
            entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent())
            return Connection.CONNECTED;
        else
            return Connection.NO_CONNECTION;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : Utils.checkType(type, ModObjects.ITEM_PIPE_TYPE(), (l, p, s, pipe) -> pipe.tick());
    }
}
