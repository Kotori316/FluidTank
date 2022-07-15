package com.kotori316.fluidtank.recipes;

import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import scala.jdk.javaapi.StreamConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockCAT;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.blocks.FluidSourceBlock;

final class StateAndModelProvider extends BlockStateProvider {
    StateAndModelProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, FluidTank.modID, exFileHelper);
    }

    private ResourceLocation blockTexture(String name) {
        return modLoc("blocks/" + name);
    }

    @Override
    protected void registerStatesAndModels() {
        catBlock();
        sourceBlock();
        tankBase();
        StreamConverters.asJavaSeqStream(ModObjects.blockTanks()).forEach(this::tank);
    }

    void catBlock() {
        this.directionalBlock(ModObjects.blockCat(), models().cubeTop(BlockCAT.NAME(),
            blockTexture("cat_side"), blockTexture("cat_front")));
    }

    void sourceBlock() {
        var builder = getVariantBuilder(ModObjects.blockSource());
        builder.setModels(builder.partialState().with(FluidSourceBlock.CHEAT_MODE(), false),
            new ConfiguredModel(models().cubeColumn(FluidSourceBlock.NAME(), blockTexture("fluid_source"), blockTexture("white"))));
        builder.setModels(builder.partialState().with(FluidSourceBlock.CHEAT_MODE(), true),
            new ConfiguredModel(models().cubeColumn(FluidSourceBlock.NAME() + "_inf", blockTexture("fluid_source_inf"), blockTexture("pink"))));
    }

    void tankBase() {
        models().withExistingParent("block/tanks", mcLoc("block"))
            .element()
            .from(2.0f, 0.0f, 2.0f)
            .to(14.0f, 16.0f, 14.0f)
            .allFaces((direction, faceBuilder) -> {
                if (direction.getAxis() == Direction.Axis.Y) {
                    faceBuilder.texture("#top").uvs(0.0f, 0.0f, 12.0f, 12.0f);
                } else {
                    faceBuilder.texture("#side").uvs(0.0f, 0.0f, 12.0f, 16.0f);
                }
            });
    }

    void tank(BlockTank blockTank) {
        var tier = blockTank.tier();
        getVariantBuilder(blockTank)
            .forAllStates(blockState -> new ConfiguredModel[]{
                new ConfiguredModel(models().withExistingParent("tank_" + tier.lowerName(), new ResourceLocation(FluidTank.modID, "block/tanks"))
                    .texture("particle", blockTexture(tier.lowerName() + "1"))
                    .texture("side", blockTexture(tier.lowerName() + "1"))
                    .texture("top", blockTexture(tier.lowerName() + "2"))
                    .renderType("cutout")
                )
            });
    }
}
