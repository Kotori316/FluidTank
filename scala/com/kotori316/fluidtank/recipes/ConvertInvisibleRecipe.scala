package com.kotori316.fluidtank.recipes

import com.kotori316.fluidtank.FluidTank
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.{IRecipe, Ingredient}
import net.minecraft.util.NonNullList
import net.minecraft.world.World
import net.minecraftforge.registries.IForgeRegistryEntry

import scala.collection.JavaConverters._

object ConvertInvisibleRecipe extends IForgeRegistryEntry.Impl[IRecipe] with IRecipe {
    setRegistryName(FluidTank.modID + ":tankconvert")

    override def canFit(width: Int, height: Int) = width * height >= 1

    override def getCraftingResult(inv: InventoryCrafting) = {
        val stack = (for (i <- 0 until inv.getWidth;
                          j <- 0 until inv.getHeight) yield inv.getStackInRowAndColumn(i, j)).filterNot(_.isEmpty).headOption
        stack.map(_.copy()).map(s => {
            val d = s.getItemDamage
            if ((d & 8) == 8) s.setItemDamage(~(~d | 8))
            else s.setItemDamage(d | 8)
            s.setCount(1)
            s
        }).getOrElse(ItemStack.EMPTY)
    }

    override def matches(inv: InventoryCrafting, worldIn: World) = {
        val stacks = (for (i <- 0 until inv.getWidth;
                           j <- 0 until inv.getHeight) yield inv.getStackInRowAndColumn(i, j)).filterNot(_.isEmpty)
        stacks.size == 1 && TankIngredient.apply(stacks.head)
    }

    override def getRecipeOutput = new ItemStack(FluidTank.BLOCK_TANKS.get(0).itemBlock, 1, 8)


    override def getIngredients = NonNullList.from[Ingredient](TankIngredient)

    override def getGroup: String = FluidTank.modID + ":tankconvertrecipe"

    private final object TankIngredient extends Ingredient(
        FluidTank.BLOCK_TANKS.asScala
          .flatMap(_.itemBlock.itemList)
          .flatMap { case (i, m) => Seq(new ItemStack(i, 1, m), new ItemStack(i, 1, m | 8)) }: _*
    )

}
