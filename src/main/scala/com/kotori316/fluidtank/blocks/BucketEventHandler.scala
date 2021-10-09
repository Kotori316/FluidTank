package com.kotori316.fluidtank.blocks

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.tags.FluidTags
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{Direction, Hand, SoundCategory, SoundEvent, SoundEvents}
import net.minecraft.world.World
import net.minecraftforge.fluids.capability.{IFluidHandler, IFluidHandlerItem}
import net.minecraftforge.fluids.{FluidStack, FluidUtil}
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.wrapper.EmptyHandler

object BucketEventHandler {

  def transferFluid(worldIn: World, pos: BlockPos, playerIn: PlayerEntity, handIn: Hand, toFill: => FluidStack, stack: ItemStack,
                    handlerItem: IFluidHandlerItem, tankHandler: IFluidHandler): Unit = {
    // Just a bridge method. It was used to handle Milk but now forge has the instance of milk, which can be treated as normal fluid.
    transferFluid_internal(worldIn, pos, playerIn, handIn, toFill, stack, handlerItem, tankHandler)
  }

  private def transferFluid_internal(worldIn: World, pos: BlockPos, playerIn: PlayerEntity, handIn: Hand, toFill: => FluidStack, stack: ItemStack,
                                     handlerItem: IFluidHandlerItem, tankHandler: IFluidHandler): Unit = {
    val drain = handlerItem.drain(Int.MaxValue, IFluidHandler.FluidAction.SIMULATE)
    val drainAmount = drain.getAmount
    val itemHandler = playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).orElse(EmptyHandler.INSTANCE)
    val resultFill = FluidUtil.tryEmptyContainerAndStow(stack, tankHandler, itemHandler, drainAmount, playerIn, true)
    if (resultFill.isSuccess) {
      val soundEvent = getFillSound(drain)
      worldIn.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1f, 1f)
      playerIn.setHeldItem(handIn, resultFill.getResult)
    } else {
      val fill = toFill
      val fillAmount = handlerItem.fill(fill, IFluidHandler.FluidAction.SIMULATE)
      val resultDrain = FluidUtil.tryFillContainerAndStow(stack, tankHandler, itemHandler, fillAmount, playerIn, true)
      if (resultDrain.isSuccess) {
        val soundEvent = getEmptySound(fill)
        worldIn.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1f, 1f)
        playerIn.setHeldItem(handIn, resultDrain.getResult)
      }
    }
  }

  private def getEmptySound(fluidStack: FluidStack): SoundEvent = {
    Option(fluidStack.getFluid.getAttributes.getEmptySound(fluidStack)).getOrElse(
      if (fluidStack.getFluid isIn FluidTags.LAVA) SoundEvents.ITEM_BUCKET_EMPTY_LAVA else SoundEvents.ITEM_BUCKET_EMPTY
    )
  }

  private def getFillSound(fluidStack: FluidStack): SoundEvent = {
    Option(fluidStack.getFluid.getAttributes.getFillSound(fluidStack)).getOrElse(
      if (fluidStack.getFluid.isIn(FluidTags.LAVA)) SoundEvents.ITEM_BUCKET_FILL_LAVA else SoundEvents.ITEM_BUCKET_FILL
    )
  }
}
