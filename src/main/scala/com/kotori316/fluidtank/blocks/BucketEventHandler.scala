package com.kotori316.fluidtank.blocks

import cats.Eval
import net.minecraft.core.BlockPos
import net.minecraft.sounds.{SoundEvent, SoundEvents, SoundSource}
import net.minecraft.tags.FluidTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.{FluidActionResult, FluidStack, FluidUtil}
import net.minecraftforge.items.{CapabilityItemHandler, ItemHandlerHelper}

import scala.jdk.OptionConverters._

object BucketEventHandler {
  case class TransferResult(result: FluidActionResult, sound: SoundEvent)

  def transferFluid(tank: IFluidHandler, tankContent: => FluidStack, stack: ItemStack): Option[TransferResult] = {
    // Fill tank and drain from item
    tryFillTank(tank, stack) orElse tryFillContainer(tank, stack, tankContent)
  }

  def transferFluidTest(tank: IFluidHandler, stack: ItemStack): Option[TransferResult] = {
    transferFluid(tank, tank.getFluidInTank(0), stack)
  }

  def checkStack(stack: ItemStack): Option[TransferResult] = {
    FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(stack, 1)).resolve().toScala
      .map(i => TransferResult(new FluidActionResult(i.getContainer), null))
  }

  def tryFillTank(destination: IFluidHandler, stack: ItemStack): Option[TransferResult] = {
    val sourceOption = Eval.always(FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(stack, 1)).resolve().toScala)
    for {
      source <- sourceOption.value
      fluidInItem = source.drain(Int.MaxValue, IFluidHandler.FluidAction.SIMULATE)
      fillSimulation = destination.fill(fluidInItem, IFluidHandler.FluidAction.SIMULATE)
      if fillSimulation > 0
      drained = source.drain(fillSimulation, IFluidHandler.FluidAction.EXECUTE)
      if !drained.isEmpty
      fillSimulation2 = destination.fill(drained, IFluidHandler.FluidAction.SIMULATE)
      if fillSimulation2 > 0
      source2 <- sourceOption.value
      drainExecution = source2.drain(fillSimulation2, IFluidHandler.FluidAction.EXECUTE)
    } yield {
      destination.fill(drainExecution, IFluidHandler.FluidAction.EXECUTE)
      TransferResult(new FluidActionResult(source2.getContainer), getFillSound(drainExecution))
    }
  }

  def tryFillContainer(source: IFluidHandler, stack: ItemStack, tankContent: => FluidStack): Option[TransferResult] = {
    val destinationGetter = Eval.always(FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(stack, 1)).resolve().toScala)
    for {
      destination <- destinationGetter.value
      toFill = tankContent
      fillSimulation = toFill.copy()
      _ = fillSimulation.setAmount(destination.fill(toFill, IFluidHandler.FluidAction.EXECUTE))
      if !fillSimulation.isEmpty
      drained = source.drain(fillSimulation, IFluidHandler.FluidAction.EXECUTE)
      d2 <- destinationGetter.value
    } yield {
      d2.fill(drained, IFluidHandler.FluidAction.EXECUTE)
      TransferResult(new FluidActionResult(d2.getContainer), getEmptySound(drained))
    }
  }

  def setItem(level: Level, pos: BlockPos, player: Player, hand: InteractionHand, result: TransferResult, originalStack: ItemStack): Unit = {
    if (!player.isCreative) {
      if (originalStack.getCount == 1) {
        player.setItemInHand(hand, result.result.getResult)
      } else {
        val rest = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
          .map[ItemStack](i => ItemHandlerHelper.insertItemStacked(i, result.result.getResult, false))
        rest.ifPresent(i => ItemHandlerHelper.giveItemToPlayer(player, i))
        originalStack.shrink(1)
      }
    }
    level.playSound(null, pos, result.sound, SoundSource.BLOCKS, 1f, 1f)
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
