package com.kotori316.fluidtank.network

import cats.Eval
import com.kotori316.fluidtank.blocks.FluidSourceBlock
import com.kotori316.fluidtank.render.{ModelWrapper, RenderItemTank, RenderPipe, RenderReservoirItem, RenderTank}
import com.kotori316.fluidtank.tiles.{CATContainer, CATScreen}
import com.kotori316.fluidtank.{Config, FluidTank, ModObjects}
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.blockentity.{BlockEntityRendererProvider, BlockEntityRenderers}
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.renderer.{ItemBlockRenderTypes, RenderType}
import net.minecraft.client.resources.model.ModelResourceLocation
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.client.event.{ModelBakeEvent, ModelRegistryEvent, TextureStitchEvent}
import net.minecraftforge.common.util.LogicalSidedProvider
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.network.NetworkEvent

import scala.jdk.OptionConverters._

@OnlyIn(Dist.CLIENT)
object ClientProxy {
  val RENDER_ITEM_TANK: Eval[RenderItemTank] = Eval.later(new RenderItemTank)
  val RENDER_ITEM_RESERVOIR: Eval[RenderReservoirItem] = Eval.later(new RenderReservoirItem)
  var whiteTexture: TextureAtlasSprite = _

  //noinspection ScalaUnusedSymbol
  private def createPipeRenderer(d: BlockEntityRendererProvider.Context): RenderPipe = {
    val renderPipe = new RenderPipe
    renderPipe.useColor = !Config.content.enablePipeRainbowRenderer.get()
    renderPipe
  }
}

@OnlyIn(Dist.CLIENT)
class ClientProxy extends SideProxy {
  override def getLevel(context: NetworkEvent.Context): Option[Level] = {
    val serverWorld = Option(context.getSender).map(_.getCommandSenderWorld)
    serverWorld.orElse(LogicalSidedProvider.CLIENTWORLD.get(context.getDirection.getReceptionSide).toScala)
  }

  override def getTankProperties: Item.Properties = new Item.Properties().tab(ModObjects.CREATIVE_TABS)

  override def getReservoirProperties: Item.Properties = new Item.Properties().tab(ModObjects.CREATIVE_TABS)

  //noinspection ScalaUnusedSymbol
  @SubscribeEvent
  def registerTESR(event: FMLClientSetupEvent): Unit = {
    BlockEntityRenderers.register(ModObjects.TANK_TYPE, d => new RenderTank(d))
    BlockEntityRenderers.register(ModObjects.TANK_CREATIVE_TYPE, d => new RenderTank(d))
    BlockEntityRenderers.register(ModObjects.FLUID_PIPE_TYPE, ClientProxy.createPipeRenderer)
    BlockEntityRenderers.register(ModObjects.ITEM_PIPE_TYPE, ClientProxy.createPipeRenderer)
    MenuScreens.register[CATContainer, CATScreen](ModObjects.CAT_CONTAINER_TYPE, (m, i, c) => new CATScreen(m, i, c))
    val renderType = RenderType.cutoutMipped
    ModObjects.blockTanks.foreach(tank => ItemBlockRenderTypes.setRenderLayer(tank, renderType))
    ItemBlockRenderTypes.setRenderLayer(ModObjects.blockFluidPipe, renderType)
    ItemBlockRenderTypes.setRenderLayer(ModObjects.blockItemPipe, renderType)
    // Item Properties Override
    ItemProperties.register(ModObjects.blockSource.itemBlock, new ResourceLocation(FluidTank.modID, "source_cheat"),
      (stack, _, _, _) => if (FluidSourceBlock.isCheatStack(stack)) 1f else 0f)
  }

  //noinspection ScalaUnusedSymbol
  @SubscribeEvent
  def registerModels(event: ModelRegistryEvent): Unit = {
  }

  @SubscribeEvent
  def onBake(event: ModelBakeEvent): Unit = {
    ModObjects.itemReservoirs
      .map(_.getRegistryName)
      .map(n => new ModelResourceLocation(n, "inventory"))
      .foreach(n => event.getModelRegistry.put(n, new ModelWrapper(event.getModelManager.getModel(n))))
  }

  @SubscribeEvent
  def registerTexture(event: TextureStitchEvent.Pre): Unit =
    if (event.getAtlas.location == InventoryMenu.BLOCK_ATLAS)
      event.addSprite(new ResourceLocation(FluidTank.modID, "blocks/white"))

  @SubscribeEvent
  def putTexture(event: TextureStitchEvent.Post): Unit =
    if (event.getAtlas.location == InventoryMenu.BLOCK_ATLAS)
      ClientProxy.whiteTexture = event.getAtlas.getSprite(new ResourceLocation(FluidTank.modID, "blocks/white"))
}