package com.kotori316.fluidtank.transport;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;

public class FluidPipeBlock extends PipeBlock {
    @Override
    protected String getRegName() {
        return "pipe";
    }

    @Override
    protected boolean isHandler(BlockGetter level, BlockPos pos, EnumProperty<Connection> property) {
        return isFluidHandler(level, pos, property);
    }

    @Override
    @NotNull
    protected Connection getConnection(Direction direction, @NotNull BlockEntity entity) {
        LazyOptional<IFluidHandler> capability = entity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite());
        if (capability.isPresent())
            if (capability.map(f -> f.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.SIMULATE)).orElse(0) >= 4000)
                return Connection.OUTPUT;
            else
                return Connection.CONNECTED;
        else
            return Connection.NO_CONNECTION;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModObjects.FLUID_PIPE_TYPE().create(pos, state);
    }

    private static boolean isFluidHandler(BlockGetter w, BlockPos pipePos, EnumProperty<PipeBlock.Connection> p) {
        Direction d = Objects.requireNonNull(FACING_TO_PROPERTY_MAP.inverse().get(p));
        return isFluidHandler(w, pipePos.relative(d), d);
    }

    public static boolean isFluidHandler(BlockGetter world, BlockPos pos, Direction direction) {
        BlockEntity t = world.getBlockEntity(pos);
        if (t != null && t.getLevel() != null)
            return FluidUtil.getFluidHandler(t.getLevel(), pos, direction.getOpposite()).isPresent();
        else return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : Utils.checkType(type, ModObjects.FLUID_PIPE_TYPE(), (l, p, s, pipe) -> pipe.tick());
    }
}
