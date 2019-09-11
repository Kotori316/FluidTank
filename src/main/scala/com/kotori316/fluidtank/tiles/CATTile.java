package com.kotori316.fluidtank.tiles;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.kotori316.fluidtank.FluidAmount;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.items.ItemBlockTank;

public class CATTile extends TileEntity implements IInteractionObject {
    // The direction of FACING is facing to you, people expect to use itemBlock targeting an inventory so the chest exists on the opposite side of FACING.
    public List<FluidAmount> fluidCache = Collections.emptyList();

    public CATTile() {
        super(ModObjects.CAT_TYPE());
    }

    @Override
    public ITextComponent getName() {
        return getDisplayName();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(ModObjects.blockCat().getTranslationKey());
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new CATContainer(0, playerIn, pos);
    }

    @Override
    public String getGuiID() {
        return CATContainer.GUI_ID;
    }

    public List<FluidAmount> fluidAmountList() {
        EnumFacing facing = getBlockState().get(BlockStateProperties.FACING);
        TileEntity entity = world.getTileEntity(getPos().offset(facing));
        if (entity != null) {
            IItemHandler handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())
                .filter(h -> h instanceof IItemHandlerModifiable).<IItemHandlerModifiable>cast().orElseGet(EmptyHandler::new);
            return IntStream.range(0, handler.getSlots())
                .mapToObj(handler::getStackInSlot)
                .map(FluidAmount::fromItem)
                .filter(FluidAmount::nonEmpty)
                .collect(Collectors.groupingBy(FluidAmount::fluid, Collectors.summingLong(FluidAmount::amount)))
                .entrySet().stream()
                .map(e -> new FluidAmount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * @return fluid that was filled to chest.
     */
    public FluidAmount fillToChest(FluidAmount toFill) {
        if (toFill.isEmpty()) return FluidAmount.EMPTY();
        EnumFacing facing = getBlockState().get(BlockStateProperties.FACING);
        TileEntity entity = world.getTileEntity(getPos().offset(facing));
        if (entity != null) {
            FluidAmount filled = FluidAmount.EMPTY();
            IItemHandlerModifiable handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())
                .filter(h -> h instanceof IItemHandlerModifiable).<IItemHandlerModifiable>cast().orElseGet(EmptyHandler::new);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack inSlot = handler.getStackInSlot(i);
                if (inSlot.getCount() != 1) continue;
                if (inSlot.getItem() == Items.BUCKET) {
                    handler.setStackInSlot(i, new ItemStack(toFill.fluid().getFilledBucket()));
                    filled = filled.$plus(toFill.setAmount(FluidAmount.AMOUNT_BUCKET()));
                    toFill = toFill.$minus(toFill.setAmount(FluidAmount.AMOUNT_BUCKET()));
                } else if (inSlot.getItem() instanceof ItemBlockTank) {
                    FluidAmount a = FluidAmount.fromItem(inSlot);
                    if (a.isEmpty() || a.fluidEqual(toFill)) {
                        long fill_able = Math.min(ItemBlockTank.getCapacity(inSlot) - a.amount(), toFill.amount());
                        FluidAmount fill = toFill.setAmount(fill_able);
                        ItemBlockTank.saveFluid(inSlot, a.$plus(fill));
                        filled = filled.$plus(fill);
                        toFill = toFill.$minus(fill);
                    }
                }
                if (toFill.isEmpty())
                    break;
            }
            return filled;
        } else {
            return FluidAmount.EMPTY();
        }
    }

    public FluidAmount drainFromChest(int amount) {
        if (amount <= 0) return FluidAmount.EMPTY();
        EnumFacing facing = getBlockState().get(BlockStateProperties.FACING);
        TileEntity entity = world.getTileEntity(getPos().offset(facing));
        if (entity != null) {
            FluidAmount drained = FluidAmount.EMPTY();
            IItemHandlerModifiable handler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite())
                .filter(h -> h instanceof IItemHandlerModifiable).<IItemHandlerModifiable>cast().orElseGet(EmptyHandler::new);
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack inSlot = handler.getStackInSlot(i);
                if (inSlot.getCount() != 1) continue;
                FluidAmount fluid = FluidAmount.fromItem(inSlot);
                if (fluid.nonEmpty() && (drained.isEmpty() || drained.fluidEqual(fluid))) {
                    long drain = Math.min(amount, fluid.amount());
                    drained = drained.$plus(fluid.setAmount(drain));
                    amount = (int) (amount - drain);
                    if (inSlot.getItem() instanceof ItemBlockTank) {
                        ItemBlockTank.saveFluid(inSlot, fluid.$minus(fluid.setAmount(drain)));
                    } else {
                        handler.setStackInSlot(i, inSlot.getContainerItem());
                    }
                }
                if (amount <= 0) break;
            }
            return drained;
        } else {
            return FluidAmount.EMPTY();
        }
    }
}
