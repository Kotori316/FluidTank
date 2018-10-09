package com.kotori316.fluidtank

import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient

import scala.collection.JavaConverters._

package object recipes {

    object TankIngredient extends Ingredient(
        FluidTank.BLOCK_TANKS.asScala
          .flatMap(_.itemBlock.itemList)
          .flatMap { case (i, m) if i.hasRecipe => Seq(new ItemStack(i, 1, m), new ItemStack(i, 1, m | 8)) }: _*
    )

}
