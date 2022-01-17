package com.kotori316.fluidtank;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.fluidtank.integration.FabricFluidTankStorage;
import com.kotori316.fluidtank.milk.MilkFluid;
import com.kotori316.fluidtank.packet.PacketHandler;
import com.kotori316.fluidtank.recipe.RecipeConfigCondition;
import com.kotori316.fluidtank.recipe.TankRecipe;
import com.kotori316.fluidtank.recipe.TierRecipe;
import com.kotori316.fluidtank.tank.ContentTankSerializer;
import com.kotori316.fluidtank.tank.CreativeTankBlock;
import com.kotori316.fluidtank.tank.TankBlock;
import com.kotori316.fluidtank.tank.Tiers;
import com.kotori316.fluidtank.tank.TileTank;
import com.kotori316.fluidtank.tank.TileTankCreative;
import com.kotori316.fluidtank.tank.TileTankVoid;
import com.kotori316.fluidtank.tank.VoidTankBlock;

public class ModTank implements ModInitializer {
    public static final String modID = "fluidtank";
    public static final Logger LOGGER = LogManager.getLogger(modID);
    public static final CreativeModeTab CREATIVE_TAB = FabricItemGroupBuilder.build(
        new ResourceLocation(modID, modID), () -> new ItemStack(Entries.WOOD_TANK)
    );

    @Override
    public void onInitialize() {
        ModTank.LOGGER.debug("Universal init is called. {} ", ModTank.modID);
        AutoConfig.register(TankConfig.class, GsonConfigSerializer::new);
        TankConstant.config = AutoConfig.getConfigHolder(TankConfig.class).getConfig();
        PacketHandler.Server.initServer();
        Registry.register(Registry.BLOCK, new ResourceLocation(modID, "tank_wood"), Entries.WOOD_TANK);
        Entries.TANK_BLOCKS.forEach(block -> Registry.register(Registry.BLOCK, new ResourceLocation(modID, "tank_" + block.tiers.toString().toLowerCase()), block));
        Registry.register(Registry.BLOCK, new ResourceLocation(modID, "creative"), Entries.CREATIVE_TANK);
        Registry.register(Registry.BLOCK, new ResourceLocation(modID, "tank_void"), Entries.VOID_TANK);

        Registry.register(Registry.ITEM, new ResourceLocation(modID, "tank_wood"), Entries.WOOD_TANK.blockItem());
        Entries.TANK_BLOCKS.forEach(block -> Registry.register(Registry.ITEM, new ResourceLocation(modID, "tank_" + block.tiers.toString().toLowerCase()), block.blockItem()));
        Registry.register(Registry.ITEM, new ResourceLocation(modID, "creative"), Entries.CREATIVE_TANK.blockItem());
        Registry.register(Registry.ITEM, new ResourceLocation(modID, "tank_void"), Entries.VOID_TANK.blockItem());

        Registry.register(Registry.FLUID, new ResourceLocation(modID, MilkFluid.NAME()), Entries.MILK_FLUID);

        Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(modID, "tank"), Entries.TANK_BLOCK_ENTITY_TYPE);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(modID, "tank_creative"), Entries.CREATIVE_BLOCK_ENTITY_TYPE);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(modID, "tank_void"), Entries.VOID_BLOCK_ENTITY_TYPE);

        Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(ModTank.modID, "content_tank"), Entries.CONTENT_LOOT_FUNCTION_TYPE);

        ResourceConditions.register(RecipeConfigCondition.Provider.ID, new RecipeConfigCondition());
        RecipeSerializer.register(TierRecipe.Serializer.LOCATION.toString(), TierRecipe.SERIALIZER);
        RecipeSerializer.register(TankRecipe.LOCATION.toString(), TankRecipe.SERIALIZER);
        if (FabricLoader.getInstance().isModLoaded("fabric-transfer-api-v1"))
            FabricFluidTankStorage.register();
    }

    public static class Entries {
        public static final TankBlock WOOD_TANK = new TankBlock(Tiers.WOOD);
        public static final List<TankBlock> TANK_BLOCKS = Collections.unmodifiableList(Tiers.stream().filter(Tiers::hasTagRecipe).map(TankBlock::new).collect(Collectors.toList()));
        public static final CreativeTankBlock CREATIVE_TANK = new CreativeTankBlock();
        public static final VoidTankBlock VOID_TANK = new VoidTankBlock();
        public static final MilkFluid MILK_FLUID = new MilkFluid();
        public static final BlockEntityType<TileTank> TANK_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(TileTank::new,
            ImmutableList.<TankBlock>builder().add(WOOD_TANK).addAll(TANK_BLOCKS).build().toArray(new TankBlock[0])).build(DSL.emptyPartType());
        public static final BlockEntityType<TileTankCreative> CREATIVE_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(TileTankCreative::new, CREATIVE_TANK).build(DSL.emptyPartType());
        public static final BlockEntityType<TileTankVoid> VOID_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(TileTankVoid::new, VOID_TANK).build(DSL.emptyPartType());
        public static final LootItemFunctionType CONTENT_LOOT_FUNCTION_TYPE = new LootItemFunctionType(new ContentTankSerializer());

        public static final List<TankBlock> ALL_TANK_BLOCKS = ImmutableList.<TankBlock>builder()
            .add(WOOD_TANK)
            .addAll(TANK_BLOCKS)
            .add(CREATIVE_TANK)
            .add(VOID_TANK)
            .build();
    }

}
