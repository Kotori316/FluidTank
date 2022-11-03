package com.kotori316.fluidtank.integration.mekanism_gas

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.network.SideProxy
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.server.TickTask
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.{LazyOptional, LogicalSidedProvider}
import net.minecraftforge.fml.LogicalSide

class TileGasTank(p: BlockPos, s: BlockState, var tier: Tier) extends BlockEntity(ModObjects.GAS_TANK_TYPE, p, s) {
  def this(p: BlockPos, s: BlockState) {
    this(p, s, Tier.Invalid)
  }

  val tileInfo: TileInfo = new TileInfo(this)

  def setTier(t: Tier): Unit = {
    this.tier = t
    this.tileInfo.updateInfo()
  }

  override def onLoad(): Unit = {
    super.onLoad()
    if (SideProxy.isServer(this) && Constant.isMekanismLoaded) {
      val executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)
      executor.tell(new TickTask(0, TileInfo.loadTask(this)))
    }
  }

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    val t = tileInfo.getCapability(cap, side)
    if (t.isPresent) {
      t
    } else {
      super.getCapability(cap, side)
    }

  }
}
