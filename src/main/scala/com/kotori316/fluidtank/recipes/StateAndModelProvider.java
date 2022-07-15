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
import com.kotori316.fluidtank.transport.PipeBlock;

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
        pipeBase(ModObjects.blockFluidPipe());
        pipeBase(ModObjects.blockItemPipe());
        pipe(ModObjects.blockFluidPipe());
        pipe(ModObjects.blockItemPipe());
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

    @SuppressWarnings("SpellCheckingInspection")
    void pipeBase(PipeBlock pipeBlock) {
        String prefix = pipeBlock.registryName.getPath().replace("pipe", "");
        // Center Model
        models().getBuilder("block/" + pipeBlock.registryName.getPath() + "_center")
            .texture("particle", blockTexture(prefix + "frame"))
            .texture("texture", blockTexture(prefix + "frame"))
            .renderType("cutout_mipped")
            .element().from(4.0f, 4.0f, 4.0f).to(12.0f, 12.0f, 12.0f)
            .allFaces((direction, faceBuilder) -> faceBuilder.uvs(4.0f, 4.0f, 12.0f, 12.0f).texture("#texture"));
        // Side Model
        models().getBuilder("block/" + pipeBlock.registryName.getPath() + "_side")
            .texture("particle", blockTexture(prefix + "frame"))
            .texture("texture", blockTexture(prefix + "frame"))
            .renderType("cutout_mipped")
            .element().from(4.0f, 4.0f, 0.0f).to(12.0f, 12.0f, 4.0f)
            .face(Direction.SOUTH).uvs(4.0f, 4.0f, 12.0f, 12.0f).texture("#texture").cullface(Direction.SOUTH).end()
            .face(Direction.DOWN).uvs(4.0f, 6.0f, 12.0f, 10.0f).texture("#texture").end()
            .face(Direction.UP).uvs(4.0f, 6.0f, 12.0f, 10.0f).texture("#texture").end()
            .face(Direction.WEST).uvs(6.0f, 4.0f, 10.0f, 12.0f).texture("#texture").end()
            .face(Direction.EAST).uvs(6.0f, 4.0f, 10.0f, 12.0f).texture("#texture").end();

        // In-Out Model
        models().getBuilder("block/" + "pipe_in_out")
            .renderType("cutout_mipped")
            // Inside
            .element().from(4, 4, 2).to(12, 12, 4)
            .face(Direction.SOUTH).uvs(4.0f, 4.0f, 12.0f, 12.0f).texture("#texture").cullface(Direction.SOUTH).end()
            .face(Direction.DOWN).uvs(4.0f, 6.0f, 12.0f, 10.0f).texture("#texture").end()
            .face(Direction.UP).uvs(4.0f, 6.0f, 12.0f, 10.0f).texture("#texture").end()
            .face(Direction.WEST).uvs(6.0f, 4.0f, 10.0f, 12.0f).texture("#texture").end()
            .face(Direction.EAST).uvs(6.0f, 4.0f, 10.0f, 12.0f).texture("#texture").end()
            .end()
            // Outside
            .element().from(2, 2, 0).to(14, 14, 2)
            .face(Direction.SOUTH).uvs(4.0f, 4.0f, 12.0f, 12.0f).texture("#side").end()
            .face(Direction.DOWN).uvs(2, 14, 14, 16).texture("#side").end()
            .face(Direction.UP).uvs(2, 0, 14, 2).texture("#side").end()
            .face(Direction.WEST).uvs(0, 2, 2, 14).texture("#side").end()
            .face(Direction.EAST).uvs(14, 2, 16, 14).texture("#side").end()
        ;
    }

    void pipe(PipeBlock pipeBlock) {
        String prefix = pipeBlock.registryName.getPath().replace("pipe", "");
        var centerModel = new ResourceLocation(FluidTank.modID, "block/" + pipeBlock.registryName.getPath() + "_center");
        var sideModel = new ResourceLocation(FluidTank.modID, "block/" + pipeBlock.registryName.getPath() + "_side");
        var outModel = models().withExistingParent("block/" + pipeBlock.registryName.getPath() + "_output", new ResourceLocation(FluidTank.modID, "block/pipe_in_out"))
            .texture("particle", blockTexture(prefix + "frame"))
            .texture("texture", blockTexture(prefix + "frame"))
            .texture("side", blockTexture(prefix + "frame_output"));
        var inModel = models().withExistingParent("block/" + pipeBlock.registryName.getPath() + "_input", new ResourceLocation(FluidTank.modID, "block/pipe_in_out"))
            .texture("particle", blockTexture(prefix + "frame"))
            .texture("texture", blockTexture(prefix + "frame"))
            .texture("side", blockTexture(prefix + "frame_input"));
        getMultipartBuilder(pipeBlock).part()
            .modelFile(models().getExistingFile(centerModel)).addModel().end().part()
            // Connected
            .modelFile(models().getExistingFile(sideModel)).uvLock(true).addModel().condition(PipeBlock.NORTH, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(models().getExistingFile(sideModel)).uvLock(true).rotationY(90).addModel().condition(PipeBlock.EAST, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(models().getExistingFile(sideModel)).uvLock(true).rotationY(180).addModel().condition(PipeBlock.SOUTH, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(models().getExistingFile(sideModel)).uvLock(true).rotationY(270).addModel().condition(PipeBlock.WEST, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(models().getExistingFile(sideModel)).uvLock(true).rotationX(270).addModel().condition(PipeBlock.UP, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(models().getExistingFile(sideModel)).uvLock(true).rotationX(90).addModel().condition(PipeBlock.DOWN, PipeBlock.Connection.CONNECTED).end().part()
            // OUTPUT
            .modelFile(outModel).uvLock(true).addModel().condition(PipeBlock.NORTH, PipeBlock.Connection.OUTPUT).end().part()
            .modelFile(outModel).uvLock(true).rotationY(90).addModel().condition(PipeBlock.EAST, PipeBlock.Connection.OUTPUT).end().part()
            .modelFile(outModel).uvLock(true).rotationY(180).addModel().condition(PipeBlock.SOUTH, PipeBlock.Connection.OUTPUT).end().part()
            .modelFile(outModel).uvLock(true).rotationY(270).addModel().condition(PipeBlock.WEST, PipeBlock.Connection.OUTPUT).end().part()
            .modelFile(outModel).uvLock(true).rotationX(270).addModel().condition(PipeBlock.UP, PipeBlock.Connection.OUTPUT).end().part()
            .modelFile(outModel).uvLock(true).rotationX(90).addModel().condition(PipeBlock.DOWN, PipeBlock.Connection.OUTPUT).end().part()
            // INPUT
            .modelFile(inModel).uvLock(true).addModel().condition(PipeBlock.NORTH, PipeBlock.Connection.INPUT).end().part()
            .modelFile(inModel).uvLock(true).rotationY(90).addModel().condition(PipeBlock.EAST, PipeBlock.Connection.INPUT).end().part()
            .modelFile(inModel).uvLock(true).rotationY(180).addModel().condition(PipeBlock.SOUTH, PipeBlock.Connection.INPUT).end().part()
            .modelFile(inModel).uvLock(true).rotationY(270).addModel().condition(PipeBlock.WEST, PipeBlock.Connection.INPUT).end().part()
            .modelFile(inModel).uvLock(true).rotationX(270).addModel().condition(PipeBlock.UP, PipeBlock.Connection.INPUT).end().part()
            .modelFile(inModel).uvLock(true).rotationX(90).addModel().condition(PipeBlock.DOWN, PipeBlock.Connection.INPUT).end()
        ;
    }
}
