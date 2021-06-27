package com.kotori316.fluidtank.recipes

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.items.ItemBlockTank
import net.minecraft.init.Blocks
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.{IRecipe, Ingredient}
import net.minecraft.util.NonNullList
import net.minecraft.world.World
import net.minecraftforge.registries.IForgeRegistryEntry


object EmptyTankRecipe extends IForgeRegistryEntry.Impl[IRecipe] with IRecipe {
  setRegistryName(FluidTank.modID + ":tankempty")
  val ingredientList: NonNullList[Ingredient] = {
    val l = NonNullList.withSize(9, Ingredient.EMPTY)
    for (i <- 0 until l.size() if i != 4) {
      l.set(i, Ingredient.fromStacks(new ItemStack(Blocks.OBSIDIAN)))
    }
    l.set(4, TankIngredient)
    l
  }

  override def getCraftingResult(inv: InventoryCrafting): ItemStack = {
    val item = inv.getStackInSlot(4)
    item.getItem match {
      case tank: ItemBlockTank => new ItemStack(tank, 1, item.getItemDamage)
      case _ => FluidTank.LOGGER.warn("Invalid item to EmptyTankRecipe")
        getRecipeOutput.copy()
    }
  }

  override def matches(inv: InventoryCrafting, worldIn: World): Boolean = {
    Range(0, ingredientList.size()).forall(i =>
      ingredientList.get(i).apply(inv.getStackInSlot(i))
        && (i != 4 || inv.getStackInSlot(i).hasTagCompound)
    )
  }

  override def canFit(width: Int, height: Int): Boolean = width >= 3 && height >= 3

  override def getRecipeOutput: ItemStack = new ItemStack(BlockTank.blockTank1)

  override def getRemainingItems(inv: InventoryCrafting): NonNullList[ItemStack] = NonNullList.withSize(inv.getSizeInventory, ItemStack.EMPTY)

  override def getGroup: String = FluidTank.modID + ":tankemptyrecipe"

  override def getIngredients: NonNullList[Ingredient] = ingredientList

  override def isDynamic: Boolean = true
}
