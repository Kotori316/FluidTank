package com.kotori316.fluidtank.blocks

import java.util

import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tier, TileTankCreative}
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.text.{ITextComponent, StringTextComponent}
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.ICapabilityProvider

class BlockCreativeTank extends BlockTank(Tier.CREATIVE) {
  override def namePrefix = ""

  override def createTileEntity(state: BlockState, world: IBlockReader) = new TileTankCreative

  override def saveTankNBT(tileEntity: TileEntity, stack: ItemStack): Unit = {
    // Just save custom name.
    Option(tileEntity).collect { case tank: TileTankCreative => tank.getStackName }.flatten
      .foreach(stack.setDisplayName)
  }

  override val itemBlock: ItemBlockTank = new ItemBlockTank(this) {
    @OnlyIn(Dist.CLIENT)
    override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
      tooltip.add(new StringTextComponent("Creative"))
    }

    override def hasInvisibleRecipe: Boolean = false

    override def initCapabilities(stack: ItemStack, nbt: CompoundNBT): ICapabilityProvider = null

  }
}
