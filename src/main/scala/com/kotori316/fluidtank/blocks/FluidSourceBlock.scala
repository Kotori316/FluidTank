package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank._
import com.kotori316.fluidtank.fluids.FluidAmount
import com.kotori316.fluidtank.items.FluidSourceItem
import com.kotori316.fluidtank.tiles.FluidSourceTile
import net.minecraft.core.{BlockPos, NonNullList}
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.{CreativeModeTab, Item, ItemStack, Items, TooltipFlag}
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityTicker, BlockEntityType}
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.{BaseEntityBlock, Block, RenderShape}
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.{BlockGetter, Level}
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.{InteractionHand, InteractionResult}
import org.jetbrains.annotations.Nullable

class FluidSourceBlock extends BaseEntityBlock(BlockBehaviour.Properties.of(Material.METAL).strength(0.5f)) {
  final val registryName = new ResourceLocation(FluidTank.modID, FluidSourceBlock.NAME)
  val itemBlock = new FluidSourceItem(this, new Item.Properties().tab(ModObjects.CREATIVE_TABS))
  registerDefaultState(this.getStateDefinition.any.setValue(FluidSourceBlock.CHEAT_MODE, Boolean.box(false)))

  //noinspection ScalaDeprecation,deprecation
  override final def getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ModObjects.SOURCE_TYPE.create(pos, state)

  override def setPlacedBy(level: Level, pos: BlockPos, state: BlockState, @Nullable entity: LivingEntity, stack: ItemStack): Unit = {
    super.setPlacedBy(level, pos, state, entity, stack)
    Option(level.getBlockEntity(pos)).foreach {
      case s: FluidSourceTile =>
        if (FluidSourceBlock.isCheatStack(stack)) {
          level.setBlockAndUpdate(pos, state.setValue(FluidSourceBlock.CHEAT_MODE, Boolean.box(true)))
          s.locked = false
        } else {
          level.setBlockAndUpdate(pos, state.setValue(FluidSourceBlock.CHEAT_MODE, Boolean.box(false)))
          s.fluid = FluidAmount.BUCKET_WATER
        }
      case _ =>
    }
  }

  override def getCloneItemStack(level: BlockGetter, pos: BlockPos, state: BlockState): ItemStack = {
    val stack = super.getCloneItemStack(level, pos, state)
    if (Option(level.getBlockEntity(pos)).collect { case s: FluidSourceTile => !s.locked }.getOrElse(false)) {
      stack.getOrCreateTag().putBoolean(FluidSourceBlock.KEY_CHEAT, true)
    }
    stack
  }

  override def fillItemCategory(group: CreativeModeTab, items: NonNullList[ItemStack]): Unit = {
    items.add(new ItemStack(this))
    val stack = new ItemStack(this)
    stack.getOrCreateTag().putBoolean(FluidSourceBlock.KEY_CHEAT, true)
    items.add(stack)
  }

  //noinspection ScalaDeprecation,deprecation
  override def use(state: BlockState, level: Level, pos: BlockPos, player: Player,
                   hand: InteractionHand, hit: BlockHitResult): InteractionResult = {
    if (FluidTank.config.enableFluidSupplier) {
      val stack = player.getItemInHand(hand)
      val fluid = FluidAmount.fromItem(stack)
      if (fluid.isEmpty) {
        stack.getItem match {
          case Items.BUCKET =>
            // Reset to empty.
            changeContent(level, pos, FluidAmount.EMPTY, player, hand == InteractionHand.MAIN_HAND)
            InteractionResult.SUCCESS
          case Items.CLOCK =>
            // Change interval time to push fluid.
            val i = if (hand == InteractionHand.MAIN_HAND) 1 else -1
            changeInterval(level, pos, stack.getCount * i, player)
            InteractionResult.SUCCESS
          case _ => InteractionResult.PASS
        }
      } else {
        changeContent(level, pos, fluid, player, hand == InteractionHand.MAIN_HAND)
        InteractionResult.SUCCESS
      }
    } else {
      if (!player.isCrouching) player.displayClientMessage(Component.literal("Fluid Supplier is disabled."), true)
      InteractionResult.PASS
    }
  }

  def changeContent(world: Level, pos: BlockPos, fluid: FluidAmount, player: Player, isMainHand: Boolean): Unit = if (!world.isClientSide) {
    world.getBlockEntity(pos) match {
      case s: FluidSourceTile =>
        val replace =
          if (s.fluid fluidEqual fluid) {
            val r1 = if (isMainHand) s.fluid + fluid else s.fluid - fluid
            if (r1.nonEmpty) r1 else FluidAmount.EMPTY
          } else if (fluid.isEmpty) {
            FluidAmount.EMPTY
          } else {
            fluid
          }
        s.fluid = replace
        if (!s.locked || fluid.fluidEqual(FluidAmount.BUCKET_WATER) || fluid.isEmpty)
          player.displayClientMessage(Component.translatable(FluidSourceBlock.CHANGE_SOURCE, s.fluid), false)
      case _ =>
    }
  }

  def changeInterval(world: Level, pos: BlockPos, dt: Int, player: Player): Unit = if (!world.isClientSide) {
    world.getBlockEntity(pos) match {
      case s: FluidSourceTile =>
        s.interval = Math.max(1, s.interval + dt)
        player.displayClientMessage(Component.translatable(FluidSourceBlock.CHANGE_INTERVAL, s.interval), false)
      case _ =>
    }
  }

  override def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit = {
    super.createBlockStateDefinition(builder)
    builder.add(FluidSourceBlock.CHEAT_MODE)
  }

  override def appendHoverText(stack: ItemStack, worldIn: BlockGetter, tooltip: java.util.List[Component], flagIn: TooltipFlag): Unit = {
    if (FluidTank.config.enableFluidSupplier) {
      tooltip.add(
        if (FluidSourceBlock.isCheatStack(stack)) {
          Component.translatable(FluidSourceBlock.TOOLTIP_INF)
        } else {
          Component.translatable(FluidSourceBlock.TOOLTIP)
        }
      )
    } else {
      tooltip.add(Component.translatable(FluidSourceBlock.TOOLTIP_DISABLED))
    }
  }

  override def getTicker[T <: BlockEntity](l: Level, s: BlockState, t: BlockEntityType[T]): BlockEntityTicker[T] = {
    if (l.isClientSide) {
      null
    } else {
      BaseEntityBlock.createTickerHelper[FluidSourceTile, T](t, ModObjects.SOURCE_TYPE, (_, _, _, tile) => tile.tick())
    }
  }
}

object FluidSourceBlock {
  final val NAME = "fluid_source"
  final val CHANGE_SOURCE = "chat.fluidtank.change_source"
  final val CHANGE_INTERVAL = "chat.fluidtank.change_interval"
  final val TOOLTIP = "tooltip.fluidtank.source"
  final val TOOLTIP_INF = "tooltip.fluidtank.source_inf"
  final val TOOLTIP_DISABLED = "tooltip.fluidtank.source_disabled"
  final val CHEAT_MODE = BooleanProperty.create("cheat_mode")
  val KEY_CHEAT = "unlocked"

  def isCheatStack(stack: ItemStack): Boolean = {
    Option(stack.getTag).exists(_.contains(FluidSourceBlock.KEY_CHEAT))
  }

}
