package com.kotori316.fluidtank.blocks

import java.util

import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tier, TileTankCreative}
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraft.world.item.{ItemStack, TooltipFlag}
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.ICapabilityProvider

class BlockCreativeTank extends BlockTank(Tier.CREATIVE) {
  override def namePrefix = ""

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new TileTankCreative(pos, state)

  override def saveTankNBT(tileEntity: BlockEntity, stack: ItemStack): Unit = {
    // Just save custom name.
    Option(tileEntity).collect { case tank: TileTankCreative => tank.getStackName }.flatten
      .foreach(stack.setHoverName)
  }

  override protected def createTankItem() = new ItemBlockTank(this) {
    @OnlyIn(Dist.CLIENT)
    override def appendHoverText(stack: ItemStack, worldIn: Level, tooltip: util.List[Component], flagIn: TooltipFlag): Unit = {
      tooltip.add(new TextComponent("Creative"))
    }

    override def hasInvisibleRecipe: Boolean = false

    override def initCapabilities(stack: ItemStack, nbt: CompoundTag): ICapabilityProvider = null

  }
}
