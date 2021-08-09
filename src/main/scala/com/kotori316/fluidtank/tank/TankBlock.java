package com.kotori316.fluidtank.tank;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.kotori316.fluidtank.ModTank;

public class TankBlock extends BlockWithEntity {
    public static final String NBT_Tank = "tank";
    public static final String NBT_Tier = "tier";
    public static final String NBT_Capacity = "capacity";
    public static final String NBT_BlockTag = BlockItem.BLOCK_ENTITY_TAG_KEY;
    public static final String NBT_StackName = "stackName";
    public final Tiers tiers;
    private final TankBlockItem blockItem;

    public TankBlock(Tiers tiers) {
        super(Settings.of(ModTank.MATERIAL).nonOpaque().strength(1f, 1f));
        this.tiers = tiers;
        this.blockItem = new TankBlockItem(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ePos) {
        return ModTank.TANK_SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ePos) {
        return ModTank.TANK_SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public void saveTankNBT(BlockEntity entity, ItemStack stack) {
        if (entity instanceof TileTank tank) {
            if (tank.hasContent()) {
                stack.setSubNbt(NBT_BlockTag, tank.getBlockTag());
            }
            tank.getStackName().foreach(stack::setCustomName);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockHitResult hit) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof TileTank tileTank) {
            ItemStack stack = playerIn.getStackInHand(handIn);
            if (playerIn.getMainHandStack().isEmpty()) {
                if (!world.isClient) {
                    playerIn.sendMessage(tileTank.connection().getTextComponent(), true);
                }
                return ActionResult.SUCCESS;
            } else if (!(stack.getItem() instanceof TankBlockItem)) {
                if (!world.isClient) {
                    return com.kotori316.fluidtank.integration.FluidInteractions.interact(tileTank.connection(), playerIn, handIn, stack);
                } else {
                    return ActionResult.SUCCESS;
                }
            } else {
                return ActionResult.PASS;
            }
        } else
            return super.onUse(state, world, pos, playerIn, handIn, hit);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient)
            Optional.ofNullable(world.getBlockEntity(pos))
                .filter(e -> e instanceof TileTank)
                .map(e -> (TileTank) e)
                .ifPresent(TileTank::onBlockPlacedBy);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            Optional.ofNullable(world.getBlockEntity(pos))
                .filter(e -> e instanceof TileTank)
                .map(e -> (TileTank) e)
                .ifPresent(TileTank::onDestroy);
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = super.getPickStack(world, pos, state);
        saveTankNBT(world.getBlockEntity(pos), stack);
        return stack;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity e = world.getBlockEntity(pos);
        if (e instanceof TileTank tank) {
            return tank.getComparatorLevel();
        }
        return super.getComparatorOutput(state, world, pos);
    }

    public TankBlockItem blockItem() {
        return blockItem;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TileTank(tiers, pos, state);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) return null;
        return Optional.ofNullable(checkType(type, ModTank.Entries.TANK_BLOCK_ENTITY_TYPE, TileTank::tick))
            .or(() -> Optional.ofNullable(checkType(type, ModTank.Entries.CREATIVE_BLOCK_ENTITY_TYPE, TileTank::tick)))
            .or(() -> Optional.ofNullable(checkType(type, ModTank.Entries.VOID_BLOCK_ENTITY_TYPE, TileTank::tick)))
            .orElse(null);
    }
}
