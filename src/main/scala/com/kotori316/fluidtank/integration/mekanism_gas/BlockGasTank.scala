package com.kotori316.fluidtank.integration.mekanism_gas

import com.kotori316.fluidtank.blocks.TankPos
import com.kotori316.fluidtank.integration.mekanism_gas.Constant.isMekanismLoaded
import com.kotori316.fluidtank.tiles.Tier
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import net.minecraft.core.BlockPos
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState}
import net.minecraft.world.level.block.{Block, EntityBlock}
import org.jetbrains.annotations.Nullable

class BlockGasTank(val tier: Tier) extends Block(BlockBehaviour.Properties.of(ModObjects.MATERIAL).strength(1f).dynamicShape())
  with EntityBlock {
  setRegistryName(FluidTank.modID, "gas_tank_" + tier.toString.toLowerCase)
  registerDefaultState(this.getStateDefinition.any.setValue(TankPos.TANK_POS_PROPERTY, TankPos.SINGLE))
  final val itemBlock = new ItemBlockGasTank(this)

  override final def asItem(): Item = itemBlock

  @Nullable
  override def newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity = {
    if (isMekanismLoaded) {
      ???
    } else {
      null
    }
  }
}
