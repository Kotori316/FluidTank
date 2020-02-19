package com.kotori316.fluidtank.transport;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableBiMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;

public class PipeBlock extends Block {
    public static final VoxelShape BOX_AABB = VoxelShapes.create(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    public static final VoxelShape North_AABB = VoxelShapes.create(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    public static final VoxelShape South_AABB = VoxelShapes.create(0.25, 0.25, .75, 0.75, 0.75, 1);
    public static final VoxelShape West_AABB = VoxelShapes.create(0, 0.25, 0.25, .25, 0.75, 0.75);
    public static final VoxelShape East_AABB = VoxelShapes.create(.75, 0.25, 0.25, 1, 0.75, 0.75);
    public static final VoxelShape UP_AABB = VoxelShapes.create(0.25, .75, 0.25, 0.75, 1, 0.75);
    public static final VoxelShape Down_AABB = VoxelShapes.create(0.25, 0, 0.25, 0.75, .25, 0.75);

    public static final EnumProperty<Connection> NORTH = EnumProperty.create("north", Connection.class);
    public static final EnumProperty<Connection> SOUTH = EnumProperty.create("south", Connection.class);
    public static final EnumProperty<Connection> WEST = EnumProperty.create("west", Connection.class);
    public static final EnumProperty<Connection> EAST = EnumProperty.create("east", Connection.class);
    public static final EnumProperty<Connection> UP = EnumProperty.create("up", Connection.class);
    public static final EnumProperty<Connection> DOWN = EnumProperty.create("down", Connection.class);
    @SuppressWarnings("UnstableApiUsage")
    private static final ImmutableBiMap<EnumProperty<Connection>, VoxelShape> SHAPE_MAP = Stream.of(
        Pair.of(NORTH, North_AABB),
        Pair.of(SOUTH, South_AABB),
        Pair.of(WEST, West_AABB),
        Pair.of(EAST, East_AABB),
        Pair.of(UP, UP_AABB),
        Pair.of(DOWN, Down_AABB)
    ).collect(ImmutableBiMap.toImmutableBiMap(Pair::getKey, Pair::getValue));
    @SuppressWarnings("UnstableApiUsage")
    public static final ImmutableBiMap<Direction, EnumProperty<Connection>> FACING_TO_PROPERTY_MAP = Stream.of(
        Pair.of(Direction.NORTH, NORTH),
        Pair.of(Direction.SOUTH, SOUTH),
        Pair.of(Direction.WEST, WEST),
        Pair.of(Direction.EAST, EAST),
        Pair.of(Direction.UP, UP),
        Pair.of(Direction.DOWN, DOWN)
    ).collect(ImmutableBiMap.toImmutableBiMap(Pair::getKey, Pair::getValue));

    public BlockItem itemBlock() {
        return blockItem;
    }

    private final BlockItem blockItem;

    public PipeBlock() {
        super(Block.Properties.create(ModObjects.MATERIAL_PIPE())
            .hardnessAndResistance(0.5f));
        setRegistryName(FluidTank.modID, "pipe");
        setDefaultState(getStateContainer().getBaseState()
                .with(NORTH, Connection.NO_CONNECTION)
                .with(SOUTH, Connection.NO_CONNECTION)
                .with(WEST, Connection.NO_CONNECTION)
                .with(EAST, Connection.NO_CONNECTION)
                .with(UP, Connection.NO_CONNECTION)
                .with(DOWN, Connection.NO_CONNECTION)
//            .with(WATERLOGGED, false)
        );
        blockItem = new BlockItem(this, new Item.Properties().group(ModObjects.CREATIVE_TABS()));
        blockItem.setRegistryName(FluidTank.modID, "pipe");
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN/*, WATERLOGGED*/);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE_MAP.entrySet().stream()
            .filter(s -> state.get(s.getKey()) != Connection.NO_CONNECTION)
            .map(Map.Entry::getValue)
            .reduce(BOX_AABB, VoxelShapes::or);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        World worldIn = context.getWorld();
        BlockPos pos = context.getPos();
//        return FACING_TO_PROPERTY_MAP.entrySet().stream()
//        .reduce(this.getDefaultState(), (s, e) -> s.with(e.getValue(), canConnectTo(worldIn, pos.offset(e.getKey()), e.getKey())), (s1, s2) -> s1);
//        IFluidState fluidState = worldIn.getFluidState(pos);
        return this.getDefaultState()
            .with(NORTH, canConnectTo(worldIn, pos.north(), Direction.NORTH))
            .with(EAST, canConnectTo(worldIn, pos.east(), Direction.EAST))
            .with(SOUTH, canConnectTo(worldIn, pos.south(), Direction.SOUTH))
            .with(WEST, canConnectTo(worldIn, pos.west(), Direction.WEST))
            .with(DOWN, canConnectTo(worldIn, pos.down(), Direction.DOWN))
            .with(UP, canConnectTo(worldIn, pos.up(), Direction.UP));
//            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState,
                                          IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        /*if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }*/
        Connection value;
        Connection now = stateIn.get(FACING_TO_PROPERTY_MAP.get(facing));
        if (facingState.getBlock() == this) {
            value = facingState.get(FACING_TO_PROPERTY_MAP.get(facing.getOpposite()));
            return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), value);
        } else {
            value = canConnectTo(worldIn, currentPos.offset(facing), facing);
            if (value.is(Connection.NO_CONNECTION)) {
                if (facingState.getMaterial() == Material.AIR || facingState.getMaterial().isLiquid()) {
                    return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), value);
                } else {
                    return stateIn;
                }
            } else if (value.hasConnection() ^ now.hasConnection())
                return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), value);
            else
                return stateIn;
        }
    }

    private Connection canConnectTo(IWorld worldIn, BlockPos pos, Direction direction) {
        BlockState blockState = worldIn.getBlockState(pos);
        if (blockState.getBlock() == this) {
            return Connection.CONNECTED;
        } else {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity != null) {
                LazyOptional<IFluidHandler> capability = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
                if (capability.isPresent())
                    if (capability.map(f -> f.fill(new FluidStack(Fluids.WATER, 4000), IFluidHandler.FluidAction.SIMULATE)).orElse(0) >= 4000)
                        return Connection.OUTPUT;
                    else
                        return Connection.CONNECTED;
                else
                    return Connection.NO_CONNECTION;
            } else {
                return Connection.NO_CONNECTION;
            }
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ModObjects.PIPE_TYPE().create();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType func_225533_a_(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (player.getHeldItem(handIn).getItem() instanceof BlockItem || player.isCrouching())
            return ActionResultType.PASS;
        Vec3d d = hit.getHitVec().subtract(pos.getX(), pos.getY(), pos.getZ());
        Predicate<Map.Entry<?, VoxelShape>> predicate = e -> {
            AxisAlignedBB box = e.getValue().getBoundingBox();
            return box.minX <= d.x && box.maxX >= d.x && box.minY <= d.y && box.maxY >= d.y && box.minZ <= d.z && box.maxZ >= d.z;
        };
        Optional<Pair<BlockState, Boolean>> blockState = SHAPE_MAP.entrySet().stream()
            .filter(predicate)
            .map(Map.Entry::getKey)
            .findFirst()
            .map(p -> {
                if (worldIn.getBlockState(pos.offset(FACING_TO_PROPERTY_MAP.inverse().get(p))).getBlock() != this)
                    return Pair.of(state.cycle(p), false);
                else
                    return Pair.of(state.with(p, Connection.onOffConnection(state.get(p))), true);
            });
        if (blockState.isPresent()) {
            if (!worldIn.isRemote) {
                worldIn.setBlockState(pos, blockState.get().getKey());
                if (blockState.get().getValue())
                    Optional.ofNullable(worldIn.getTileEntity(pos)).map(PipeTile.class::cast).ifPresent(PipeTile::connectorUpdate);
            }
            return ActionResultType.SUCCESS;
        } else {
            return super.func_225533_a_(state, worldIn, pos, player, handIn, hit);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        BlockState fromState = worldIn.getBlockState(fromPos);
        if (fromState.getBlock() == this) {
            BlockPos vec = fromPos.subtract(pos);
            Direction direction = Direction.func_218383_a(vec.getX(), vec.getY(), vec.getZ());
            if (direction != null) {
                if (fromState.get(FACING_TO_PROPERTY_MAP.get(direction.getOpposite())) == Connection.NO_CONNECTION) {
                    worldIn.setBlockState(pos, state.with(FACING_TO_PROPERTY_MAP.get(direction), Connection.NO_CONNECTION));
                } else if (fromState.get(FACING_TO_PROPERTY_MAP.get(direction.getOpposite())) == Connection.CONNECTED) {
                    worldIn.setBlockState(pos, state.with(FACING_TO_PROPERTY_MAP.get(direction), Connection.CONNECTED));
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof PipeTile) {
                PipeTile tile = (PipeTile) entity;
                tile.connection().reset();
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public IFluidState getFluidState(BlockState state) {
        return /*state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) :*/ super.getFluidState(state);
    }

    public enum Connection implements IStringSerializable {
        NO_CONNECTION,
        CONNECTED,
        INPUT,
        OUTPUT;

        @Override
        public String getName() {
            return name().toLowerCase();
        }

        public boolean is(Connection c1, Connection... cs) {
            return this == c1 || Arrays.asList(cs).contains(this);
        }

        public boolean hasConnection() {
            return is(CONNECTED, INPUT, OUTPUT);
        }

        public boolean isOutput() {
            return is(OUTPUT);
        }

        public boolean isInput() {
            return is(INPUT);
        }

        public static Connection onOffConnection(Connection now) {
            if (now == NO_CONNECTION)
                return CONNECTED;
            else
                return NO_CONNECTION;
        }
    }
}
