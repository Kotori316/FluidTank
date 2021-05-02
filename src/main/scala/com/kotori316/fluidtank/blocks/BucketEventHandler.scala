package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.network.SideProxy
import com.kotori316.fluidtank.tiles.TileTank
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{BucketItem, ItemStack, Items}
import net.minecraft.tags.FluidTags
import net.minecraft.util.math.{BlockPos, BlockRayTraceResult, RayTraceResult}
import net.minecraft.util.{Direction, Hand, SoundCategory, SoundEvent, SoundEvents}
import net.minecraft.world.World
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.eventbus.api.Event.Result
import net.minecraftforge.fluids.capability.{IFluidHandler, IFluidHandlerItem}
import net.minecraftforge.fluids.{FluidStack, FluidUtil}
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.wrapper.EmptyHandler

object BucketEventHandler {

  def onBucketUsed(event: FillBucketEvent): Unit = {
    val ray = event.getTarget
    if (ray == null || ray.getType != RayTraceResult.Type.BLOCK) return

    event.getWorld.getTileEntity(ray.asInstanceOf[BlockRayTraceResult].getPos) match {
      case tileTank: TileTank =>
        val stack = event.getEmptyBucket
        //1.13.2 code
        val stackFluid = FluidAmount.fromItem(stack)
        // Filling tank.
        if (stackFluid.nonEmpty) {
          if (SideProxy.isServer(tileTank)) {
            val fillAmount = tileTank.connection.handler.fill(stackFluid, IFluidHandler.FluidAction.SIMULATE)
            if (fillAmount.amount == FluidAmount.AMOUNT_BUCKET) {
              tileTank.connection.handler.fill(stackFluid, IFluidHandler.FluidAction.EXECUTE)
              event.setFilledBucket(getContainer(stack, event.getPlayer))
              event.setResult(Result.ALLOW)
            }
          } else {
            event.setFilledBucket(getContainer(stack, event.getPlayer))
            event.setResult(Result.ALLOW)
          }

        }

        if (stack.getItem == Items.BUCKET) {
          if (SideProxy.isServer(tileTank)) {
            val drained = tileTank.connection.handler.drain(FluidAmount.EMPTY.setAmount(FluidAmount.AMOUNT_BUCKET), IFluidHandler.FluidAction.SIMULATE)
            if (drained.nonEmpty) {
              tileTank.connection.handler.drain(FluidAmount.EMPTY.setAmount(FluidAmount.AMOUNT_BUCKET), IFluidHandler.FluidAction.EXECUTE)
              event.setFilledBucket(new ItemStack(drained.fluid.getFilledBucket.asItem()))
              event.setResult(Result.ALLOW)
            }
          }
        }
      case _ =>
    }
  }

  private[this] final val empty_bucket = ObfuscationReflectionHelper.findMethod(classOf[BucketItem],
    "func_203790_a", classOf[ItemStack], classOf[PlayerEntity])

  def getContainer(stack: ItemStack, player: PlayerEntity): ItemStack = {
    if (stack.hasContainerItem)
      stack.getContainerItem
    else {
      stack.getItem match {
        case bucket: BucketItem =>
          if (player != null) empty_bucket.invoke(bucket, stack, player).asInstanceOf[ItemStack] else new ItemStack(Items.BUCKET)
        case _ => ItemStack.EMPTY
      }
    }
  }

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
