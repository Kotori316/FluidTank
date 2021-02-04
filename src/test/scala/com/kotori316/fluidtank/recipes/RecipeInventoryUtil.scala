package com.kotori316.fluidtank.recipes

import com.kotori316.fluidtank.items.{ItemBlockTank, TankItemFluidHandler}
import com.kotori316.fluidtank.recipes.AccessRecipeTest.DummyContainer
import net.minecraft.inventory.CraftingInventory
import net.minecraft.item.ItemStack

private[recipes] object RecipeInventoryUtil {

  def getInv(s1: String = "", s2: String = "", s3: String = "", itemMap: Map[Char, ItemStack]): CraftingInventory = {
    val map = itemMap + (' ' -> ItemStack.EMPTY)
    val cf = new CraftingInventory(new DummyContainer(), 3, 3)

    require(s1.length <= 3 && s2.length <= 3 && s3.length <= 3, s"Over 4 elements are not allowed. ${(s1, s2, s3)}")
    require(s1.nonEmpty || s2.nonEmpty || s3.nonEmpty, "All Empty?")
    require(Set(s1, s2, s3).flatMap(_.toIterable).forall(map.keySet), s"Contains all keys, ${Set(s1, s2, s3).flatMap(_.toIterable)}")

    val itemList = (List(s1, s2, s3) zip (0 until 9 by 3))
      .flatMap { case (str, i) => str.zipWithIndex.map { case (c, i1) => (c, i + i1) } }
      .flatMap { case (c, i) => map.get(c).map((i, _)).toList }
    for ((index, stack) <- itemList) {
      cf.setInventorySlotContents(index, stack)
    }
    cf
  }

  def getFluidHandler(tankStack: ItemStack) = new TankItemFluidHandler(tankStack.getItem.asInstanceOf[ItemBlockTank], tankStack)
}
