package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.tiles.TileTankNoDisplay
import com.kotori316.fluidtank.{FluidAmount, FluidTank, Utils}
import net.minecraft.block.state.IBlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item._
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{ITextComponent, TextComponentString}
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.ICapabilityProvider

class ItemBlockTank(val blockTank: BlockTank) extends ItemBlock(blockTank, FluidTank.proxy.getTankProperties) {
  setRegistryName(FluidTank.modID, blockTank.namePrefix + blockTank.tier.toString.toLowerCase)

  override def getRarity(stack: ItemStack): EnumRarity =
    if (stack.hasTag && stack.getTag.contains(TileTankNoDisplay.NBT_BlockTag)) EnumRarity.RARE
    else EnumRarity.COMMON

  def hasRecipe = true

  @OnlyIn(Dist.CLIENT)
  override def addInformation(stack: ItemStack, worldIn: World, tooltip: java.util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
    val nbt = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)
    if (nbt != null) {
      val tankNBT = nbt.getCompound(TileTankNoDisplay.NBT_Tank)
      val fluid = Option(FluidAmount.fromNBT(tankNBT))
      val c = tankNBT.getInt(TileTankNoDisplay.NBT_Capacity)
      tooltip.add(new TextComponentString(fluid.fold("Empty")(_.getLocalizedName) + " : " + fluid.fold(0L)(_.amount) + " mB / " + c + " mB"))
    } else {
      tooltip.add(new TextComponentString("Capacity : " + blockTank.tier.amount + "mB"))
    }
  }

  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
    if (true) return null // 1.13.2
    new TankItemFluidHandler(this, stack)
  }

  override def onBlockPlaced(pos: BlockPos, worldIn: World, player: EntityPlayer, stack: ItemStack, newState: IBlockState): Boolean = {
    if (worldIn.getServer != null) {
      val tileentity = worldIn.getTileEntity(pos)
      if (tileentity != null) {
        val subTag = stack.getChildTag(TileTankNoDisplay.NBT_BlockTag)
        if (subTag != null) {
          if (!(!worldIn.isRemote && tileentity.onlyOpsCanSetNbt) || !(player == null || !player.canUseCommandBlock)) {
            val nbt = tileentity.write(new NBTTagCompound)
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

object ItemBlockTank {
  def saveFluid(stack: ItemStack, amount: FluidAmount): Unit = {
    stack.getItem match {
      case t: ItemBlockTank =>
        if (amount.nonEmpty) {
          val tiers = t.blockTank.tier
          val tag = new NBTTagCompound
          tag.put(TileTankNoDisplay.NBT_Tier, tiers.toNBTTag)
          val tankTag = new NBTTagCompound
          tankTag.putInt(TileTankNoDisplay.NBT_Capacity, Utils.toInt(tiers.amount))
          amount.write(tankTag)
          tag.put(TileTankNoDisplay.NBT_Tank, tankTag)

          stack.getOrCreateTag().put(TileTankNoDisplay.NBT_BlockTag, tag)
        } else {
          stack.removeChildTag(TileTankNoDisplay.NBT_BlockTag)
        }
      case _ =>
    }
  }

  def loadFluid(stack: ItemStack): FluidAmount = {
    stack.getItem match {
      case _: ItemBlockTank =>
        Option(stack.getTag)
          .map(_.getCompound(TileTankNoDisplay.NBT_BlockTag))
          .map(_.getCompound(TileTankNoDisplay.NBT_Tank))
          .map(FluidAmount.fromNBT)
          .getOrElse(FluidAmount.EMPTY)
      case _ => FluidAmount.EMPTY
    }
  }

  def getCapacity(stack: ItemStack): Int = {
    stack.getItem match {
      case t: ItemBlockTank => Utils.toInt(t.blockTank.tier.amount)
      case _ => 0
    }
  }
}