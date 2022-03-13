package com.kotori316.fluidtank.tiles

import java.util.Objects

import com.kotori316.fluidtank.network.{FluidCacheMessage, PacketHandler}
import com.kotori316.fluidtank.{FluidTank, ModObjects}
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.{AbstractContainerMenu, Slot}
import net.minecraft.world.item.ItemStack

object CATContainer {
  final val GUI_ID = FluidTank.modID + ":gui_chest_as_tank"
}

class CATContainer(id: Int, player: Player, pos: BlockPos) extends AbstractContainerMenu(ModObjects.CAT_CONTAINER_TYPE, id) {
  final val catTile = player.getCommandSenderWorld.getBlockEntity(pos).asInstanceOf[CATTile]
  Objects.requireNonNull(catTile)
  val oneBox = 18
  for (h <- 0 until 3; v <- 0 until 9) {
    this.addSlot(new Slot(player.getInventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox))
  }
  for (vertical <- 0 until 9) {
    this.addSlot(new Slot(player.getInventory, vertical, 8 + vertical * oneBox, 142))
  }

  override def stillValid(player: Player): Boolean = catTile.getBlockPos.distToCenterSqr(player.position) < 64

  override def broadcastChanges(): Unit = {
    super.broadcastChanges()
    PacketHandler.sendToClient(new FluidCacheMessage(catTile), catTile.getLevel)
  }

  override def quickMoveStack(playerIn: Player, index: Int): ItemStack = {
    ItemStack.EMPTY
  }
}