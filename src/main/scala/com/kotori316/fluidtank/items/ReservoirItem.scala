package com.kotori316.fluidtank.items

import cats.implicits.catsSyntaxEq
import com.kotori316.fluidtank.fluids.{FabricAmount, FluidAction, FluidAmount, VariantUtil}
import com.kotori316.fluidtank.integration.Localize
import com.kotori316.fluidtank.tiles.Tier
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.item.{BlockItem, Item, ItemStack, Rarity, TooltipFlag}
import net.minecraft.world.level.block.BucketPickup
import net.minecraft.world.level.{ClipContext, Level}
import net.minecraft.world.phys.HitResult
import net.minecraft.world.{InteractionHand, InteractionResult, InteractionResultHolder}

class ReservoirItem(val tier: Tier) extends Item(new Item.Properties().stacksTo(1)) {
  final val registryName = new ResourceLocation(FluidTank.modID, "reservoir_" + tier.lowerName)

  // ---------- Fluid Interaction ----------
  override def useOn(context: UseOnContext): InteractionResult = {
    // Move to containers such as tanks.
    val handler = new TankItemFluidHandler(tier, context.getItemInHand)
    if (handler.getFluid.nonEmpty) {
      if (!context.getLevel.isClientSide) {
        val source = handler.getFluid
        val inserted = VariantUtil.fillAtPos(source, context.getLevel, context.getClickedPos, context.getClickedFace)
        handler.drain(inserted, FluidAction.EXECUTE)
        InteractionResult.CONSUME
      } else {
        if (VariantUtil.isFluidContainer(context.getLevel, context.getClickedPos, context.getClickedFace)) {
          InteractionResult.sidedSuccess(context.getLevel.isClientSide)
        } else {
          InteractionResult.PASS
        }
      }
    } else {
      super.useOn(context)
    }
  }

  override def use(worldIn: Level, playerIn: Player, handIn: InteractionHand): InteractionResultHolder[ItemStack] = {
    val stack = playerIn.getItemInHand(handIn).copy()
    val rayTraceResult = Item.getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.SOURCE_ONLY)
    if (rayTraceResult.getType == HitResult.Type.BLOCK) {
      val pos = rayTraceResult.getBlockPos
      if (worldIn.mayInteract(playerIn, pos)) {
        val state = worldIn.getBlockState(pos)
        val fluidState = worldIn.getFluidState(pos)
        val itemHandler = new TankItemFluidHandler(tier, stack)
        state.getBlock match {
          case pickup: BucketPickup =>
            val simulate = itemHandler.fill(FluidAmount(fluidState.getType, FabricAmount.BUCKET, None), FluidAction.SIMULATE)
            if (simulate.nonEmpty && simulate.fabricAmount === FabricAmount.BUCKET) {
              val picked = pickup.pickupBlock(worldIn, pos, state)
              if (!picked.isEmpty) {
                val fluid = VariantUtil.getFluidInItem(picked)
                itemHandler.fill(fluid, FluidAction.EXECUTE)
                pickup.getPickupSound.ifPresent(s => playerIn.playSound(s, 1f, 1f))
                InteractionResultHolder.success(stack)
              } else {
                // Couldn't pickup the fluid
                InteractionResultHolder.pass(stack)
              }
            } else {
              // Fill execution failed.
              InteractionResultHolder.pass(stack)
            }
          case _ => InteractionResultHolder.pass(stack)
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
  override def getRecipeRemainder(itemStack: ItemStack): ItemStack = ItemUtil.removeOneBucket(itemStack)

  override def hasCraftingRemainingItem: Boolean = true

  // ---------- Information ----------
  @Environment(EnvType.CLIENT)
  override def appendHoverText(stack: ItemStack, worldIn: Level, tooltip: java.util.List[Component], flagIn: TooltipFlag): Unit = {
    val nbt = BlockItem.getBlockEntityData(stack)
    if (nbt != null) {
      val fluid = Option(new TankItemFluidHandler(tier, stack))
        .map { h => h.getFluid -> h.getCapacity }
        .filter(_._1.nonEmpty)
      fluid.foreach { case (f, capacity) =>
        tooltip.add(Component.translatable(Localize.TOOLTIP, f.getDisplayName, f.amount, capacity))
      }
    }
  }

  override def getRarity(stack: ItemStack): Rarity =
    if (BlockItem.getBlockEntityData(stack) != null) Rarity.RARE
    else Rarity.COMMON

}
