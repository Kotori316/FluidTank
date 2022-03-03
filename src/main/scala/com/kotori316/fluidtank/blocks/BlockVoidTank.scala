package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tier, TileTankVoid}
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.{ItemStack, TooltipFlag}
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.ICapabilityProvider

class BlockVoidTank extends BlockTank(Tier.VOID) {
  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ModObjects.TANK_VOID_TYPE.create(pos, state)

  override def saveTankNBT(tileEntity: BlockEntity, stack: ItemStack): Unit = {
    // Just save custom name.
    Option(tileEntity).collect { case tank: TileTankVoid => tank.getStackName }.flatten
      .foreach(stack.setHoverName)
  }

  override protected def createTankItem() = new ItemBlockTank(this) {
    @OnlyIn(Dist.CLIENT)
    override def appendHoverText(stack: ItemStack, worldIn: Level, tooltip: java.util.List[Component], flagIn: TooltipFlag): Unit = ()

    override def hasInvisibleRecipe: Boolean = false

    override def initCapabilities(stack: ItemStack, nbt: CompoundTag): ICapabilityProvider = null

  }
}
