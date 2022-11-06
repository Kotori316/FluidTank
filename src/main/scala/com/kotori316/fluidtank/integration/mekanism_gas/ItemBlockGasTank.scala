package com.kotori316.fluidtank.integration.mekanism_gas

import com.kotori316.fluidtank.FluidTank
import com.kotori316.fluidtank.integration.Localize
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.{Component, TextComponent, TranslatableComponent}
import net.minecraft.world.item.{BlockItem, ItemStack, Rarity, TooltipFlag}
import net.minecraft.world.level.Level
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.ICapabilityProvider
import org.jetbrains.annotations.Nullable

class ItemBlockGasTank(val blockGasTank: BlockGasTank)
  extends BlockItem(blockGasTank, FluidTank.proxy.getTankProperties) {
  setRegistryName(FluidTank.modID, "gas_tank_" + blockGasTank.tier.toString.toLowerCase)

  override def toString: String = String.valueOf(getRegistryName)

  override def getRarity(stack: ItemStack): Rarity =
    if (BlockItem.getBlockEntityData(stack) != null) Rarity.RARE
    else Rarity.COMMON

  @OnlyIn(Dist.CLIENT)
  override def appendHoverText(stack: ItemStack, @Nullable level: Level, tooltip: java.util.List[Component], flag: TooltipFlag): Unit = {
    if (Constant.isMekanismLoaded) {
      val nbt = BlockItem.getBlockEntityData(stack)
      if (nbt != null) {
        TileInfo.addItemDescription(nbt, tooltip)
      } else {
        tooltip.add(new TranslatableComponent(Localize.CAPACITY, blockGasTank.tier.amount))
      }
    } else {
      tooltip.add(new TextComponent(f"${ChatFormatting.RED}Gas Tank is unavailable.${ChatFormatting.RESET}"))
    }
  }

  @Nullable
  override def initCapabilities(stack: ItemStack, nbt: CompoundTag): ICapabilityProvider = {
    if (Constant.isMekanismLoaded) {
      new GasCapProvider(blockGasTank.tier, stack)
    } else {
      super.initCapabilities(stack, nbt)
    }
  }
}
