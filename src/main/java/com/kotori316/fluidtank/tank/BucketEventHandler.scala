package com.kotori316.fluidtank.tank

import com.kotori316.fluidtank.FluidAmount
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.sound.{SoundCategory, SoundEvent, SoundEvents}
import net.minecraft.tag.FluidTags
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object BucketEventHandler {

  def transferFluid(worldIn: World, pos: BlockPos, playerIn: PlayerEntity, handIn: Hand, toFill: => FluidAmount, stack: ItemStack, tankHandler: FluidAmount.Tank): Unit = {
    if (stack.getItem != Items.MILK_BUCKET) {
      transferFluid_internal(worldIn, pos, playerIn, handIn, toFill, stack, tankHandler)
    } else {
      // Transfer milk
      val drained = FluidAmount.BUCKET_MILK
      val filledSimulation = tankHandler.fill(drained, doFill = false)
      if (filledSimulation.amount >= drained.amount) {
        // Tank can be filled with 1000 mB of milk.
        tankHandler.fill(drained, doFill = true)
        val soundEvent = getFillSound(drained)
        worldIn.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1f, 1f)
        if (!playerIn.abilities.creativeMode) {
          if (stack.getCount > 1) {
            if (!playerIn.inventory.insertStack(new ItemStack(Items.BUCKET))) {
              // Not inserted
              playerIn.dropItem(new ItemStack(Items.BUCKET), false)
            }
            stack.decrement(1)
            playerIn.setStackInHand(handIn, stack)
          } else {
            // Just replace to empty bucket.
            playerIn.setStackInHand(handIn, new ItemStack(Items.BUCKET))
          }
        }
      }
    }
  }

  private def transferFluid_internal(worldIn: World, pos: BlockPos, playerIn: PlayerEntity, handIn: Hand, toFill: => FluidAmount, stack: ItemStack, tankHandler: FluidAmount.Tank): Unit = {
    val drain = FluidAmount.fromItem(stack)
    val drainAmount = drain.amount
    val resultFill = tankHandler.fill(drain, doFill = true, min = drainAmount)
    if (drain.nonEmpty && resultFill == drain) { // Accepted all fluid
      val soundEvent = getFillSound(drain)
      worldIn.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1f, 1f)
      if (!playerIn.abilities.creativeMode) {
        if (stack.getCount == 1)
          playerIn.setStackInHand(handIn, new ItemStack(Items.BUCKET))
        else
          playerIn.inventory.insertStack(new ItemStack(Items.BUCKET))
      }
    } else {
      if (stack.getItem == Items.BUCKET) {
        val fill = toFill
        val fillAmount = fill.setAmount(FluidAmount.AMOUNT_BUCKET) // Limited to bucket
        val drainedFromTank = tankHandler.drain(fillAmount, doDrain = true, fillAmount.amount)
        if (fill.nonEmpty && drainedFromTank == fillAmount) {
          val soundEvent = getEmptySound(fill)
          worldIn.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1f, 1f)
          if (!playerIn.abilities.creativeMode) {
            if (stack.getCount == 1)
              playerIn.setStackInHand(handIn, new ItemStack(fillAmount.fluid.getBucketItem))
            else
              playerIn.inventory.insertStack(new ItemStack(fillAmount.fluid.getBucketItem))
          }
        }
      }
    }
  }

  private def getEmptySound(fluidStack: FluidAmount): SoundEvent = {
    if (fluidStack.fluid matches FluidTags.LAVA) SoundEvents.ITEM_BUCKET_EMPTY_LAVA else SoundEvents.ITEM_BUCKET_EMPTY
  }

  private def getFillSound(fluidStack: FluidAmount): SoundEvent = {
    if (fluidStack.fluid.matches(FluidTags.LAVA)) SoundEvents.ITEM_BUCKET_FILL_LAVA else SoundEvents.ITEM_BUCKET_FILL
  }
}
