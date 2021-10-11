package com.kotori316.fluidtank.blocks

import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.sounds.{SoundEvent, SoundEvents, SoundSource}
import net.minecraft.tags.FluidTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.fluids.capability.{IFluidHandler, IFluidHandlerItem}
import net.minecraftforge.fluids.{FluidStack, FluidUtil}
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.wrapper.EmptyHandler

object BucketEventHandler {

  def transferFluid(worldIn: Level, pos: BlockPos, playerIn: Player, handIn: InteractionHand, toFill: => FluidStack, stack: ItemStack,
                    handlerItem: IFluidHandlerItem, tankHandler: IFluidHandler): Unit = {
    // Just a bridge method. It was used to handle Milk but now forge has the instance of milk, which can be treated as normal fluid.
    transferFluid_internal(worldIn, pos, playerIn, handIn, toFill, stack, handlerItem, tankHandler)
  }

  private def transferFluid_internal(worldIn: Level, pos: BlockPos, playerIn: Player, handIn: InteractionHand, toFill: => FluidStack, stack: ItemStack,
                                     handlerItem: IFluidHandlerItem, tankHandler: IFluidHandler): Unit = {
    val drain = handlerItem.drain(Int.MaxValue, IFluidHandler.FluidAction.SIMULATE)
    val drainAmount = drain.getAmount
    val itemHandler = playerIn.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP).orElse(EmptyHandler.INSTANCE)
    val resultFill = FluidUtil.tryEmptyContainerAndStow(stack, tankHandler, itemHandler, drainAmount, playerIn, true)
    if (resultFill.isSuccess) {
      val soundEvent = getFillSound(drain)
      worldIn.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1f, 1f)
      playerIn.setItemInHand(handIn, resultFill.getResult)
    } else {
      val fill = toFill
      val fillAmount = handlerItem.fill(fill, IFluidHandler.FluidAction.SIMULATE)
      val resultDrain = FluidUtil.tryFillContainerAndStow(stack, tankHandler, itemHandler, fillAmount, playerIn, true)
      if (resultDrain.isSuccess) {
        val soundEvent = getEmptySound(fill)
        worldIn.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1f, 1f)
        playerIn.setItemInHand(handIn, resultDrain.getResult)
      }
    }
  }

  private def getEmptySound(fluidStack: FluidStack): SoundEvent = {
    Option(fluidStack.getFluid.getAttributes.getEmptySound(fluidStack)).getOrElse(
      if (fluidStack.getFluid is FluidTags.LAVA) SoundEvents.BUCKET_EMPTY_LAVA else SoundEvents.BUCKET_EMPTY
    )
  }

  private def getFillSound(fluidStack: FluidStack): SoundEvent = {
    Option(fluidStack.getFluid.getAttributes.getFillSound(fluidStack)).getOrElse(
      if (fluidStack.getFluid.is(FluidTags.LAVA)) SoundEvents.BUCKET_FILL_LAVA else SoundEvents.BUCKET_FILL
    )
  }
}
