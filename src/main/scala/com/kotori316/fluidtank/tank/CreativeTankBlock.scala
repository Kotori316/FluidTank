package com.kotori316.fluidtank.tank

import java.util

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.{LiteralText, Text}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class CreativeTankBlock extends TankBlock(Tiers.CREATIVE) {

  override def createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new TileTankCreative(pos, state)

  override def saveTankNBT(tileEntity: BlockEntity, stack: ItemStack): Unit = {
    //     Just save custom name.
    Option(tileEntity).collect { case tank: TileTankCreative => tank.getStackName }.flatten
      .foreach(stack.setCustomName)
  }

  override val blockItem: TankBlockItem = new TankBlockItem(this) {
    override def appendTooltip(stack: ItemStack, world: World, tooltip: util.List[Text], context: TooltipContext): Unit = {
      tooltip.add(new LiteralText("Creative"))
    }

    override def hasRecipe: Boolean = false

  }
}
