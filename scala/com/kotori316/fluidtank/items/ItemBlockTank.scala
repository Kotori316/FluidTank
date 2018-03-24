package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.Utils
import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.tiles.Tiers
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.item.{EnumRarity, ItemBlock, ItemStack}

import scala.collection.JavaConverters._

class ItemBlockTank(block: BlockTank, val rank: Int) extends ItemBlock(block) {
    setHasSubtypes(true)
    setCreativeTab(Utils.CREATIVE_TABS)

    override def getRarity(stack: ItemStack): EnumRarity =
        if (stack.hasTagCompound && stack.getTagCompound.hasKey("BlockEntityTag")) EnumRarity.RARE
        else EnumRarity.COMMON

    def getModelResouceLocation(meta: Int) = new ModelResourceLocation(getRegistryName + tierName(meta), "inventory")

    private def tierName(meta: Int) = block.getTierByMeta(meta).toString.toLowerCase

    def itemList: java.util.List[((ItemBlockTank, Integer))] = (0 until Tiers.rankList(rank)).map(i => (this, Int.box(i))).asJava

    override def getUnlocalizedName(stack: ItemStack): String = {
        super.getUnlocalizedName(stack) + "." + tierName(stack.getItemDamage)
    }

    override def getMetadata(damage: Int): Int = damage
}
