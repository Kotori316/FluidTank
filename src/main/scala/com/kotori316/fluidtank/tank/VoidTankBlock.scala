package com.kotori316.fluidtank.tank

import java.util

import net.minecraft.core.BlockPos
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.world.item.{ItemStack, TooltipFlag}
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class VoidTankBlock extends TankBlock(Tiers.VOID) {
  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = new TileTankVoid(pos, state)

  override def saveTankNBT(tileEntity: BlockEntity, stack: ItemStack): Unit = {
    Option(tileEntity)
      .collect { case tank: TileTankCreative => tank }
      .flatMap(_.getStackName)
      .foreach(stack.setHoverName)
  }

  override protected def createTankItem(): TankBlockItem = new TankBlockItem(this) {
    override def appendHoverText(stack: ItemStack, world: Level, tooltip: util.List[Component], context: TooltipFlag): Unit = {
      tooltip.add(new TextComponent("Void"))
    }

    override def hasRecipe: Boolean = false

  }
}
