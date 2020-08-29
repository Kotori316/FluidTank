package com.kotori316.fluidtank.tank;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModTank;
import com.kotori316.fluidtank.Utils;

public class TankBlock extends Block implements BlockEntityProvider {
    public static final String NBT_Tank = "tank";
    public static final String NBT_Tier = "tier";
    public static final String NBT_Capacity = "capacity";
    public static final String NBT_BlockTag = "BlockEntityTag";
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

    public void saveTankNBT(BlockEntity entity, ItemStack stack) {
        if (entity instanceof TileTank) {
            TileTank tank = (TileTank) entity;
            if (tank.hasContent()) {
                stack.putSubTag(NBT_BlockTag, tank.getBlockTag());
            }
            tank.getStackName().foreach(stack::setCustomName);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerIn, Hand handIn, BlockHitResult hit) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof TileTank) {
            TileTank tileTank = (TileTank) entity;
            ItemStack stack = playerIn.getStackInHand(handIn);
            if (playerIn.getMainHandStack().isEmpty()) {
                if (!world.isClient) {
                    playerIn.sendMessage(tileTank.connection().getTextComponent(), true);
                }
                return ActionResult.SUCCESS;
            } else if (!(stack.getItem() instanceof TankBlockItem)) {
                ItemStack copiedStack = stack.getCount() == 1 ? stack : stack.copy();
                copiedStack.setCount(1);
                if (!stack.isEmpty() && FluidAmount.isFluidContainer(stack)) {
                    if (!world.isClient) {
                        FluidAmount.Tank handler = tileTank.connection().handler();
                        BucketEventHandler.transferFluid(world, pos, playerIn, handIn,
                            () -> Utils.toJava(tileTank.connection().getFluidStack()).map(p -> p.setAmount(Integer.MAX_VALUE)).orElse(FluidAmount.EMPTY()),
                            stack, handler);
                    }
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.PASS;
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
    public BlockEntity createBlockEntity(BlockView view) {
        return new TileTank(tiers);
    }

    public TankBlockItem blockItem() {
        return blockItem;
    }
}
