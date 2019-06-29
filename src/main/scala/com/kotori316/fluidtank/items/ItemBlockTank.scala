package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.tiles.TileTankNoDisplay
import com.kotori316.fluidtank.{FluidAmount, FluidTank}
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item._
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, StringTextComponent}
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.ICapabilityProvider

class ItemBlockTank(val blockTank: BlockTank) extends BlockItem(blockTank, FluidTank.proxy.getTankProperties) {
  setRegistryName(FluidTank.modID, blockTank.namePrefix + blockTank.tier.toString.toLowerCase)

  override def getRarity(stack: ItemStack): Rarity =
    if (stack.hasTag && stack.getTag.contains(TileTankNoDisplay.NBT_BlockTag)) Rarity.RARE
    else Rarity.COMMON

  def hasRecipe = true

  @OnlyIn(Dist.CLIENT)
  override def addInformation(stack: ItemStack, worldIn: World, tooltip: java.util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
    val nbt = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)
    if (nbt != null) {
      val tankNBT = nbt.getCompound(TileTankNoDisplay.NBT_Tank)
      val fluid = Option(FluidAmount.fromNBT(tankNBT))
      val c = tankNBT.getInt(TileTankNoDisplay.NBT_Capacity)
      tooltip.add(new StringTextComponent(fluid.fold("Empty")(_.getLocalizedName) + " : " + fluid.fold(0l)(_.amount) + " mB / " + c + " mB"))
    } else {
      tooltip.add(new StringTextComponent("Capacity : " + blockTank.tier.amount + "mB"))
    }
  }

  override def initCapabilities(stack: ItemStack, nbt: CompoundNBT): ICapabilityProvider = {
    if (true) return null // 1.13.2
    new TankItemFluidHandler(this, stack)
  }

  override def onBlockPlaced(pos: BlockPos, worldIn: World, player: PlayerEntity, stack: ItemStack, state: BlockState): Boolean = {
    if (worldIn.getServer != null) {
      val tileentity = worldIn.getTileEntity(pos)
      if (tileentity != null) {
        val subTag = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)
        if (subTag != null) {
          if (!(!worldIn.isRemote && tileentity.onlyOpsCanSetNbt) || !(player == null || !player.canUseCommandBlock)) {
            val nbt = tileentity.write(new CompoundNBT)
            nbt.merge(subTag)
            nbt.putInt("x", pos.getX)
            nbt.putInt("y", pos.getY)
            nbt.putInt("z", pos.getZ)
            tileentity.read(nbt)
            tileentity.markDirty()
          }
        }
        if (stack.hasDisplayName) {
          tileentity match {
            case tank: TileTankNoDisplay => tank.stackName = stack.getDisplayName
            case _ =>
          }
        }
        return true
      }
    }
    false
  }

  override def tryPlace(context: BlockItemUseContext) = {
    if (Option(context.getPlayer).exists(_.abilities.isCreativeMode)) {
      val size = context.getItem.getCount
      val result = super.tryPlace(context)
      context.getItem.setCount(size)
      result
    } else {
      super.tryPlace(context)
    }
  }
}
