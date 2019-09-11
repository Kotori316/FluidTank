package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.items.ItemBlockTank
import com.kotori316.fluidtank.tiles.CATTile
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, BlockContainer}
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.init.Items
import net.minecraft.item._
import net.minecraft.state.StateContainer
import net.minecraft.state.properties.BlockStateProperties.FACING
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{BlockRenderLayer, EnumBlockRenderType, EnumFacing, EnumHand}
import net.minecraft.world.{IBlockReader, World}
import net.minecraftforge.fml.network.NetworkHooks

class BlockCAT extends BlockContainer(Block.Properties.create(ModObjects.MATERIAL).hardnessAndResistance(0.7f)) {
  // Chest as Tank
  setRegistryName(FluidTank.modID, BlockCAT.NAME)
  val itemBlock = new ItemBlock(this, new Item.Properties().group(ModObjects.CREATIVE_TABS))
  itemBlock.setRegistryName(FluidTank.modID, BlockCAT.NAME)
  setDefaultState(getStateContainer.getBaseState.`with`(FACING, EnumFacing.NORTH))

  override def fillStateContainer(builder: StateContainer.Builder[Block, IBlockState]): Unit = builder.add(FACING)

  override final def getRenderType(state: IBlockState): EnumBlockRenderType = EnumBlockRenderType.MODEL

  override def createNewTileEntity(worldIn: IBlockReader): TileEntity = new CATTile

  override final def getRenderLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT

  override def getStateForPlacement(context: BlockItemUseContext): IBlockState = this.getDefaultState.`with`(FACING, context.getNearestLookingDirection)

  override def onBlockActivated(state: IBlockState, worldIn: World, pos: BlockPos, player: EntityPlayer, hand: EnumHand,
                                side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    val stack = player.getHeldItem(hand)
    if (!FluidAmount.fromItem(stack).isEmpty && stack.getCount == 1) {
      // Fill chest.
      if (!worldIn.isRemote) {
        val filled = worldIn.getTileEntity(pos).asInstanceOf[CATTile].fillToChest(FluidAmount.fromItem(stack))
        stack.getItem match {
          case _: ItemBucket =>
            if (filled.nonEmpty) {
              player.setHeldItem(hand, stack.getContainerItem)
            }
          case _: ItemBlockTank =>
            ItemBlockTank.saveFluid(stack, FluidAmount.fromItem(stack) - filled)
            player.setHeldItem(hand, stack)
          case _ =>
        }
      }
      true
    } else if (stack.getCount == 1 && (stack.getItem == Items.BUCKET || (stack.getItem.isInstanceOf[ItemBlockTank] && !stack.getItem.asInstanceOf[ItemBlockTank].getBlock.isInstanceOf[BlockCreativeTank]))) {
      // Drain from chest.
      if (!worldIn.isRemote) {
        val amount = stack.getItem match {
          case _: ItemBucket => FluidAmount.AMOUNT_BUCKET
          case _: ItemBlockTank => ItemBlockTank.getCapacity(stack) - FluidAmount.fromItem(stack).amount.toInt
          case _ => FluidAmount.AMOUNT_BUCKET
        }
        val drained = worldIn.getTileEntity(pos).asInstanceOf[CATTile].drainFromChest(amount)
        if (drained.nonEmpty)
          stack.getItem match {
            case _: ItemBucket =>
              player.setHeldItem(hand, new ItemStack(drained.fluid.getFilledBucket))
            case _: ItemBlockTank =>
              ItemBlockTank.saveFluid(stack, FluidAmount.fromItem(stack) + drained)
            case _ =>
          }
      }
      true
    } else {
      if (!player.isSneaking) {
        if (!worldIn.isRemote)
          NetworkHooks.openGui(player.asInstanceOf[EntityPlayerMP], worldIn.getTileEntity(pos).asInstanceOf[CATTile], pos)
        true
      } else {
        false
      }
    }
  }
}

object BlockCAT {
  final val NAME = "chest_as_tank"
}
