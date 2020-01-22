package com.kotori316.fluidtank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.datafixers.DSL;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.function.LootFunctions;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.fluidtank.milk.MilkFluid;
import com.kotori316.fluidtank.render.RenderTank;
import com.kotori316.fluidtank.tank.ContentTankSerializer;
import com.kotori316.fluidtank.tank.CreativeTankBlock;
import com.kotori316.fluidtank.tank.TankBlock;
import com.kotori316.fluidtank.tank.TierRecipe;
import com.kotori316.fluidtank.tank.Tiers;
import com.kotori316.fluidtank.tank.TileTank;
import com.kotori316.fluidtank.tank.TileTankCreative;

public class ModTank implements ModInitializer {
    public static final String modID = "fluidtank";
    public static final Logger LOGGER = LogManager.getLogger(modID);
    public static final ItemGroup CREATIVE_TAB = FabricItemGroupBuilder.build(
        new Identifier(modID, modID), () -> new ItemStack(Entries.WOOD_TANK)
    );
    public static final Material MATERIAL = new Material(MaterialColor.AIR, false, true, true, false,
        true, false, false, PistonBehavior.BLOCK);
    public static final double d = 1 / 16d;
    public static final Box BOUNDING_BOX = new Box(2 * d, 0, 2 * d, 14 * d, 1d, 14 * d);
    public static final VoxelShape TANK_SHAPE = VoxelShapes.cuboid(BOUNDING_BOX);

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(modID, "tank_wood"), Entries.WOOD_TANK);
        Entries.TANK_BLOCKS.forEach(block -> Registry.register(Registry.BLOCK, new Identifier(modID, "tank_" + block.tiers.toString().toLowerCase()), block));
        Registry.register(Registry.BLOCK, new Identifier(modID, "creative"), Entries.CREATIVE_TANK);

        Registry.register(Registry.ITEM, new Identifier(modID, "tank_wood"), Entries.WOOD_TANK.blockItem());
        Entries.TANK_BLOCKS.forEach(block -> Registry.register(Registry.ITEM, new Identifier(modID, "tank_" + block.tiers.toString().toLowerCase()), block.blockItem()));
        Registry.register(Registry.ITEM, new Identifier(modID, "creative"), Entries.CREATIVE_TANK.blockItem());

        Registry.register(Registry.FLUID, new Identifier(modID, MilkFluid.NAME()), Entries.MILK_FLUID);

        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, "tiletank"), Entries.TANK_BLOCK_ENTITY_TYPE);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, "tiletankcreative"), Entries.CREATIVE_BLOCK_ENTITY_TYPE);

        RecipeSerializer.register(TierRecipe.Serializer.LOCATION.toString(), TierRecipe.SERIALIZER);
        LootFunctions.register(new ContentTankSerializer());
    }

    public static class Entries {
        public static final TankBlock WOOD_TANK = new TankBlock(Tiers.WOOD);
        public static final List<TankBlock> TANK_BLOCKS = Collections.unmodifiableList(Tiers.stream().filter(Tiers::hasTagRecipe).map(TankBlock::new).collect(Collectors.toList()));
        public static final CreativeTankBlock CREATIVE_TANK = new CreativeTankBlock();
        public static final MilkFluid MILK_FLUID = new MilkFluid();
        public static final BlockEntityType<TileTank> TANK_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.create(TileTank::new,
            ListBuilder.of(WOOD_TANK).add(TANK_BLOCKS).build().toArray(new TankBlock[0])).build(DSL.nilType());
        public static final BlockEntityType<TileTankCreative> CREATIVE_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.create(TileTankCreative::new, CREATIVE_TANK).build(DSL.nilType());

        public static final List<TankBlock> ALL_TANK_BLOCKS = ListBuilder.<TankBlock>of()
            .add(WOOD_TANK)
            .add(TANK_BLOCKS)
            .add(CREATIVE_TANK)
            .build();

        private static class ListBuilder<T> {
            private final List<T> list;

            public static <E> ListBuilder<E> of() {
                return new ListBuilder<>();
            }

            public static <E> ListBuilder<E> of(E e) {
                return new ListBuilder<E>().add(e);
            }

            private ListBuilder() {
                this.list = new ArrayList<T>();
            }

            public ListBuilder<T> add(T t) {
                list.add(t);
                return this;
            }

            public ListBuilder<T> add(List<T> ts) {
                list.addAll(ts);
                return this;
            }

            public List<T> build() {
                return Collections.unmodifiableList(new ArrayList<>(this.list));
            }
        }
    }

    public static class ClientInit implements ClientModInitializer {

        @SuppressWarnings("unchecked")
        @Override
        public void onInitializeClient() {
            System.out.println(modID + " is called client init.");
            Entries.ALL_TANK_BLOCKS.forEach(b -> BlockRenderLayerMap.INSTANCE.putBlock(b, RenderLayer.getCutoutMipped()));
            BlockEntityRendererRegistry.INSTANCE.register(Entries.TANK_BLOCK_ENTITY_TYPE, RenderTank::new);
            BlockEntityRendererRegistry.INSTANCE.register(Entries.CREATIVE_BLOCK_ENTITY_TYPE, d -> (BlockEntityRenderer<TileTankCreative>) ((BlockEntityRenderer<?>) new RenderTank(d)));
        }
    }
}
