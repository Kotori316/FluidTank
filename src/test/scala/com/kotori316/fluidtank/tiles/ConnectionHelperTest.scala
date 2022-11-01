package com.kotori316.fluidtank.tiles

import cats.implicits.catsSyntaxEq
import com.kotori316.fluidtank.fluids.{GenericAccess, GenericAmount, GenericAmountTest, ListHandler, Tank, drainList, fillList}
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.{Component, TextComponent}
import net.minecraftforge.fluids.capability.IFluidHandler
import org.junit.jupiter.api.{Assertions, Test}

class ConnectionHelperTest {
  private implicit val a: GenericAccess[String] = new GenericAmountTest().GenericAccessString

  @Test
  def instance(): Unit = {
    val connection = new StringConnection(Seq.empty[StringTile])
    Assertions.assertEquals(0, connection.amount)
  }

  @Test
  def contentOfEmpty(): Unit = {
    val connection = new StringConnection(Seq.empty[StringTile])
    val c = connection.content
    val s: String = c.c
    Assertions.assertEquals("", s)
    Assertions.assertTrue(s === "")
  }

  @Test
  def contentOfOne(): Unit = {
    val connection = new StringConnection(Seq(tile(BlockPos.ZERO, "a")))
    val s = connection.content.c
    Assertions.assertEquals("a", s)
    Assertions.assertTrue(s === "a")
  }

  @Test
  def contentOfTwo(): Unit = {
    val connection = new StringConnection(Seq(tile(BlockPos.ZERO, "a"), tile(BlockPos.ZERO.above(), "b")))
    val s = connection.content.c
    Assertions.assertEquals("a", s)
    Assertions.assertTrue(s === "a")
  }

  @Test
  def instanceTypeCheck(): Unit = {
    val s1: ConnectionHelper.Aux[StringTile, String, StringHandler]#Content = "String"
    val h1: ConnectionHelper.Aux[StringTile, String, StringHandler]#Handler = StringHandler(Seq.empty)
    val s2: String = "String"
    val s3: SCH.Content = "String"

    Assertions.assertNotNull(s1)
    Assertions.assertNotNull(h1)
    Assertions.assertTrue(s1 === s2)
    Assertions.assertTrue(s3 === s2)
  }

  implicit val SCH: ConnectionHelper.Aux[StringTile, String, StringHandler] = StringConnectionHelper

  class StringConnection(s: Seq[StringTile])(override implicit val helper: ConnectionHelper.Aux[StringTile, String, StringHandler]) extends Connection[StringTile](s) {
    def content = this.contentType

    override def getTextComponent: Component = new TextComponent(s.map(_.tank.content).mkString(", "))
  }

  case class StringTile(pos: BlockPos, var tank: Tank[String], var connection: Option[StringConnection])

  def tile(pos: BlockPos, content: String): StringTile = {
    StringTile(pos, Tank(new GenericAmount[String](content, 1, None), 100), Option.empty)
  }

  case class StringHandler(s: Seq[StringTile]) extends ListHandler[String] {
    override def getSumOfCapacity: Long = s.map(_.tank.content.length).sum

    override def fill(resource: GenericAmount[String], action: IFluidHandler.FluidAction): GenericAmount[String] = {
      val op = fillList(s.map(_.tank).toList)
      val (_, left, newTanks) = op.run((), resource)
      val moved = resource - left
      if (action.execute()) {
        (s zip newTanks).foreach { case (tile, tank) => tile.tank = tank }
      }
      moved
    }

    override def drain(toDrain: GenericAmount[String], action: IFluidHandler.FluidAction): GenericAmount[String] = {
      val op = drainList(s.map(_.tank).toList)
      val (_, left, newTanks) = op.run((), toDrain)
      val moved = toDrain - left
      if (action.execute()) {
        (s zip newTanks).foreach { case (tile, tank) => tile.tank = tank }
      }
      moved
    }
  }

  private object StringConnectionHelper extends ConnectionHelper[StringTile] {
    override type Content = String
    override type Handler = StringHandler
    override type ConnectionType = StringConnection

    override def getPos(t: StringTile): BlockPos = t.pos

    override def isCreative(t: StringTile): Boolean = false

    override def isVoid(t: StringTile): Boolean = false

    override def setChanged(t: StringTile): Unit = ()

    override def getContentRaw(t: StringTile): GenericAmount[String] = t.tank.genericAmount

    override def defaultAmount: GenericAmount[String] = new GenericAmount(a.empty, 0, None)

    override def createHandler(s: Seq[StringTile]): StringHandler = StringHandler(s)

    override def createConnection(s: Seq[StringTile]): StringConnection = new StringConnection(s.sortBy(_.pos.getY))

    override def connectionSetter(connection: StringConnection): StringTile => Unit = {
      t => t.connection = Some(connection)
    }

  }
}
