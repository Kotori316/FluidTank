package com.kotori316.fluidtank.blocks

import java.util

import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tiers, TileTankCreative}
import net.minecraft.block.state.IBlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.text.{ITextComponent, TextComponentString}
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.common.capabilities.ICapabilityProvider

class BlockCreativeTank extends BlockTank(Tiers.CREATIVE) {
  override def namePrefix = ""

  override def createTileEntity(state: IBlockState, world: IBlockReader) = new TileTankCreative

  override protected def saveTankNBT(tileEntity: TileEntity, stack: ItemStack): Unit = {
    // Just save custom name.
    Option(tileEntity).collect { case tank: TileTankCreative => tank.getStackName }.flatten
      .foreach(stack.setDisplayName)
  }

  override val itemBlock = new ItemBlockTank(this) {
    override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
      tooltip.add(new TextComponentString("Creative"))
    }

    override def hasRecipe: Boolean = false

    override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = null

  }
}
