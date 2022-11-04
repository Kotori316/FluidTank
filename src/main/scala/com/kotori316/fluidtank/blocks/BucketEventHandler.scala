package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.fluids.{FluidAmount, FluidContainer, VariantUtil}
import net.minecraft.core.BlockPos
import net.minecraft.sounds.{SoundEvent, SoundSource}
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object BucketEventHandler {
  case class TransferResult(result: FluidActionResult, sound: SoundEvent)

  case class FluidActionResult(stack: ItemStack, succeed: Boolean) {
    def this(stack: ItemStack) = {
      this(stack, succeed = true)
    }

    def getResult: ItemStack = stack

    def isSuccess: Boolean = succeed
  }

  def transferFluid(tank: FluidContainer, tankContent: FluidAmount, stack: ItemStack): Option[TransferResult] = {
    // Fill tank and drain from item
    tryFillTank(tank, stack) orElse tryFillContainer(tank, stack, tankContent)
  }

  def transferFluid(tank: FluidContainer, tankContent: FluidAmount, player: Player, hand: InteractionHand): Option[TransferResult] = {
    // Fill tank and drain from item
    tryFillTank(tank, player, hand) orElse tryFillContainer(tank, player, hand, tankContent)
  }

  def transferFluidTest(tank: FluidContainer, stack: ItemStack): Option[TransferResult] = {
    transferFluid(tank, tank.getFluid, stack)
  }

  def checkStack(stack: ItemStack): Option[TransferResult] = {
    if (VariantUtil.isFluidContainer(stack)) {
      val copy = stack.copy()
      copy.setCount(1)
      Option(TransferResult(new FluidActionResult(copy), null))
    } else {
      None
    }
  }

  def tryFillTank(destination: FluidContainer, stack: ItemStack): Option[TransferResult] = {
    VariantUtil.fillFluidContainer(destination, stack)
  }

  def tryFillTank(destination: FluidContainer, player: Player, hand: InteractionHand): Option[TransferResult] = {
    VariantUtil.fillFluidContainer(destination, player, hand)
  }

  def tryFillContainer(source: FluidContainer, stack: ItemStack, tankContent: FluidAmount): Option[TransferResult] = {
    VariantUtil.fillItemContainer(source, stack, tankContent)
  }

  def tryFillContainer(source: FluidContainer, player: Player, hand: InteractionHand, tankContent: FluidAmount): Option[TransferResult] = {
    VariantUtil.fillItemContainer(source, tankContent, player, hand)
  }

  def setItem(level: Level, pos: BlockPos, player: Player, hand: InteractionHand, result: TransferResult, originalStack: ItemStack): Unit = {
    if (!player.isCreative) {
      if (originalStack.getCount == 1) {
        player.setItemInHand(hand, result.result.getResult)
      } else {
        VariantUtil.addItemToPlayer(player, result)
        originalStack.shrink(1)
      }
    }
    level.playSound(null, pos, result.sound, SoundSource.BLOCKS, 1f, 1f)
  }
}
