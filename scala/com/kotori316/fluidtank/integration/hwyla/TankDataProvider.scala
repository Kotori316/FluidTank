package com.kotori316.fluidtank.integration.hwyla

import java.util.{List => JList}

import mcp.mobius.waila.api.{IWailaConfigHandler, IWailaDataAccessor, IWailaDataProvider}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.Optional

@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = "waila")
class TankDataProvider extends IWailaDataProvider {
  override def getWailaHead(itemStack: ItemStack, tooltip: JList[String],
                            accessor: IWailaDataAccessor, config: IWailaConfigHandler): JList[String] = {
    super.getWailaHead(itemStack, tooltip, accessor, config)
  }

  override def getWailaBody(itemStack: ItemStack, tooltip: JList[String],
                            accessor: IWailaDataAccessor, config: IWailaConfigHandler): JList[String] = {
    super.getWailaBody(itemStack, tooltip, accessor, config)
  }

  override def getWailaTail(itemStack: ItemStack, tooltip: JList[String],
                            accessor: IWailaDataAccessor, config: IWailaConfigHandler): JList[String] = {
    super.getWailaTail(itemStack, tooltip, accessor, config)
  }

  override def getNBTData(player: EntityPlayerMP, te: TileEntity, tag: NBTTagCompound,
                          world: World, pos: BlockPos): NBTTagCompound = {
    super.getNBTData(player, te, tag, world, pos)
  }
}
