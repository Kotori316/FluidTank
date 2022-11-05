package com.kotori316.fluidtank.integration.mekanism_gas

import cats.data.Chain
import com.kotori316.fluidtank.tiles.{Connection, ConnectionHelper, Tier}
import mekanism.api.chemical.IChemicalHandler
import mekanism.api.chemical.gas.{Gas, GasStack}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.{Component, TextComponent, TranslatableComponent}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.LazyOptional

class GasConnection(s: Seq[TileGasTank])(override implicit val helper: ConnectionHelper.Aux[TileGasTank, Gas, GasListHandler]) extends Connection[TileGasTank](s) {
  override def getTextComponent: Component = {
    new TranslatableComponent("chat.fluidtank.connection",
      getContent.map(_.toStack.getTextComponent).getOrElse(new TranslatableComponent("chat.fluidtank.empty")),
      Long.box(amount),
      Long.box(capacity),
      Int.box(getComparatorLevel))
  }

  private final val gasCap: LazyOptional[IChemicalHandler[Gas, GasStack]] = LazyOptional.of(() => this.handler)

  override protected def invalidate(): Unit = {
    super.invalidate()
    this.gasCap.invalidate()
  }

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    if (cap == Constant.GAS_HANDLER_CAPABILITY) {
      this.gasCap.cast()
    } else {
      super.getCapability(cap, side)
    }
  }
}

object GasConnection {
  implicit final val GasConnectionHelper: ConnectionHelper.Aux[TileGasTank, Gas, GasListHandler] = GasConnectionHelperImpl

  def invalid: GasConnection = new InvalidGasConnection

  private class InvalidGasConnection extends GasConnection(Nil) {
    override val isDummy = true

    override protected def contentType: GasAmount = GasAmount.EMPTY

    override def capacity: Long = 0

    override def amount: Long = 0

    override val toString: String = "GasConnection.Invalid"

    override def getComparatorLevel: Int = 0

    override def remove(tileTank: TileGasTank): Unit = ()

    override def getTextComponent: Component = new TextComponent(toString)
  }

  private object GasConnectionHelperImpl extends ConnectionHelper[TileGasTank] {
    override type Content = Gas
    override type Handler = GasListHandler
    override type ConnectionType = GasConnection

    override def getPos(t: TileGasTank): BlockPos = t.getBlockPos

    override def isCreative(t: TileGasTank): Boolean = t.tier == Tier.CREATIVE

    override def isVoid(t: TileGasTank): Boolean = t.tier == Tier.VOID

    override def setChanged(t: TileGasTank): Unit = t.setChanged()

    override def getContentRaw(t: TileGasTank): GasAmount = t.tileInfo.getHolder match {
      case holder: TileInfo.Holder => holder.gasTankHandler.getTank.genericAmount
      case _ => defaultAmount
    }

    override def defaultAmount: GasAmount = GasAmount.EMPTY

    override def createHandler(s: Seq[TileGasTank]): GasListHandler = new GasListHandler(Chain.fromSeq(
      s.map(_.tileInfo.getHolder).collect { case holder: TileInfo.Holder => holder.gasTankHandler }
    ))

    override def createConnection(s: Seq[TileGasTank]): GasConnection = {
      Connection.updatePosPropertyAndCreateConnection[TileGasTank, GasConnection](s, s => new GasConnection(s), invalid)
    }

    override def connectionSetter(connection: GasConnection): TileGasTank => Unit =
      t => t.tileInfo.getHolder match {
        case holder: TileInfo.Holder =>
          holder.gasConnection.invalidate()
          holder.gasConnection = connection
          t.setChanged()
        case _ =>
      }
  }

}
