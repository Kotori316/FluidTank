package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tiers, TileTankNoDisplay}
import net.minecraft.block.BlockState
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.ICapabilityProvider

class BlockVoidTank extends BlockTank(Tiers.VOID) {
  override def createTileEntity(state: BlockState, world: IBlockReader): TileEntity = ModObjects.TANK_VOID_TYPE.create()

  override def saveTankNBT(tileEntity: TileEntity, stack: ItemStack): Unit = {
    // Just save custom name.
    Option(tileEntity).collect { case tank: TileTankNoDisplay => tank.getStackName }.flatten
      .foreach(stack.setDisplayName)
  }

  override val itemBlock: ItemBlockTank = new ItemBlockTank(this) {
    @OnlyIn(Dist.CLIENT)
    override def addInformation(stack: ItemStack, worldIn: World, tooltip: java.util.List[ITextComponent], flagIn: ITooltipFlag): Unit = ()

    override def hasInvisibleRecipe: Boolean = false

    override def initCapabilities(stack: ItemStack, nbt: CompoundNBT): ICapabilityProvider = null

  }
}
