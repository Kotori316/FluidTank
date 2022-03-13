package com.kotori316.fluidtank.transport

import com.google.common.collect.ImmutableBiMap
import com.kotori316.fluidtank.{BlockPosHelper, Config, FluidTank, ModObjects, Utils}
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.network.chat.{Component, TextComponent, TranslatableComponent}
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.{BlockItem, DyeColor, Item}
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.{BlockBehaviour, BlockState, StateDefinition}
import net.minecraft.world.level.block.{Block, EntityBlock}
import net.minecraft.world.level.material.{FluidState, Material}
import net.minecraft.world.level.{BlockGetter, Level, LevelAccessor}
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.{CollisionContext, Shapes, VoxelShape}
import net.minecraft.world.{InteractionHand, InteractionResult}

import scala.annotation.nowarn
import scala.jdk.CollectionConverters._

object PipeBlock {
  val BOX_AABB: VoxelShape = Shapes.box(0.25, 0.25, 0.25, 0.75, 0.75, 0.75)
  val North_AABB: VoxelShape = Shapes.box(0.25, 0.25, 0, 0.75, 0.75, 0.25)
  val South_AABB: VoxelShape = Shapes.box(0.25, 0.25, .75, 0.75, 0.75, 1)
  val West_AABB: VoxelShape = Shapes.box(0, 0.25, 0.25, .25, 0.75, 0.75)
  val East_AABB: VoxelShape = Shapes.box(.75, 0.25, 0.25, 1, 0.75, 0.75)
  val UP_AABB: VoxelShape = Shapes.box(0.25, .75, 0.25, 0.75, 1, 0.75)
  val Down_AABB: VoxelShape = Shapes.box(0.25, 0, 0.25, 0.75, .25, 0.75)
  val NORTH: EnumProperty[PipeBlockConnection] = EnumProperty.create("north", classOf[PipeBlockConnection])
  val SOUTH: EnumProperty[PipeBlockConnection] = EnumProperty.create("south", classOf[PipeBlockConnection])
  val WEST: EnumProperty[PipeBlockConnection] = EnumProperty.create("west", classOf[PipeBlockConnection])
  val EAST: EnumProperty[PipeBlockConnection] = EnumProperty.create("east", classOf[PipeBlockConnection])
  val UP: EnumProperty[PipeBlockConnection] = EnumProperty.create("up", classOf[PipeBlockConnection])
  val DOWN: EnumProperty[PipeBlockConnection] = EnumProperty.create("down", classOf[PipeBlockConnection])
  private val SHAPE_MAP = ImmutableBiMap.of(
    PipeBlock.NORTH, North_AABB,
    PipeBlock.SOUTH, South_AABB,
    PipeBlock.WEST, West_AABB,
    PipeBlock.EAST, East_AABB,
    PipeBlock.UP, UP_AABB,
    PipeBlock.DOWN, Down_AABB,
  )
  val FACING_TO_PROPERTY_MAP: ImmutableBiMap[Direction, EnumProperty[PipeBlockConnection]] = ImmutableBiMap.of(
    Direction.NORTH, NORTH,
    Direction.SOUTH, SOUTH,
    Direction.WEST, WEST,
    Direction.EAST, EAST,
    Direction.UP, UP,
    Direction.DOWN, DOWN,
  )

  def getPropertyFromDirection(facing: Direction): EnumProperty[PipeBlockConnection] = FACING_TO_PROPERTY_MAP.get(facing)
}

abstract class PipeBlock extends Block(BlockBehaviour.Properties.of(ModObjects.MATERIAL_PIPE).strength(0.5f)) with EntityBlock {
  setRegistryName(FluidTank.modID, getRegName)
  this.registerDefaultState(getStateDefinition.any
    .setValue(PipeBlock.NORTH, PipeBlockConnection.NO_CONNECTION)
    .setValue(PipeBlock.SOUTH, PipeBlockConnection.NO_CONNECTION)
    .setValue(PipeBlock.WEST, PipeBlockConnection.NO_CONNECTION)
    .setValue(PipeBlock.EAST, PipeBlockConnection.NO_CONNECTION)
    .setValue(PipeBlock.UP, PipeBlockConnection.NO_CONNECTION)
    .setValue(PipeBlock.DOWN, PipeBlockConnection.NO_CONNECTION)
    //            .with(WATERLOGGED, false)
  )
  private final val blockItem = new BlockItem(this, new Item.Properties().tab(ModObjects.CREATIVE_TABS))
  blockItem.setRegistryName(FluidTank.modID, getRegName)

