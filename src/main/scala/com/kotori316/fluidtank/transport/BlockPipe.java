package com.kotori316.fluidtank.transport;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;

public class BlockPipe extends Block {
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
    private static final Map<EnumProperty<Connection>, VoxelShape> SHAPE_MAP = Stream.of(
        Pair.of(NORTH, North_AABB),
        Pair.of(SOUTH, South_AABB),
        Pair.of(WEST, West_AABB),
        Pair.of(EAST, East_AABB),
        Pair.of(UP, UP_AABB),
        Pair.of(DOWN, Down_AABB)
    ).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    public static final Map<Direction, EnumProperty<Connection>> FACING_TO_PROPERTY_MAP = Stream.of(
        Pair.of(Direction.NORTH, NORTH),
        Pair.of(Direction.SOUTH, SOUTH),
        Pair.of(Direction.WEST, WEST),
        Pair.of(Direction.EAST, EAST),
        Pair.of(Direction.UP, UP),
        Pair.of(Direction.DOWN, DOWN)
    ).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    ;

    public BlockItem itemBlock() {
        return blockItem;
    }

    private final BlockItem blockItem;

    public BlockPipe() {
        super(Block.Properties.create(Material.MISCELLANEOUS)
            .hardnessAndResistance(0.5f));
        setRegistryName(FluidTank.modID, "pipe");
        setDefaultState(getStateContainer().getBaseState()
            .with(NORTH, Connection.NO_CONNECTION)
            .with(SOUTH, Connection.NO_CONNECTION)
            .with(WEST, Connection.NO_CONNECTION)
            .with(EAST, Connection.NO_CONNECTION)
            .with(UP, Connection.NO_CONNECTION)
            .with(DOWN, Connection.NO_CONNECTION)
        );
        blockItem = new BlockItem(this, new Item.Properties().group(ModObjects.CREATIVE_TABS()));
        blockItem.setRegistryName(FluidTank.modID, "pipe");
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
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
        return this.getDefaultState()
            .with(NORTH, canConnectTo(worldIn, pos.north(), Direction.NORTH))
            .with(EAST, canConnectTo(worldIn, pos.east(), Direction.EAST))
            .with(SOUTH, canConnectTo(worldIn, pos.south(), Direction.SOUTH))
            .with(WEST, canConnectTo(worldIn, pos.west(), Direction.WEST))
            .with(DOWN, canConnectTo(worldIn, pos.down(), Direction.DOWN))
            .with(UP, canConnectTo(worldIn, pos.up(), Direction.UP));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState,
                                          IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), canConnectTo(worldIn, currentPos.offset(facing), facing));
    }

    private Connection canConnectTo(IWorld worldIn, BlockPos pos, Direction direction) {
        if (worldIn.getBlockState(pos).getBlock() == this) {
            return Connection.CONNECTED;
        } else {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity != null && entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
                return Connection.CONNECTED;
            } else {
                return Connection.NO_CONNECTION;
            }
        }
    }

    public enum Connection implements IStringSerializable {
        NO_CONNECTION,
        INPUT,
        OUTPUT,
        CONNECTED;

        @Override
        public String getName() {
            return name().toLowerCase();
        }
    }
}
