package com.kotori316.fluidtank.tiles

import com.kotori316.fluidtank.fluids.{GenericAmount, ListHandler}
import net.minecraft.core.BlockPos

trait ConnectionHelper[TileType] {
  type Content
  type Handler <: ListHandler[Content]
  type ConnectionType <: Connection[TileType]

  def getPos(t: TileType): BlockPos

  def isCreative(t: TileType): Boolean

  def isVoid(t: TileType): Boolean

  def setChanged(t: TileType): Unit

  /**
   * @param t the tile
   * @return the amount in the tank. None if it contains empty amount.
   */
  final def getContent(t: TileType): Option[GenericAmount[Content]] = {
    val c = getContentRaw(t)
    if (c.isEmpty) Option.empty
    else Option(c)
  }

  /**
   * Get the content in the tank. If the tank contains nothing, returns empty amount.
   *
   * @param t the tile
   * @return the content. Empty amount if the tank contains nothing.
   */
  def getContentRaw(t: TileType): GenericAmount[Content]

  def defaultAmount: GenericAmount[Content]

  def createHandler(s: Seq[TileType]): Handler

  def createConnection(s: Seq[TileType]): ConnectionType

  def connectionSetter(connection: ConnectionType): TileType => Unit
}

object ConnectionHelper {

  type Aux[TileType, ContentType, HandlerType <: ListHandler[ContentType]] = ConnectionHelper[TileType] {
    type Content = ContentType
    type Handler = HandlerType
  }

  implicit final class ConnectionHelperMethods[T](private val t: T)(implicit val helper: ConnectionHelper[T]) {

    def getPos: BlockPos = helper.getPos(t)

    def isCreative: Boolean = helper isCreative t

    def isVoid: Boolean = helper isVoid t

    def setChanged(): Unit = helper.setChanged(t)

    /**
     * @param h implicit parameter of the connection helper, required to get the type of content type.
     *          [[helper]] doesn't have the concrete type info.
     * @return Some(content) if the tank contains valid amount. None if it contains nothing or invalid(amount <= 0) amount.
     */
    def getContent[C](implicit h: ConnectionHelper.Aux[T, C, _]): Option[GenericAmount[C]] = h.getContent(t)
  }
}
