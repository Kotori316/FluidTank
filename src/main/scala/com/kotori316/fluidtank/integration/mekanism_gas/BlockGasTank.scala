package com.kotori316.fluidtank.integration.mekanism_gas

import com.kotori316.fluidtank.blocks.TankPos
import com.kotori316.fluidtank.integration.mekanism_gas.Constant.isMekanismLoaded
import com.kotori316.fluidtank.network.SideProxy
import com.kotori316.fluidtank.tiles.Tier
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.{Block, EntityBlock}
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.{CollisionContext, VoxelShape}
import net.minecraft.world.{InteractionHand, InteractionResult}
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
      new TileGasTank(pPos, pState, tier)
    } else {
      null
    }
  }

  @scala.annotation.nowarn("cat=deprecation") //noinspection ScalaDeprecation,deprecation
  override def use(pState: BlockState, pLevel: Level, pPos: BlockPos, pPlayer: Player, pHand: InteractionHand, pHit: BlockHitResult): InteractionResult = {
    if (!isMekanismLoaded) {
      pPlayer.displayClientMessage(new TextComponent("Invalid request."), false)
      return InteractionResult.PASS
    }
    pLevel.getBlockEntity(pPos) match {
      case tileGasTank: TileGasTank =>
        if (pPlayer.getMainHandItem.isEmpty) {
          if (SideProxy.isServer(tileGasTank))
            pPlayer.displayClientMessage(tileGasTank.tileInfo.getMessage, false)
        }
        InteractionResult.SUCCESS
      case _ => super.use(pState, pLevel, pPos, pPlayer, pHand, pHit)
    }
  }

  @scala.annotation.nowarn("cat=deprecation") //noinspection ScalaDeprecation,deprecation
  override final def onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, moved: Boolean): Unit = {
    if (!state.is(newState.getBlock)) {
      if (isMekanismLoaded) {
        level.getBlockEntity(pos) match {
          case tank: TileGasTank => tank.onRemoved()
          case tile => FluidTank.LOGGER.error(ModObjects.MARKER_TileGasTank, "There is not TileGasTank at the pos : " + pos + " but " + tile)
        }
      }
      super.onRemove(state, level, pos, newState, moved)
    }
  }

  override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit = {
    super.createBlockStateDefinition(builder)
    builder.add(TankPos.TANK_POS_PROPERTY)
  }
}
