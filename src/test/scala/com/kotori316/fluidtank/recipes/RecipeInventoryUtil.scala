package com.kotori316.fluidtank.recipes

import com.kotori316.fluidtank.items.{ItemBlockTank, ReservoirItem, TankItemFluidHandler}
import com.kotori316.fluidtank.recipes.AccessRecipeTest.DummyContainer
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack

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
}
