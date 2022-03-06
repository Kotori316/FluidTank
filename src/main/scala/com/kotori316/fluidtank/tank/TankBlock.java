package com.kotori316.fluidtank.tank;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.TankConstant;

public class TankBlock extends BaseEntityBlock {
    public static final String NBT_Tank = "tank";
    public static final String NBT_Tier = "tier";
    public static final String NBT_Capacity = "capacity";
    public static final String NBT_StackName = "stackName";
    public final Tiers tiers;
    private final TankBlockItem blockItem;

    public TankBlock(Tiers tiers) {
        super(Properties.of(TankConstant.MATERIAL).noOcclusion().strength(1f, 1f));
        this.tiers = tiers;
        this.blockItem = createTankItem();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return TankConstant.TANK_SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
        return TankConstant.TANK_SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public void saveTankNBT(BlockEntity entity, ItemStack stack) {
        if (entity instanceof TileTank tank) {
            if (tank.hasContent()) {
                BlockItem.setBlockEntityData(stack, tank.getType(), tank.saveWithoutMetadata());
            }
            tank.getStackName().foreach(stack::setHoverName);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player playerIn, InteractionHand handIn, BlockHitResult hit) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof TileTank tileTank) {
            ItemStack stack = playerIn.getItemInHand(handIn);
            if (playerIn.getMainHandItem().isEmpty()) {
                if (!world.isClientSide) {
                    playerIn.displayClientMessage(tileTank.connection().getTextComponent(), true);
                }
                return InteractionResult.SUCCESS;
            } else if (!(stack.getItem() instanceof TankBlockItem)) {
                if (!world.isClientSide) {
                    return com.kotori316.fluidtank.integration.FluidInteractions.interact(tileTank.connection(), playerIn, handIn, stack);
                } else {
                    return InteractionResult.SUCCESS;
                }
            } else {
                return InteractionResult.PASS;
            }
        } else
            return super.use(state, world, pos, playerIn, handIn, hit);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide)
            Optional.ofNullable(world.getBlockEntity(pos))
                .filter(e -> e instanceof TileTank)
                .map(e -> (TileTank) e)
                .ifPresent(TileTank::onBlockPlacedBy);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            Optional.ofNullable(world.getBlockEntity(pos))
                .filter(e -> e instanceof TileTank)
                .map(e -> (TileTank) e)
                .ifPresent(TileTank::onDestroy);
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(world, pos, state);
        saveTankNBT(world.getBlockEntity(pos), stack);
        return stack;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity e = world.getBlockEntity(pos);
        if (e instanceof TileTank tank) {
            return tank.getComparatorLevel();
        }
        return super.getAnalogOutputSignal(state, world, pos);
    }

    public final TankBlockItem blockItem() {
        return blockItem;
    }

    protected TankBlockItem createTankItem() {
        return new TankBlockItem(this);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTank(tiers, pos, state);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide) return null;
        return Optional.ofNullable(createTickerHelper(type, ModTank.Entries.TANK_BLOCK_ENTITY_TYPE, TileTank::tick))
            .or(() -> Optional.ofNullable(createTickerHelper(type, ModTank.Entries.CREATIVE_BLOCK_ENTITY_TYPE, TileTank::tick)))
            .or(() -> Optional.ofNullable(createTickerHelper(type, ModTank.Entries.VOID_BLOCK_ENTITY_TYPE, TileTank::tick)))
            .orElse(null);
    }
}
