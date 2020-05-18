package com.kotori316.fluidtank.transport;

import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.kotori316.fluidtank.ModObjects;

public class FluidPipeBlock extends PipeBlock {
    @Override
    protected String getRegName() {
        return "pipe";
    }

    @Override
    protected boolean isHandler(IBlockReader world, BlockPos pos, EnumProperty<Connection> property) {
        return isFluidHandler(world, pos, property);
    }

    @Override
    @Nonnull
    protected Connection getConnection(Direction direction, @Nonnull TileEntity entity) {
        LazyOptional<IFluidHandler> capability = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
        if (capability.isPresent())
            if (capability.map(f -> f.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.SIMULATE)).orElse(0) >= 4000)
                return Connection.OUTPUT;
            else
                return Connection.CONNECTED;
        else
            return Connection.NO_CONNECTION;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModObjects.FLUID_PIPE_TYPE().create();
    }

    private static boolean isFluidHandler(IBlockReader w, BlockPos pipePos, EnumProperty<PipeBlock.Connection> p) {
        Direction d = FACING_TO_PROPERTY_MAP.inverse().get(p);
        return isFluidHandler(w, pipePos.offset(d), d);
    }

    public static boolean isFluidHandler(IBlockReader world, BlockPos pos, Direction direction) {
        TileEntity t = world.getTileEntity(pos);
        if (t != null && t.getWorld() != null)
            return FluidUtil.getFluidHandler(t.getWorld(), pos, direction.getOpposite()).isPresent();
        else return false;
    }
}
