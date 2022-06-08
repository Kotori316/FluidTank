package com.kotori316.fluidtank.items

import java.util.function.Consumer

import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.integration.Localize
import com.kotori316.fluidtank.network.ClientProxy
import com.kotori316.fluidtank.tiles.Tier
import com.kotori316.fluidtank.{FluidTank, _}
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.{BlockItem, Item, ItemStack, Rarity, TooltipFlag}
import net.minecraft.world.level.{ClipContext, Level}
import net.minecraft.world.phys.HitResult
import net.minecraft.world.{InteractionHand, InteractionResult, InteractionResultHolder}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.client.IItemRenderProperties
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction

class ReservoirItem(val tier: Tier) extends Item(FluidTank.proxy.getReservoirProperties.stacksTo(1)) {
  final val registryName = new ResourceLocation(FluidTank.modID, "reservoir_" + tier.lowerName)

  // ---------- Fluid Interaction ----------
  override def useOn(context: UseOnContext): InteractionResult = {
    // Move to containers such as tanks.
    val tile = context.getLevel.getBlockEntity(context.getClickedPos)
    if (tile != null) {
      if (!context.getLevel.isClientSide) {
        val moveResult = for {
          destination <- tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, context.getClickedFace).asScala
          source <- context.getItemInHand.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).asScala
          result = FluidUtil.tryEmptyContainer(context.getItemInHand, destination, source.getTankCapacity(0), context.getPlayer, true)
          if result.isSuccess
        } yield result
        moveResult.value.value.foreach(s => context.getPlayer.setItemInHand(context.getHand, s.getResult))
      }
      InteractionResult.SUCCESS
    } else {
      super.useOn(context)
    }
  }

  override def use(worldIn: Level, playerIn: Player, handIn: InteractionHand): InteractionResultHolder[ItemStack] = {
    val stack = playerIn.getItemInHand(handIn)
    val rayTraceResult = Item.getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.SOURCE_ONLY)
    if (rayTraceResult.getType == HitResult.Type.BLOCK) {
      val pos = rayTraceResult.getBlockPos
      val direction = rayTraceResult.getDirection
      if (worldIn.mayInteract(playerIn, pos)) {
        val fluidState = worldIn.getFluidState(pos)
        if (!fluidState.isEmpty) {
          val result = FluidUtil.tryPickUpFluid(stack, playerIn, worldIn, pos, direction)
          if (result.isSuccess)
            InteractionResultHolder.success(result.getResult)
          else
            InteractionResultHolder.pass(stack)
        } else {
          InteractionResultHolder.pass(stack)
        }
      } else {
        // Access denied.
        InteractionResultHolder.fail(stack)
      }
    } else {
      InteractionResultHolder.pass(stack)
    }
  }

  // ---------- Fluid Handler ----------
  override def initCapabilities(stack: ItemStack, nbt: CompoundTag): ICapabilityProvider = {
    new TankItemFluidHandler(tier, stack)
  }

  override def getContainerItem(itemStack: ItemStack): ItemStack = {
    FluidUtil.getFluidHandler(itemStack.copy()).asScala
      .map { f => f.drain(FluidAmount.AMOUNT_BUCKET, FluidAction.EXECUTE); f.getContainer }
      .getOrElse(itemStack)
      .value
  }

  override def hasContainerItem(stack: ItemStack): Boolean = BlockItem.getBlockEntityData(stack) != null

  // ---------- Information ----------
  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, worldIn: Level, tooltip: java.util.List[Component], flagIn: TooltipFlag): Unit = {
    val nbt = BlockItem.getBlockEntityData(stack)
    if (nbt != null) {
      val fluid = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).asScala
        .collect { case h: TankItemFluidHandler => h.getFluid -> h.getCapacity }
        .filter(_._1.nonEmpty)
      fluid.value.value.foreach { case (f, capacity) =>
        tooltip.add(Component.translatable(Localize.TOOLTIP, f.toStack.getDisplayName, f.amount, capacity))
      }
    }
  }

  override def getRarity(stack: ItemStack): Rarity =
    if (BlockItem.getBlockEntityData(stack) != null) Rarity.RARE
    else Rarity.COMMON

  override def initializeClient(consumer: Consumer[IItemRenderProperties]): Unit = {
    consumer.accept(new IItemRenderProperties {
      override def getItemStackRenderer: BlockEntityWithoutLevelRenderer = ClientProxy.RENDER_ITEM_RESERVOIR.value
    })
  }
}
