package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.integration.Localize
import com.kotori316.fluidtank.tiles.{Tiers, TileTankNoDisplay}
import com.kotori316.fluidtank.{FluidTank, _}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.{Item, ItemStack, ItemUseContext, Rarity}
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.math.{BlockRayTraceResult, RayTraceContext, RayTraceResult}
import net.minecraft.util.text.{ITextComponent, TranslationTextComponent}
import net.minecraft.util.{ActionResult, ActionResultType, Hand}
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction

class ReservoirItem(val tier: Tiers) extends Item(FluidTank.proxy.getReservoirProperties.maxStackSize(1)) {
  setRegistryName(FluidTank.modID, "reservoir_" + tier.lowerName)

  // ---------- Fluid Interaction ----------
  override def onItemUse(context: ItemUseContext): ActionResultType = {
    // Move to containers such as tanks.
    val tile = context.getWorld.getTileEntity(context.getPos)
    if (tile != null) {
      if (!context.getWorld.isRemote) {
        val moveResult = for {
          destination <- tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, context.getFace).asScala
          source <- context.getItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).asScala
          result = FluidUtil.tryEmptyContainer(context.getItem, destination, source.getTankCapacity(0), context.getPlayer, true)
          if result.isSuccess
        } yield result
        moveResult.value.value.foreach(s => context.getPlayer.setHeldItem(context.getHand, s.getResult))
      }
      ActionResultType.SUCCESS
    } else {
      super.onItemUse(context)
    }
  }

  override def onItemRightClick(worldIn: World, playerIn: PlayerEntity, handIn: Hand): ActionResult[ItemStack] = {
    val stack = playerIn.getHeldItem(handIn)
    val rayTraceResult = Item.rayTrace(worldIn, playerIn, RayTraceContext.FluidMode.SOURCE_ONLY)
    if (rayTraceResult.getType == RayTraceResult.Type.BLOCK) {
      val blockRayTraceResult = rayTraceResult.asInstanceOf[BlockRayTraceResult]
      val pos = blockRayTraceResult.getPos
      val direction = blockRayTraceResult.getFace
      if (worldIn.isBlockModifiable(playerIn, pos)) {
        val fluidState = worldIn.getFluidState(pos)
        if (!fluidState.isEmpty) {
          val result = FluidUtil.tryPickUpFluid(stack, playerIn, worldIn, pos, direction)
          if (result.isSuccess)
            ActionResult.resultSuccess(result.getResult)
          else
            ActionResult.resultPass(stack)
        } else {
          ActionResult.resultPass(stack)
        }
      } else {
        // Access denied.
        ActionResult.resultFail(stack)
      }
    } else {
      ActionResult.resultPass(stack)
    }
  }

  // ---------- Fluid Handler ----------
  override def initCapabilities(stack: ItemStack, nbt: CompoundNBT): ICapabilityProvider = {
    new TankItemFluidHandler(tier, stack)
  }

  override def getContainerItem(itemStack: ItemStack): ItemStack = {
    import com.kotori316.fluidtank._
    FluidUtil.getFluidHandler(itemStack.copy()).asScala
      .map { f => f.drain(FluidAmount.AMOUNT_BUCKET, FluidAction.EXECUTE); f.getContainer }
      .getOrElse(itemStack)
      .value
  }

  override def hasContainerItem(stack: ItemStack): Boolean = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag) != null

  // ---------- Information ----------
  @OnlyIn(Dist.CLIENT)
  override def addInformation(stack: ItemStack, worldIn: World, tooltip: java.util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
    val nbt = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)
    if (nbt != null) {
      val fluid = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).asScala
        .collect { case h: TankItemFluidHandler => h.getFluid -> h.getCapacity }
        .filter(_._1.nonEmpty)
      fluid.value.value.foreach { case (f, capacity) =>
        tooltip.add(new TranslationTextComponent(Localize.TOOLTIP, f.toStack.getDisplayName, f.amount, capacity))
      }
    }
  }

  override def getRarity(stack: ItemStack): Rarity =
    if (stack.hasTag && stack.getTag.contains(TileTankNoDisplay.NBT_BlockTag)) Rarity.RARE
    else Rarity.COMMON
}
