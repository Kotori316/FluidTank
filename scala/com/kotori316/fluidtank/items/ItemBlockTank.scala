package com.kotori316.fluidtank.items

import com.kotori316.fluidtank.Utils
import com.kotori316.fluidtank.blocks.BlockTank
import com.kotori316.fluidtank.tiles.Tiers
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.item.{EnumRarity, ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConverters._

class ItemBlockTank(val blockTank: BlockTank, val rank: Int) extends ItemBlock(blockTank) {
    setHasSubtypes(true)
    setCreativeTab(Utils.CREATIVE_TABS)

    override def getRarity(stack: ItemStack): EnumRarity =
        if (stack.hasTagCompound && stack.getTagCompound.hasKey("BlockEntityTag")) EnumRarity.RARE
        else EnumRarity.COMMON

    def getModelResouceLocation(meta: Int) = new ModelResourceLocation(getRegistryName + tierName(meta), "inventory")

    private def tierName(meta: Int) = blockTank.getTierByMeta(meta).toString.toLowerCase

    def itemList = (0 until Tiers.rankList(rank)).map(i => (this, Int.box(i)))

    def itemStream: java.util.stream.Stream[(ItemBlockTank, Integer)] = itemList.asJava.stream()

    override def getUnlocalizedName(stack: ItemStack): String = {
        super.getUnlocalizedName(stack) + "." + tierName(stack.getItemDamage)
    }

    override def getMetadata(damage: Int): Int = damage

    @SideOnly(Side.CLIENT)
    override def addInformation(stack: ItemStack, worldIn: World, tooltip: java.util.List[String], flagIn: ITooltipFlag): Unit = {
        val nbt = stack.getSubCompound("BlockEntityTag")
        if (nbt != null) {
            val tankNBT = nbt.getCompoundTag("tank")
            val fluid = Option(FluidStack.loadFluidStackFromNBT(tankNBT))
            val c = tankNBT.getInteger("capacity")
            tooltip.add(fluid.fold("Empty")(_.getLocalizedName) + " : " + fluid.fold(0)(_.amount) + " mB / " + c + " mB")
        } else {
            tooltip.add("Capacity : " + blockTank.getTierByMeta(stack.getItemDamage).amount + "mB")
        }
    }

    override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
        new TankItemFluidHander(stack)
    }

    override def placeBlockAt(stack: ItemStack, player: EntityPlayer, worldIn: World, pos: BlockPos,
                              side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, newState: IBlockState): Boolean = {
        if (!worldIn.setBlockState(pos, newState, 11)) return false

        val state = worldIn.getBlockState(pos)
        if (state.getBlock eq this.block) {
            if (worldIn.getMinecraftServer != null) {
                val nbttagcompound = stack.getSubCompound("BlockEntityTag")
                if (nbttagcompound != null) {
                    val tileentity = worldIn.getTileEntity(pos)
                    if (tileentity != null) {
                        if (!(!worldIn.isRemote && tileentity.onlyOpsCanSetNbt) || !(player == null || !player.canUseCommandBlock)) {
                            val nbttagcompound1 = tileentity.writeToNBT(new NBTTagCompound)
                            nbttagcompound1.merge(nbttagcompound)
                            nbttagcompound1.setInteger("x", pos.getX)
                            nbttagcompound1.setInteger("y", pos.getY)
                            nbttagcompound1.setInteger("z", pos.getZ)
                            tileentity.readFromNBT(nbttagcompound1)
                            tileentity.markDirty()
                        }
                    }
                }
                player match {
                    case p: EntityPlayerMP => CriteriaTriggers.PLACED_BLOCK.trigger(p, pos, stack)
                    case _ =>
                }
            }
        }
        true
    }
}
