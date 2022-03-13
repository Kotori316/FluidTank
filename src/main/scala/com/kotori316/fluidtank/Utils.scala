package com.kotori316.fluidtank

import java.lang.{Double => JDouble, Integer => JInt}
import java.util
import java.util.OptionalInt
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Supplier

import net.minecraft.core.{HolderSet, Registry}
import net.minecraft.nbt.CompoundTag
import net.minecraft.tags.TagKey
import net.minecraft.world.item.{DyeColor, DyeableLeatherItem, ItemStack}
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraftforge.fml.loading.FMLLoader
import org.apache.logging.log4j.Logger

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

object Utils {
  val BLOCK_ENTITY_TAG = "BlockEntityTag"

  def toInt(l: Long): Int = {
    val i = l.toInt
    if (i == l) i
    else if (l > 0) Integer.MAX_VALUE
    else Integer.MIN_VALUE
  }

  private val inDev = new AtomicInteger(-1)

  def isInDev: Boolean = {
    val i = inDev.get()
    if (i == -1) {
      inDev.set(if (!FMLLoader.isProduction || Config.content.debug.get()) 1 else 0)
      return inDev.get() == 1
    }
    i == 1
  }

  //noinspection ScalaUnusedSymbol,SpellCheckingInspection
  private def dummy(): Unit = {
    /*
    Cheat code to get filled tank.
    /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 16000L, fluid: "silents_mechanisms:diesel", capacity: 16000}}}
    /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 8000L, fluid: "silents_mechanisms:ethane", capacity: 16000}}}
    /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 8000L, fluid: "minecraft:water", capacity: 16000}}}
    /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 10000L, fluid: "fluidtank:vanilla_milk", capacity: 16000}}}
    /give @p fluidtank:tank_stone{BlockEntityTag:{tier: "Stone", id: "fluidtank:tiletank", tank: {amount: 10000L, fluid: "fluidtank:vanilla_milk", capacity: 16000}}}
    /give @p fluidtank:tank_stone{BlockEntityTag:{tier:"stone",tank:{amount:10000L,fluid:"fluidtank:vanilla_milk",capacity:16000}}}
    */
  }

  /**
   * Replacement of [[net.minecraft.world.item.BlockItem.setBlockEntityData]].
   * The method requires [[BlockEntityType]] as parameter to add "id", but it is not available in serializing tile data in item.
   * Then, the nbt of crafted tank and of removed tank will be different, which makes items un-stackable.
   * <p>
   * To solve this issue, the "id" should not be saved in item nbt. This is why I created this method.
   * <p>
   * This method will remove "BlockEntityTag" in stack tag if the `tileTag` is `null` or empty.
   *
   * @param stack   the stack where the nbt saved
   * @param tileTag The nbt provided by `BlockEntity.saveWithoutMetadata`.
   *                If `null` or empty, the nbt in stack will be removed instead of putting empty tag.
   */
  def setTileTag(stack: ItemStack, tileTag: CompoundTag): Unit =
    if (tileTag == null || tileTag.isEmpty) stack.removeTagKey(BLOCK_ENTITY_TAG)
    else stack.addTagElement(BLOCK_ENTITY_TAG, tileTag)

  /**
   * Helper method copied from [[net.minecraft.world.level.block.BaseEntityBlock]]
   */
  def checkType[E <: BlockEntity, A <: BlockEntity](type1: BlockEntityType[A], exceptedType: BlockEntityType[E], ticker: BlockEntityTicker[_ >: E]): BlockEntityTicker[A] =
    if (exceptedType eq type1) ticker.asInstanceOf[BlockEntityTicker[A]]
    else null

  def getItemColor(stack: ItemStack): OptionalInt = {
    Option(DyeColor.getColor(stack))
      .map(_.getMaterialColor.col)
      .orElse {
        stack.getItem match {
          case item: DyeableLeatherItem => Option(item.getColor(stack))
          case _ => None
        }
      }
      .toJavaPrimitive
  }

  def getLogger(clazz: Class[_]): Logger = getLogger(clazz.getName)

  def getLogger(name: String): Logger = try {
    val field = Class.forName("net.minecraftforge.fml.ModLoader").getDeclaredField("LOGGER")
    field.setAccessible(true)
    val loaderLogger = field.get(null).asInstanceOf[org.apache.logging.log4j.core.Logger]
    loaderLogger.getContext.getLogger(name)
  } catch {
    case e: ReflectiveOperationException =>
      throw new RuntimeException("Can't access to LOGGER in loader.", e)
  }

  def getTagElements[T](tag: TagKey[T]): util.Set[T] = {
    Registry.REGISTRY.entrySet().asScala
      .filter(e => tag.isFor(e.getKey))
      .map(_.getValue)
      .headOption
      .map(r => r.asInstanceOf[Registry[T]])
      .flatMap(r => r.getTag(tag).toScala)
      .toSet
      .flatMap((r: HolderSet.Named[T]) => r.iterator().asScala)
      .map(_.value())
      .asJava
  }

  def getTestInstance(configs: Map[String, Any]): TestConfig = new Utils.TestConfig(configs)

  def getTestInstance(configs: java.util.Map[String, Any]): TestConfig = getTestInstance(configs.asScala.toMap)

  class TestConfig(configs: Map[String, Any]) extends IContent {
    private def createBool(): Config.BoolSupplier = {
      val methodName = Thread.currentThread.getStackTrace.apply(2).getMethodName
      new Config.BoolSupplier() {
        override def get(): Boolean = configs.getOrElse(methodName, false).asInstanceOf[Boolean]
      }
    }

    private def createGeneric[T]: java.util.function.Supplier[T] = {
      val methodName = Thread.currentThread.getStackTrace.apply(2).getMethodName
      () => configs.getOrElse(methodName, 0).asInstanceOf[T]
    }

    override def removeRecipe: Config.BoolSupplier = createBool()

    override def debug: Config.BoolSupplier = createBool()

    override def easyRecipe: Config.BoolSupplier = createBool()

    override def usableUnavailableTankInRecipe: Config.BoolSupplier = createBool()

    override def showInvisibleTank: Config.BoolSupplier = createBool()

    override def showTOP: Config.BoolSupplier = createBool()

    override def enableWailaAndTOP: Config.BoolSupplier = createBool()

    override def enableFluidSupplier: Config.BoolSupplier = createBool()

    override def enablePipeRainbowRenderer: Config.BoolSupplier = createBool()

    override def pipeColor: Supplier[JInt] = createGeneric

    override def renderLowerBound: Supplier[JDouble] = createGeneric

    override def renderUpperBound: Supplier[JDouble] = createGeneric
  }
}