package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.integration.Localize
import com.kotori316.fluidtank.tiles.TileTank
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item._
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ActionResultType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, TranslationTextComponent}
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.fluids.FluidUtil
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction

class ItemBlockTank(val blockTank: BlockTank) extends BlockItem(blockTank, FluidTank.proxy.getTankProperties) {
  setRegistryName(FluidTank.modID, blockTank.namePrefix + blockTank.tier.toString.toLowerCase)

  override def getRarity(stack: ItemStack): Rarity =
    if (stack.hasTag && stack.getTag.contains(TileTank.NBT_BlockTag)) Rarity.RARE
    else Rarity.COMMON

  def hasRecipe = true

  @OnlyIn(Dist.CLIENT)
  override def addInformation(stack: ItemStack, worldIn: World, tooltip: java.util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
    val nbt = stack.getChildTag(TileTank.NBT_BlockTag)
    if (nbt != null) {
      val tankNBT = nbt.getCompound(TileTank.NBT_Tank)
      val fluid = FluidAmount.fromNBT(tankNBT)
      val c = tankNBT.getInt(TileTank.NBT_Capacity)
      tooltip.add(new TranslationTextComponent(Localize.TOOLTIP, fluid.toStack.getDisplayName, fluid.amount, c))
    } else {
      tooltip.add(new TranslationTextComponent(Localize.CAPACITY, blockTank.tier.amount))
    }
  }

  override def initCapabilities(stack: ItemStack, nbt: CompoundNBT): ICapabilityProvider = {
    new TankItemFluidHandler(blockTank.tier, stack)
  }

  override def onBlockPlaced(pos: BlockPos, worldIn: World, player: PlayerEntity, stack: ItemStack, state: BlockState): Boolean = {
    if (worldIn.getServer != null) {
      val tileentity = worldIn.getTileEntity(pos)
      if (tileentity != null) {
        val subTag = stack.getChildTag(TileTank.NBT_BlockTag)
        if (subTag != null) {
          if (!(!worldIn.isRemote && tileentity.onlyOpsCanSetNbt) || !(player == null || !player.canUseCommandBlock)) {
            val nbt = tileentity.write(new CompoundNBT)
            nbt.merge(subTag)
            nbt.putInt("x", pos.getX)
            nbt.putInt("y", pos.getY)
            nbt.putInt("z", pos.getZ)
            tileentity.read(state, nbt)
            tileentity.markDirty()
          }
        }
        if (stack.hasDisplayName) {
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

  override def tryPlace(context: BlockItemUseContext): ActionResultType = {
    if (Option(context.getPlayer).exists(_.isCreative)) {
      val size = context.getItem.getCount
      val result = super.tryPlace(context)
      context.getItem.setCount(size)
      result
    } else {
      super.tryPlace(context)
    }
  }

  override def getContainerItem(itemStack: ItemStack): ItemStack = {
    import com.kotori316.fluidtank._
    FluidUtil.getFluidHandler(itemStack.copy()).asScala
      .map { f => f.drain(FluidAmount.AMOUNT_BUCKET, FluidAction.EXECUTE); f.getContainer }
      .getOrElse(itemStack)
      .value
  }

  override def hasContainerItem(stack: ItemStack): Boolean = stack.getChildTag(TileTank.NBT_BlockTag) != null
}
