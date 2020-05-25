package com.kotori316.fluidtank.recipes

import com.kotori316.fluidtank.FluidTank
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.{IRecipe, Ingredient}
import net.minecraft.util.NonNullList
import net.minecraft.world.World
import net.minecraftforge.registries.IForgeRegistryEntry

object ConvertInvisibleRecipe extends IForgeRegistryEntry.Impl[IRecipe] with IRecipe {
  setRegistryName(FluidTank.modID + ":tankconvert")

  override def canFit(width: Int, height: Int) = width * height >= 1

  override def getCraftingResult(inv: InventoryCrafting) = {
    val stack = getItemInInv(inv).headOption
    stack.map(_.copy()).map(s => {
      val d = s.getItemDamage
      if ((d & 8) == 8) s.setItemDamage(~(~d | 8))
      else s.setItemDamage(d | 8)
      s.setCount(1)
      s
    }).getOrElse(ItemStack.EMPTY)
  }

  override def matches(inv: InventoryCrafting, worldIn: World) = {
    val stacks = getItemInInv(inv)
    stacks.size == 1 && TankIngredient.apply(stacks.head)
  }

  private def getItemInInv(inv: InventoryCrafting) = {
    for {
      i <- 0 until inv.getWidth
      j <- 0 until inv.getHeight
      stack = inv.getStackInRowAndColumn(i, j)
      if !stack.isEmpty
    } yield stack
  }

  override def getRemainingItems(inv: InventoryCrafting): NonNullList[ItemStack] = NonNullList.withSize(inv.getSizeInventory, ItemStack.EMPTY)

  override def getRecipeOutput = new ItemStack(FluidTank.BLOCK_TANKS.get(0).itemBlock, 1, 8)

  override def getIngredients: NonNullList[Ingredient] = NonNullList.from(TankIngredient)

  override def getGroup: String = FluidTank.modID + ":tankconvertrecipe"

  override def isDynamic: Boolean = true
}
