package com.kotori316.fluidtank.tank

import java.util

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.{LiteralText, Text}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class VoidTankBlock extends TankBlock(Tiers.VOID) {
  override def createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new TileTankVoid(pos, state)

  override def saveTankNBT(tileEntity: BlockEntity, stack: ItemStack): Unit = {
    Option(tileEntity)
      .collect { case tank: TileTankCreative => tank }
      .flatMap(_.getStackName)
      .foreach(stack.setCustomName)
  }

  override val blockItem: TankBlockItem = new TankBlockItem(this) {
    override def appendTooltip(stack: ItemStack, world: World, tooltip: util.List[Text], context: TooltipContext): Unit = {
      tooltip.add(new LiteralText("Void"))
    }

    override def hasRecipe: Boolean = false

  }
}
