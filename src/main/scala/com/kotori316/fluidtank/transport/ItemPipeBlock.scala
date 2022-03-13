package com.kotori316.fluidtank.transport

import com.kotori316.fluidtank.{ModObjects, Utils}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.world.Container
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType, HopperBlockEntity}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraftforge.items.CapabilityItemHandler

class ItemPipeBlock extends PipeBlock {
  override protected def getRegName = "item_pipe"

  override protected def isHandler(level: BlockGetter, pos: BlockPos, property: EnumProperty[PipeBlockConnection]): Boolean = {
    val d = PipeBlock.FACING_TO_PROPERTY_MAP.inverse.get(property)
    val maybeTilePos = pos.relative(d)
    val maybeTile = level.getBlockEntity(maybeTilePos)
    if (maybeTile != null) {
      val cap = maybeTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite)
      if (cap.isPresent) return true
    }
    // Technique to get world instance.
    val invPresent = for {
      entity <- Option(level.getBlockEntity(pos))
      inv <- Option(HopperBlockEntity.getContainerAt(entity.getLevel, maybeTilePos))
    } yield inv

    invPresent.isDefined
  }

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ModObjects.ITEM_PIPE_TYPE.create(pos, state)

  override protected def getConnection(direction: Direction, entity: BlockEntity) =
    if (entity.isInstanceOf[Container] || entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite).isPresent)
      PipeBlockConnection.CONNECTED
    else
      PipeBlockConnection.NO_CONNECTION

  override def getTicker[T <: BlockEntity](level: Level, state: BlockState, value: BlockEntityType[T]): BlockEntityTicker[T] =
    if (level.isClientSide) null
    else Utils.checkType(value, ModObjects.ITEM_PIPE_TYPE, (_, _, _, pipe: ItemPipeTile) => pipe.tick())
}