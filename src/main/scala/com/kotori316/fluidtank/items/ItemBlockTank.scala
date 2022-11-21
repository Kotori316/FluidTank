package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.integration.Localize
import com.kotori316.fluidtank.tiles.TileTank
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import net.fabricmc.api.{EnvType, Environment}
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.{BlockItem, Item, ItemStack, Rarity, TooltipFlag}
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.jetbrains.annotations.Nullable

class ItemBlockTank(val blockTank: BlockTank) extends BlockItem(blockTank, new Item.Properties().tab(ModObjects.CREATIVE_TABS)) {
  final val registryName = new ResourceLocation(FluidTank.modID, blockTank.namePrefix + blockTank.tier.toString.toLowerCase)

  override def getRarity(stack: ItemStack): Rarity =
    if (BlockItem.getBlockEntityData(stack) != null) Rarity.RARE
    else Rarity.COMMON

  def hasInvisibleRecipe = true

  @Environment(EnvType.CLIENT)
  override def appendHoverText(stack: ItemStack, @Nullable level: Level, tooltip: java.util.List[Component], flag: TooltipFlag): Unit = {
    val nbt = BlockItem.getBlockEntityData(stack)
    if (nbt != null) {
      val tankNBT = nbt.getCompound(TileTank.NBT_Tank)
      val fluid = FluidAmount.fromNBT(tankNBT)
      val c = tankNBT.getLong(TileTank.NBT_Capacity)
      tooltip.add(Component.translatable(Localize.TOOLTIP, fluid.getDisplayName, fluid.amount, c))
    } else {
      tooltip.add(Component.translatable(Localize.CAPACITY, blockTank.tier.amount))
    }
  }

  def initCapabilities(stack: ItemStack, nbt: CompoundTag): TankItemFluidHandler = {
    new TankItemFluidHandler(blockTank.tier, stack)
  }

  override def updateCustomBlockEntityTag(pos: BlockPos, level: Level, @Nullable player: Player, stack: ItemStack, state: BlockState): Boolean = {
    if (level.getServer != null) {
      val tileentity = level.getBlockEntity(pos)
      if (tileentity != null) {
        val subTag = BlockItem.getBlockEntityData(stack)
        if (subTag != null) {
          if (!(!level.isClientSide && tileentity.onlyOpCanSetNbt) || !(player == null || !player.canUseGameMasterBlocks)) {
            val nbt = tileentity.saveWithoutMetadata()
            nbt.merge(subTag)
            nbt.putInt("x", pos.getX)
            nbt.putInt("y", pos.getY)
            nbt.putInt("z", pos.getZ)
            tileentity.load(nbt)
            tileentity.setChanged()
          }
        }
        if (stack.hasCustomHoverName) {
          tileentity match {
            case tank: TileTank => tank.stackName = stack.getDisplayName
            case _ =>
          }
        }
        return true
      }
    }
    false
  }

  override def place(context: BlockPlaceContext): InteractionResult = {
    if (Option(context.getPlayer).exists(_.isCreative)) {
      val size = context.getItemInHand.getCount
      val result = super.place(context)
      context.getItemInHand.setCount(size)
      result
    } else {
      super.place(context)
    }
  }

  override def getRecipeRemainder(itemStack: ItemStack): ItemStack = ItemUtil.removeOneBucket(itemStack)

  override def hasCraftingRemainingItem: Boolean = true

  override def toString: String = String.valueOf(registryName)
}
