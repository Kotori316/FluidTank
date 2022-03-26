package com.kotori316.fluidtank.recipes

import cats.syntax.foldable._
import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.fluids.FluidKey
import com.kotori316.fluidtank.items.{ItemBlockTank, TankItemFluidHandler}
import com.kotori316.fluidtank.{Config, ModObjects, Utils}
import net.minecraft.core.NonNullList
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.{CustomRecipe, Ingredient, RecipeSerializer, SimpleRecipeSerializer}
import net.minecraft.world.level.Level
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.items.ItemHandlerHelper

final class CombineRecipe(location: ResourceLocation) extends CustomRecipe(location) {

  import CombineRecipe._

  LOGGER.debug("Recipe instance of ConvertInvisibleRecipe({}) was created.", location)

  override def canCraftInDimensions(width: Int, height: Int): Boolean = width * height >= 2

  override def getSerializer: RecipeSerializer[_] = SERIALIZER

  override def matches(inv: CraftingContainer, worldIn: Level): Boolean = {
    val tanks = tankList()
    val isAllItemTank = (0 until inv.getContainerSize)
      .map(inv.getItem)
      .filterNot(_.isEmpty)
      .forall(tanks.test)
    if (!isAllItemTank) return false
    val tankCount = (0 until inv.getContainerSize)
      .map(inv.getItem)
      .filterNot(_.isEmpty)
      .count(tanks.test)
    if (tankCount < 2) return false
    // Check all tanks have the same fluid.
    val fluids = (0 until inv.getContainerSize)
      .map(inv.getItem)
      .flatMap(s => getHandler(s).map(_.getFluid))
    val allSameFluid = fluids
      .map(FluidKey.from)
      .filter(_.isDefined)
      .distinct.lengthIs == 1
    if (!allSameFluid) return false
    val totalAmount = fluids.map(_.amount).sum
    getMaxCapacityTank(inv).exists { case (_, capacity) => capacity >= totalAmount }
  }

  override def assemble(inv: CraftingContainer): ItemStack = {
    val fluid = (0 until inv.getContainerSize).toList
      .map(inv.getItem)
      .flatMap(s => getHandler(s).map(_.getFluid))
      .filter(_.nonEmpty)
      .combineAll
    val tank = for {
      (tankItem, _) <- getMaxCapacityTank(inv)
      handler <- getHandler(ItemHandlerHelper.copyStackWithSize(tankItem, 1))
    } yield {
      handler.drain(Int.MaxValue, IFluidHandler.FluidAction.EXECUTE)
      handler.fill(fluid.toStack, IFluidHandler.FluidAction.EXECUTE)
      handler.getContainer
    }
    tank.getOrElse(ItemStack.EMPTY)
  }

  override def getRemainingItems(inv: CraftingContainer): NonNullList[ItemStack] = {
    var maxTank: ItemStack = getMaxCapacityTank(inv).map(_._1).getOrElse(ItemStack.EMPTY)
    val nn = NonNullList.withSize(inv.getContainerSize, ItemStack.EMPTY)
    for (i <- 0 until nn.size()) {
      val item = inv.getItem(i)
      if (item sameItem maxTank) {
        maxTank = ItemStack.EMPTY
      } else {
        val leave = getHandler(ItemHandlerHelper.copyStackWithSize(item, 1))
          .map { h => h.drain(Int.MaxValue, IFluidHandler.FluidAction.EXECUTE); h.getContainer }
          .getOrElse(item)
        nn.set(i, leave)
      }
    }
    nn
  }

  private def tankList(): Ingredient = {
    val filter = if (Config.content.usableUnavailableTankInRecipe.get()) {
      (t: BlockTank) => t.tier.isNormalTier
    } else {
      (t: BlockTank) => t.tier.isNormalTier && t.tier.hasWayToCreate
    }
    Ingredient.of(ModObjects.blockTanks.filter(filter).map(new ItemStack(_)): _*)
  }

  private def getMaxCapacityTank(inv: CraftingContainer): Option[(ItemStack, Long)] = {
    getMaxCapacityTank((0 until inv.getContainerSize).map(inv.getItem))
  }

  private def getMaxCapacityTank(stacks: Seq[ItemStack]): Option[(ItemStack, Long)] = {
    stacks
      .filterNot(_.isEmpty)
      .flatMap(getHandler)
      .maxByOption(_.getCapacity)
      .map(f => (f.getContainer.copy(), f.getCapacity))
  }

  private def getHandler(stack: ItemStack): Option[TankItemFluidHandler] = {
    stack.getItem match {
      case tankItem: ItemBlockTank => Option(new TankItemFluidHandler(tankItem.blockTank.tier, stack))
      case _ => None
    }
  }
}

object CombineRecipe {
  private final val LOGGER = Utils.getLogger(classOf[CombineRecipe])
  final val SERIALIZER = new SimpleRecipeSerializer(new CombineRecipe(_))
  final val LOCATION = "fluidtank:combine_tanks"
}
