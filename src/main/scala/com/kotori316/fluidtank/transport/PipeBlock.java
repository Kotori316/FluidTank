package com.kotori316.fluidtank.transport;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableBiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import com.kotori316.fluidtank.Config;
import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.Utils;

public abstract class PipeBlock extends Block implements EntityBlock {
    public static final VoxelShape BOX_AABB = Shapes.box(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    public static final VoxelShape North_AABB = Shapes.box(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    public static final VoxelShape South_AABB = Shapes.box(0.25, 0.25, .75, 0.75, 0.75, 1);
    public static final VoxelShape West_AABB = Shapes.box(0, 0.25, 0.25, .25, 0.75, 0.75);
    public static final VoxelShape East_AABB = Shapes.box(.75, 0.25, 0.25, 1, 0.75, 0.75);
    public static final VoxelShape UP_AABB = Shapes.box(0.25, .75, 0.25, 0.75, 1, 0.75);
    public static final VoxelShape Down_AABB = Shapes.box(0.25, 0, 0.25, 0.75, .25, 0.75);

    public static final EnumProperty<Connection> NORTH = EnumProperty.create("north", Connection.class);
    public static final EnumProperty<Connection> SOUTH = EnumProperty.create("south", Connection.class);
    public static final EnumProperty<Connection> WEST = EnumProperty.create("west", Connection.class);
    public static final EnumProperty<Connection> EAST = EnumProperty.create("east", Connection.class);
    public static final EnumProperty<Connection> UP = EnumProperty.create("up", Connection.class);
    public static final EnumProperty<Connection> DOWN = EnumProperty.create("down", Connection.class);
    private static final ImmutableBiMap<EnumProperty<Connection>, VoxelShape> SHAPE_MAP = ImmutableBiMap.of(
        PipeBlock.NORTH, North_AABB,
        PipeBlock.SOUTH, South_AABB,
        PipeBlock.WEST, West_AABB,
        PipeBlock.EAST, East_AABB,
        PipeBlock.UP, UP_AABB,
        PipeBlock.DOWN, Down_AABB
    );
    public static final ImmutableBiMap<Direction, EnumProperty<Connection>> FACING_TO_PROPERTY_MAP = ImmutableBiMap.of(
        Direction.NORTH, NORTH,
        Direction.SOUTH, SOUTH,
        Direction.WEST, WEST,
        Direction.EAST, EAST,
        Direction.UP, UP,
        Direction.DOWN, DOWN
    );

    @NotNull
    public static EnumProperty<Connection> getPropertyFromDirection(Direction facing) {
        return Objects.requireNonNull(FACING_TO_PROPERTY_MAP.get(facing));
    }

    public BlockItem itemBlock() {
        return blockItem;
    }

    private final BlockItem blockItem;
    public final ResourceLocation registryName;

    public PipeBlock() {
        super(Block.Properties.of(ModObjects.MATERIAL_PIPE())
            .strength(0.5f));
        registryName = new ResourceLocation(FluidTank.modID, getRegName());
        this.registerDefaultState(getStateDefinition().any()
                .setValue(NORTH, Connection.NO_CONNECTION)
                .setValue(SOUTH, Connection.NO_CONNECTION)
                .setValue(WEST, Connection.NO_CONNECTION)
                .setValue(EAST, Connection.NO_CONNECTION)
                .setValue(UP, Connection.NO_CONNECTION)
                .setValue(DOWN, Connection.NO_CONNECTION)
//            .with(WATERLOGGED, false)
        );
        blockItem = new BlockItem(this, new Item.Properties());
    }

    @NotNull
    protected abstract String getRegName();

    protected abstract boolean isHandler(BlockGetter level, BlockPos pos, EnumProperty<Connection> property);

    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN/*, WATERLOGGED*/);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE_MAP.entrySet().stream()
            .filter(s -> state.getValue(s.getKey()) != Connection.NO_CONNECTION || isHandler(worldIn, pos, s.getKey()))
            .map(Map.Entry::getValue)
            .reduce(BOX_AABB, Shapes::or);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level worldIn = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
            .setValue(NORTH, canConnectTo(worldIn, pos.north(), Direction.NORTH))
            .setValue(EAST, canConnectTo(worldIn, pos.east(), Direction.EAST))
            .setValue(SOUTH, canConnectTo(worldIn, pos.south(), Direction.SOUTH))
            .setValue(WEST, canConnectTo(worldIn, pos.west(), Direction.WEST))
            .setValue(DOWN, canConnectTo(worldIn, pos.below(), Direction.DOWN))
            .setValue(UP, canConnectTo(worldIn, pos.above(), Direction.UP));
//            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState,
                                  LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        /*if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }*/
        Connection value;
        Connection now = stateIn.getValue(getPropertyFromDirection(facing));
        if (facingState.getBlock() == this) {
            value = facingState.getValue(getPropertyFromDirection(facing.getOpposite()));
            return stateIn.setValue(getPropertyFromDirection(facing), value);
        } else {
            value = canConnectTo(worldIn, currentPos.relative(facing), facing);
            if (value.is(Connection.NO_CONNECTION)) {
                if (facingState.getMaterial() == Material.AIR || facingState.getMaterial().isLiquid()) {
                    return stateIn.setValue(getPropertyFromDirection(facing), value);
                } else {
                    return stateIn;
                }
            } else if (value.hasConnection() ^ now.hasConnection())
                return stateIn.setValue(getPropertyFromDirection(facing), value);
            else
                return stateIn;
        }
    }

