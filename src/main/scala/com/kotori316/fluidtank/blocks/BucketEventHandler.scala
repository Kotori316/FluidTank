package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.FluidAmount
import com.kotori316.fluidtank.network.SideProxy
import com.kotori316.fluidtank.tiles.TileTankNoDisplay
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{BucketItem, ItemStack, Items}
import net.minecraft.util.math.{BlockRayTraceResult, RayTraceResult}
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.eventbus.api.Event.Result
import net.minecraftforge.fml.common.ObfuscationReflectionHelper

object BucketEventHandler {

  def onBucketUsed(event: FillBucketEvent): Unit = {
    val ray = event.getTarget
    if (ray == null || ray.getType != RayTraceResult.Type.BLOCK) return

    event.getWorld.getTileEntity(ray.asInstanceOf[BlockRayTraceResult].getPos) match {
      case tileTank: TileTankNoDisplay =>
        val stack = event.getEmptyBucket
        //1.13.2 code
        val stackFluid = FluidAmount.fromItem(stack)
        // Filling tank.
        if (stackFluid.nonEmpty) {
          if (SideProxy.isServer(tileTank)) {
            val fillAmount = tileTank.connection.handler.fill(stackFluid, doFill = false, min = FluidAmount.AMOUNT_BUCKET)
            if (fillAmount.nonEmpty) {
              tileTank.connection.handler.fill(stackFluid, doFill = true, min = FluidAmount.AMOUNT_BUCKET)
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
            val drained = tileTank.connection.handler.drain(FluidAmount.EMPTY.setAmount(FluidAmount.AMOUNT_BUCKET), doDrain = false)
            if (drained.nonEmpty) {
              tileTank.connection.handler.drain(FluidAmount.EMPTY.setAmount(FluidAmount.AMOUNT_BUCKET), doDrain = true)
              event.setFilledBucket(new ItemStack(drained.fluid.getFilledBucket.asItem()))
              event.setResult(Result.ALLOW)
            }
          }
        }
      case _ =>
    }
  }

  private[this] final val empty_bucket = ObfuscationReflectionHelper.findMethod(classOf[BucketItem], "func_203790_a", classOf[ItemStack], classOf[PlayerEntity])

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
}
