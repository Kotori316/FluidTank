package com.kotori316.fluidtank.blocks

import java.util

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.{Tiers, TileTankCreative}
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider

class BlockTankCreative extends AbstractTank {
  setRegistryName(FluidTank.modID, "blocktankcreative")
  setUnlocalizedName(FluidTank.modID + ".blocktankcreative")

  final val itemBlock = new ItemBlockTank(this, Tiers.CREATIVE.rank) {
    setRegistryName(FluidTank.modID, "blocktankcreative")

    override def addInformation(stack: ItemStack, worldIn: World, tooltip: util.List[String], flagIn: ITooltipFlag): Unit = {
      tooltip.add("Creative")
    }

    override def getModelResourceLocation(meta: Int) = new ModelResourceLocation(getRegistryName, "inventory")

    override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = null

    override def getUnlocalizedName(stack: ItemStack): String = "tile.fluidtank.blocktankcreative"

    override def hasRecipe: Boolean = false
  }

  override def getTierByMeta(meta: Int): Tiers = Tiers.CREATIVE

  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileTankCreative
}