    private Connection canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockState blockState = level.getBlockState(pos);
        BlockEntity entity = level.getBlockEntity(pos);
        if (blockState.getBlock() == this) {
            if (!Config.content().enablePipeRainbowRenderer().get() && entity instanceof PipeTileBase p) {
                return p.getColor() == Config.content().pipeColor().get() ? Connection.CONNECTED : Connection.NO_CONNECTION;
            }
            return Connection.CONNECTED;
        } else {
            if (entity != null) {
                return getConnection(direction, entity);
            } else {
                return Connection.NO_CONNECTION;
            }
        }
    }

    @NotNull
    protected abstract Connection getConnection(Direction direction, @NotNull BlockEntity entity);

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (player.getItemInHand(handIn).getItem() instanceof BlockItem || player.isCrouching())
            return InteractionResult.PASS;
        // Dying pipe.
        OptionalInt maybeColor = Utils.getItemColor(player.getItemInHand(handIn));
        if (maybeColor.isPresent() && !Config.content().enablePipeRainbowRenderer().get()) {
            if (!worldIn.isClientSide) {
                Optional.ofNullable(worldIn.getBlockEntity(pos)).map(PipeTileBase.class::cast).ifPresent(p -> p.changeColor(maybeColor.getAsInt()));
                Object colorName = Stream.of(DyeColor.values()).filter(d -> d.getMaterialColor().col == maybeColor.getAsInt()).findFirst()
                    .map(c -> (Object) Component.translatable("color.minecraft." + c))
                    .orElse(String.format("#%06x", maybeColor.getAsInt()));
                player.displayClientMessage(
                    Component.translatable("chat.fluidtank.change_color", colorName),
                    false);
            }
            return InteractionResult.SUCCESS;
        }

        // Modifying pipe connection.
        Vec3 d = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        Predicate<Map.Entry<?, VoxelShape>> predicate = e -> {
            AABB box = e.getValue().bounds();
            return box.minX <= d.x && box.maxX >= d.x && box.minY <= d.y && box.maxY >= d.y && box.minZ <= d.z && box.maxZ >= d.z;
        };
        Optional<Pair<BlockState, Boolean>> blockState = SHAPE_MAP.entrySet().stream()
            .filter(predicate)
            .map(Map.Entry::getKey)
            .findFirst()
            .map(p -> {
                if (worldIn.getBlockState(pos.relative(Objects.requireNonNull(FACING_TO_PROPERTY_MAP.inverse().get(p)))).getBlock() != this)
                    return Pair.of(state.cycle(p), false);
                else
                    return Pair.of(state.setValue(p, Connection.onOffConnection(state.getValue(p))), true);
            });
        if (blockState.isPresent()) {
            if (!worldIn.isClientSide) {
                worldIn.setBlockAndUpdate(pos, blockState.get().getKey());
                if (blockState.get().getValue())
                    Optional.ofNullable(worldIn.getBlockEntity(pos)).map(PipeTileBase.class::cast).ifPresent(PipeTileBase::connectorUpdate);
            }
            return InteractionResult.SUCCESS;
        } else {
            return super.use(state, worldIn, pos, player, handIn, hit);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        BlockState fromState = worldIn.getBlockState(fromPos);
        // Update connection between pipes.
        if (fromState.getBlock() == this) {
            BlockPos vec = fromPos.subtract(pos);
            Direction direction = Direction.fromNormal(vec.getX(), vec.getY(), vec.getZ());
            if (direction != null) {
                if (fromState.getValue(getPropertyFromDirection(direction.getOpposite())) == Connection.NO_CONNECTION) {
                    worldIn.setBlockAndUpdate(pos, state.setValue(getPropertyFromDirection(direction), Connection.NO_CONNECTION));
                } else if (fromState.getValue(getPropertyFromDirection(direction.getOpposite())) == Connection.CONNECTED) {
                    worldIn.setBlockAndUpdate(pos, state.setValue(getPropertyFromDirection(direction), Connection.CONNECTED));
                }
            }
        }
        // Update handlers
        if (!worldIn.isClientSide) {
            Optional.ofNullable((PipeTileBase) worldIn.getBlockEntity(pos))
                .ifPresent(t -> t.removeCapCache(fromPos));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof PipeTileBase tile) {
                tile.connectorUpdate();
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return /*state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) :*/ super.getFluidState(state);
    }

    public enum Connection implements StringRepresentable {
        NO_CONNECTION,
        CONNECTED,
        INPUT,
        OUTPUT;

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

        @Override
        public String getSerializedName() {
            return getName();
        }
    }

}