  def itemBlock: BlockItem = blockItem

  protected def getRegName: String

  protected def isHandler(level: BlockGetter, pos: BlockPos, property: EnumProperty[PipeBlockConnection]): Boolean

  override def newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity

  override protected def createBlockStateDefinition(builder: StateDefinition.Builder[Block, BlockState]): Unit = {
    builder.add(PipeBlock.NORTH, PipeBlock.EAST, PipeBlock.SOUTH, PipeBlock.WEST, PipeBlock.UP, PipeBlock.DOWN /*, WATERLOGGED*/)
  }

  //noinspection ScalaDeprecation
  override def getShape(state: BlockState, worldIn: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = {
    PipeBlock.SHAPE_MAP.entrySet.asScala
      .filter(e => state.getValue(e.getKey) != PipeBlockConnection.NO_CONNECTION || isHandler(worldIn, pos, e.getKey))
      .map(_.getValue)
      .fold(PipeBlock.BOX_AABB)(Shapes.or)
  }

  override def getStateForPlacement(context: BlockPlaceContext): BlockState = {
    val worldIn = context.getLevel
    val pos = context.getClickedPos
    this.defaultBlockState
      .setValue(PipeBlock.NORTH, canConnectTo(worldIn, pos.north, Direction.NORTH))
      .setValue(PipeBlock.EAST, canConnectTo(worldIn, pos.east, Direction.EAST))
      .setValue(PipeBlock.SOUTH, canConnectTo(worldIn, pos.south, Direction.SOUTH))
      .setValue(PipeBlock.WEST, canConnectTo(worldIn, pos.west, Direction.WEST))
      .setValue(PipeBlock.DOWN, canConnectTo(worldIn, pos.below, Direction.DOWN))
      .setValue(PipeBlock.UP, canConnectTo(worldIn, pos.above, Direction.UP))
    //            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
  }

  //noinspection ScalaDeprecation
  override def updateShape(stateIn: BlockState, facing: Direction, facingState: BlockState, worldIn: LevelAccessor, currentPos: BlockPos, facingPos: BlockPos): BlockState = {
    /*if (stateIn.get(WATERLOGGED)) {
               worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
     }*/
    val now = stateIn.getValue(PipeBlock.getPropertyFromDirection(facing))
    if (facingState.getBlock == this) {
      val value = facingState.getValue(PipeBlock.getPropertyFromDirection(facing.getOpposite))
      stateIn.setValue(PipeBlock.getPropertyFromDirection(facing), value)
    } else {
      val value = canConnectTo(worldIn, currentPos.relative(facing), facing)
      if (value is PipeBlockConnection.NO_CONNECTION) {
        if (facingState.getMaterial == Material.AIR || facingState.getMaterial.isLiquid) {
          stateIn.setValue(PipeBlock.getPropertyFromDirection(facing), value)
        } else {
          stateIn
        }
      } else if (value.hasConnection ^ now.hasConnection) {
        stateIn.setValue(PipeBlock.getPropertyFromDirection(facing), value)
      } else {
        stateIn
      }
    }
  }

  private def canConnectTo(level: BlockGetter, pos: BlockPos, direction: Direction): PipeBlockConnection = {
    val blockState = level.getBlockState(pos)
    val entity = level.getBlockEntity(pos)
    if (blockState.getBlock == this) {
      entity match {
        case p: PipeTileBase if !Config.content.enablePipeRainbowRenderer.get() =>
          if (p.getColor == Config.content.pipeColor.get) PipeBlockConnection.CONNECTED
          else PipeBlockConnection.NO_CONNECTION
        case _ => PipeBlockConnection.CONNECTED
      }
    } else {
      if (entity != null) getConnection(direction, entity)
      else PipeBlockConnection.NO_CONNECTION
    }
  }

  protected def getConnection(direction: Direction, entity: BlockEntity): PipeBlockConnection

  private def getPipeTile(worldIn: Level, pos: BlockPos): Option[PipeTileBase] =
    Option(worldIn.getBlockEntity(pos)).collect { case t: PipeTileBase => t }

  //noinspection ScalaDeprecation
  override def use(state: BlockState, worldIn: Level, pos: BlockPos, player: Player, handIn: InteractionHand, hit: BlockHitResult): InteractionResult = {
    if (player.getItemInHand(handIn).getItem.isInstanceOf[BlockItem] || player.isCrouching)
      return InteractionResult.PASS
    // Dying pipe.
    val maybeColor = Utils.getItemColor(player.getItemInHand(handIn))
    if (maybeColor.isPresent && !Config.content.enablePipeRainbowRenderer.get()) {
      if (!worldIn.isClientSide) {
        getPipeTile(worldIn, pos).foreach(_.changeColor(maybeColor.getAsInt))
        val colorName: Component = DyeColor.values.find(_.getMaterialColor.col == maybeColor.getAsInt).map(c => new TranslatableComponent("color.minecraft." + c))
          .getOrElse(new TextComponent(String.format("#%06x", maybeColor.getAsInt)))
        player.displayClientMessage(new TranslatableComponent("chat.fluidtank.change_color", colorName), false)
      }
      return InteractionResult.SUCCESS
    }

    // Modifying pipe connection.
    val d = hit.getLocation.subtract(pos.getX, pos.getY, pos.getZ)
    val blockState = PipeBlock.SHAPE_MAP.entrySet.asScala
      .filter { e =>
        val box = e.getValue.bounds
        box.minX <= d.x && box.maxX >= d.x && box.minY <= d.y && box.maxY >= d.y && box.minZ <= d.z && box.maxZ >= d.z
      }
      .map(_.getKey)
      .headOption
      .map { p =>
        if (worldIn.getBlockState(pos.relative(PipeBlock.FACING_TO_PROPERTY_MAP.inverse.get(p))).getBlock != this)
          (state.cycle(p), false)
        else (state.setValue(p, PipeBlockConnection.onOffConnection(state.getValue(p))), true)
      }
    blockState match {
      case Some((newState, update)) =>
        if (!worldIn.isClientSide) {
          worldIn.setBlockAndUpdate(pos, newState)
          if (update)
            getPipeTile(worldIn, pos).foreach(_.connectorUpdate())
        }
        InteractionResult.SUCCESS
      case None =>
        super.use(state, worldIn, pos, player, handIn, hit): @nowarn("cat=deprecation")
    }
  }

  //noinspection ScalaDeprecation
  override def neighborChanged(state: BlockState, worldIn: Level, pos: BlockPos, blockIn: Block, fromPos: BlockPos, isMoving: Boolean): Unit = {
    super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving): @nowarn("cat=deprecation")
    val fromState = worldIn.getBlockState(fromPos)
    // Update connection between pipes.
    if (fromState.getBlock != this) {
      val vec = fromPos.sub(pos)
      val direction = Direction.fromNormal(vec.getX, vec.getY, vec.getZ)
      if (direction != null) {
        if (fromState.getValue(PipeBlock.getPropertyFromDirection(direction.getOpposite)) == PipeBlockConnection.NO_CONNECTION)
          worldIn.setBlockAndUpdate(pos, state.setValue(PipeBlock.getPropertyFromDirection(direction), PipeBlockConnection.NO_CONNECTION))
        else if (fromState.getValue(PipeBlock.getPropertyFromDirection(direction.getOpposite)) == PipeBlockConnection.CONNECTED)
          worldIn.setBlockAndUpdate(pos, state.setValue(PipeBlock.getPropertyFromDirection(direction), PipeBlockConnection.CONNECTED))
      }
    }
    // Update handlers
    if (!worldIn.isClientSide) {
      getPipeTile(worldIn, pos).foreach(_.removeCapCache(fromPos))
    }
  }

  //noinspection ScalaDeprecation
  override def onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, moved: Boolean): Unit =
    if (state.getBlock != newState.getBlock) {
      getPipeTile(level, pos).foreach(_.connectorUpdate())
      super.onRemove(state, level, pos, newState, moved): @nowarn("cat=deprecation")
    }

  //noinspection ScalaDeprecation
  override def getFluidState(state: BlockState): FluidState = {
    /*state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) :*/
    super.getFluidState(state): @nowarn("cat=deprecation")
  }
}