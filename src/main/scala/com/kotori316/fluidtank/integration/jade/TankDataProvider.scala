package com.kotori316.fluidtank.integration.jade

import com.kotori316.fluidtank.integration.Localize._
import com.kotori316.fluidtank.integration.jade.TankWailaPlugin._
import com.kotori316.fluidtank.tiles.{TileTank, TileTankVoid}
import javax.annotation.Nonnull
import mcp.mobius.waila.api.config.IPluginConfig
import mcp.mobius.waila.api.{BlockAccessor, IComponentProvider, IServerDataProvider, ITooltip}
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.{TextComponent, TranslatableComponent}
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * Add details of tank with data from server.
 * This class must not contain any fields because the instance is not same in client side and server side.
 * The data should be transferred via packets with NBT, just added to given tag.
 */
final class TankDataProvider extends IServerDataProvider[BlockEntity] with IComponentProvider {
  override def appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig): Unit = {
    accessor.getBlockEntity match {
      case tank: TileTank if config.get(KEY_TANK_INFO) =>
        val nbtData = accessor.getServerData
        val list =
          if (config.get(KEY_SHORT_INFO))
            if (tank.isInstanceOf[TileTankVoid])
              Seq.empty
            else if (!nbtData.contains(NBT_Creative))
              Seq(new TranslatableComponent(WAILA_SHORT,
                tank.internalTank.getTank.fluidAmount.getLocalizedName,
                tank.internalTank.getTank.amount,
                tank.internalTank.getTank.capacity))
            else if (!nbtData.getBoolean(NBT_Creative))
              Seq(new TranslatableComponent(WAILA_SHORT,
                nbtData.getString(NBT_ConnectionFluidName),
                nbtData.getLong(NBT_ConnectionAmount),
                nbtData.getLong(NBT_ConnectionCapacity)))
            else
              Option(getCreativeFluidName(tank))
                .filter(_ != FLUID_NULL)
                .map(new TextComponent(_))
                .toSeq
          else {
            val tier = tank.tier
            if (tank.isInstanceOf[TileTankVoid])
              Seq(new TranslatableComponent(TIER, tier.toString))
            else if (!nbtData.getBoolean(NBT_Creative)) Seq(
              new TranslatableComponent(TIER, tier.toString),
              new TranslatableComponent(CONTENT, nbtData.getString(NBT_ConnectionFluidName)),
              new TranslatableComponent(AMOUNT, nbtData.getLong(NBT_ConnectionAmount)),
              new TranslatableComponent(CAPACITY, nbtData.getLong(NBT_ConnectionCapacity)),
              new TranslatableComponent(COMPARATOR, nbtData.getInt(NBT_ConnectionComparator))
            )
            else
              Seq(new TranslatableComponent(TIER, tier.toString), new TranslatableComponent(CONTENT, getCreativeFluidName(tank)))
          }
        list.foreach(tooltip.add)
      case _ =>
    }
  }

  override def appendServerData(tag: CompoundTag, player: ServerPlayer, world: Level, e: BlockEntity, b: Boolean): Unit = {
    e match {
      case tank: TileTank =>
        tag.putString(NBT_Tier, tank.tier.toString)
        if (tank.isInstanceOf[TileTankVoid]) return
        tag.putString(NBT_ConnectionFluidName, tank.connection.getFluidStack.filter(_.nonEmpty).map(_.getLocalizedName).getOrElse(FLUID_NULL))
        if (!tank.connection.hasCreative) {
          tag.putBoolean(NBT_Creative, false)
          tag.putLong(NBT_ConnectionAmount, tank.connection.amount)
          tag.putLong(NBT_ConnectionCapacity, tank.connection.capacity)
          tag.putInt(NBT_ConnectionComparator, tank.getComparatorLevel)
        }
        else tag.putBoolean(NBT_Creative, true)
      case _ =>
    }
  }

  @Nonnull
  private def getCreativeFluidName(tank: TileTank) =
    Option(tank.internalTank.getTank.fluidAmount).filter(_.nonEmpty).map(_.getLocalizedName).getOrElse(FLUID_NULL)

}