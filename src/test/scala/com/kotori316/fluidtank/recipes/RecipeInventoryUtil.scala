package com.kotori316.fluidtank.recipes

import com.kotori316.fluidtank.ModObjects
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.items.{ItemBlockTank, ReservoirItem, TankItemFluidHandler}
import com.kotori316.fluidtank.tiles.Tier
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.{AbstractContainerMenu, CraftingContainer, Slot}
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fluids.capability.IFluidHandler

object RecipeInventoryUtil {

  def getInv(s1: String = "", s2: String = "", s3: String = "", itemMap: Map[Char, ItemStack]): CraftingContainer = {
    val map = itemMap + (' ' -> ItemStack.EMPTY)
    val cf = new CraftingContainer(new DummyContainer(), 3, 3)

    require(s1.length <= 3 && s2.length <= 3 && s3.length <= 3, s"Over 4 elements are not allowed. ${(s1, s2, s3)}")
    require(s1.nonEmpty || s2.nonEmpty || s3.nonEmpty, "All Empty?")
    require(Set(s1, s2, s3).flatMap(_.toSeq).forall(map.keySet), s"Contains all keys, ${Set(s1, s2, s3).flatMap(_.toSeq)}")

    val itemList = (List(s1, s2, s3) zip (0 until 9 by 3))
      .flatMap { case (str, i) => str.zipWithIndex.map { case (c, i1) => (c, i + i1) } }
      .flatMap { case (c, i) => map.get(c).map((i, _)).toList }
    for ((index, stack) <- itemList) {
      cf.setItem(index, stack)
    }
    cf
  }

  def getFluidHandler(stack: ItemStack): TankItemFluidHandler = stack.getItem match {
    case tank: ItemBlockTank => new TankItemFluidHandler(tank.blockTank.tier, stack)
    case reservoir: ReservoirItem => new TankItemFluidHandler(reservoir.tier, stack)
    case _ => throw new IllegalArgumentException(s"Stack $stack has no valid handler")
  }

  def getFilledTankStack(tier: Tier, fluid: FluidAmount): ItemStack = {
    val stack = new ItemStack(ModObjects.tierToBlock(tier))
    val handler = getFluidHandler(stack)
    handler.fill(fluid.toStack, IFluidHandler.FluidAction.EXECUTE)
    stack
  }

  final class DummyContainer extends AbstractContainerMenu(null, 35) {
    val inventory = new SimpleContainer(9)
    for (i <- 0 until inventory.getContainerSize) {
      addSlot(new Slot(inventory, i, 0, 0))
    }

    override def stillValid(player: Player) = false

    override def quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack = ItemStack.EMPTY
  }
}
