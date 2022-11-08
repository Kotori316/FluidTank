package com.kotori316.fluidtank.recipes;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import scala.jdk.javaapi.StreamConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.blocks.BlockCAT;
import com.kotori316.fluidtank.blocks.BlockTank;
import com.kotori316.fluidtank.blocks.FluidSourceBlock;
import com.kotori316.fluidtank.integration.mekanism_gas.BlockGasTank;
import com.kotori316.fluidtank.items.ReservoirItem;
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
        StreamConverters.asJavaSeqStream(ModObjects.gasTanks()).forEach(this::gasTank);
        pipeBase();
        pipe(ModObjects.blockFluidPipe(), "fluid_pipe");
        pipe(ModObjects.blockItemPipe(), "item_pipe");
        StreamConverters.asJavaSeqStream(ModObjects.itemReservoirs()).forEach(this::reservoir);
    }

    void catBlock() {
        this.directionalBlock(ModObjects.blockCat(), models().cubeTop(BlockCAT.NAME(),
            blockTexture("cat_side"), blockTexture("cat_front")));
        this.itemModels().withExistingParent("item/" + BlockCAT.NAME(), new ResourceLocation(FluidTank.modID, "block/" + BlockCAT.NAME()));
    }

    void sourceBlock() {
        var builder = getVariantBuilder(ModObjects.blockSource());
        builder.setModels(builder.partialState().with(FluidSourceBlock.CHEAT_MODE(), false),
            new ConfiguredModel(models().cubeColumn(FluidSourceBlock.NAME(), blockTexture("fluid_source"), blockTexture("white"))));
        builder.setModels(builder.partialState().with(FluidSourceBlock.CHEAT_MODE(), true),
            new ConfiguredModel(models().cubeColumn(FluidSourceBlock.NAME() + "_inf", blockTexture("fluid_source_inf"), blockTexture("pink"))));
        ResourceLocation cheat = new ResourceLocation(FluidTank.modID, "source_cheat");
        itemModels().getBuilder(ModObjects.blockSource().registryName().getPath())
            .override()
            .predicate(cheat, 0)
            .model(models().getExistingFile(new ResourceLocation(FluidTank.modID, "block/" + FluidSourceBlock.NAME())))
            .end()
            .override()
            .predicate(cheat, 1)
            .model(models().getExistingFile(new ResourceLocation(FluidTank.modID, "block/" + FluidSourceBlock.NAME() + "_inf")))
            .end();
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
        itemModels().getBuilder("item/item_tank")
            .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
            .guiLight(BlockModel.GuiLight.SIDE)
            .transforms()
            .transform(ItemTransforms.TransformType.GUI).scale(0.625f).translation(0, 0, 0).rotation(30, 225, 0).end()
            .transform(ItemTransforms.TransformType.GROUND).scale(0.25f).translation(0, 3, 0).rotation(0, 0, 0).end()
            .transform(ItemTransforms.TransformType.FIXED).scale(0.5f).translation(0, 0, 0).rotation(0, 0, 0).end()
            .transform(ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND).scale(0.375f).translation(0, 2.5f, 0).rotation(75, 45, 0).end()
            .transform(ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND).scale(0.4f).translation(0, 0, 0).rotation(0, 45, 0).end()
            .transform(ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND).scale(0.4f).translation(0, 0, 0).rotation(0, 225, 0).end()
            .end()
            .ao(false)
            .texture("particle", "#1")
            .texture("side", "#1")
            .texture("top", "#2")
            .element()
            .from(2.0f, 0.0f, 2.0f).to(14.0f, 16.0f, 14.0f)
            .allFaces((direction, faceBuilder) -> {
                if (direction.getAxis() == Direction.Axis.Y) {
                    faceBuilder.texture("#top").uvs(0.0f, 0.0f, 12.0f, 12.0f);
                } else {
                    faceBuilder.texture("#side").uvs(0.0f, 0.0f, 12.0f, 16.0f);
                }
            });
        itemModels().withExistingParent("item/gas_item_tank", mcLoc("block/block"))
            .ao(false)
            .texture("particle", "#1")
            .texture("side", "#1")
            .texture("top", "#2")
            .element()
            .from(2.0f, 0.0f, 2.0f).to(14.0f, 16.0f, 14.0f)
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
        itemModels().withExistingParent(blockTank.registryName().getPath(), new ResourceLocation(FluidTank.modID, "item/item_tank"))
            .texture("1", blockTexture(tier.lowerName() + "1"))
            .texture("2", blockTexture(tier.lowerName() + "2"));
    }

    void gasTank(BlockGasTank blockGasTank) {
        var tier = blockGasTank.tier();
        getVariantBuilder(blockGasTank)
            .forAllStates(blockState -> new ConfiguredModel[]{
                new ConfiguredModel(models().withExistingParent("gas_tank_" + tier.lowerName(), new ResourceLocation(FluidTank.modID, "block/tanks"))
                    .texture("particle", blockTexture("gas_%s1".formatted(tier.lowerName())))
                    .texture("side", blockTexture("gas_%s1".formatted(tier.lowerName())))
                    .texture("top", blockTexture("gas_%s2".formatted(tier.lowerName())))
                    .renderType("cutout")
                )
            });
        itemModels().withExistingParent(blockGasTank.registryName().getPath(), new ResourceLocation(FluidTank.modID, "item/gas_item_tank"))
            .texture("1", blockTexture("gas_%s1".formatted(tier.lowerName())))
            .texture("2", blockTexture("gas_%s2".formatted(tier.lowerName())));
    }

    @SuppressWarnings("SpellCheckingInspection")
    void pipeBase() {
        // Center Model
        models().getBuilder("block/" + "pipe_center")
            .renderType("cutout_mipped")
            .element().from(4.0f, 4.0f, 4.0f).to(12.0f, 12.0f, 12.0f)
            .allFaces((direction, faceBuilder) -> faceBuilder.uvs(4.0f, 4.0f, 12.0f, 12.0f).texture("#texture"));
        // Side Model
        models().getBuilder("block/" + "pipe_side")
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

        // Item
        itemModels().withExistingParent("item/" + "pipe_base", "block/block")
            .transforms()
            .transform(ItemTransforms.TransformType.GUI).rotation(30, 225, 0).scale(0.8f).end()
            .transform(ItemTransforms.TransformType.FIXED).scale(0.8f).end()
            .end()
            .ao(false)
            .element()
            .from(4, 4, 4).to(12, 12, 12)
            .allFaces((direction, faceBuilder) ->
                faceBuilder.uvs(4, 4, 12, 12).texture("#texture")
            );
    }

    void pipe(PipeBlock pipeBlock, String modelBaseName) {
        String prefix = pipeBlock.registryName.getPath().replace("pipe", "");
        ResourceLocation frameTexture = blockTexture(prefix + "frame");
        var centerModel = models().withExistingParent("block/" + modelBaseName + "_center", new ResourceLocation(FluidTank.modID, "block/pipe_center"))
            .texture("particle", frameTexture)
            .texture("texture", frameTexture);
        var sideModel = models().withExistingParent("block/" + modelBaseName + "_side", new ResourceLocation(FluidTank.modID, "block/pipe_side"))
            .texture("particle", frameTexture)
            .texture("texture", frameTexture);
        var outModel = models().withExistingParent("block/" + modelBaseName + "_output", new ResourceLocation(FluidTank.modID, "block/pipe_in_out"))
            .texture("particle", frameTexture)
            .texture("texture", frameTexture)
            .texture("side", blockTexture(prefix + "frame_output"));
        var inModel = models().withExistingParent("block/" + modelBaseName + "_input", new ResourceLocation(FluidTank.modID, "block/pipe_in_out"))
            .texture("particle", frameTexture)
            .texture("texture", frameTexture)
            .texture("side", blockTexture(prefix + "frame_input"));
        getMultipartBuilder(pipeBlock).part()
            .modelFile(centerModel).addModel().end().part()
            // Connected
            .modelFile(sideModel).uvLock(true).addModel().condition(PipeBlock.NORTH, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(sideModel).uvLock(true).rotationY(90).addModel().condition(PipeBlock.EAST, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(sideModel).uvLock(true).rotationY(180).addModel().condition(PipeBlock.SOUTH, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(sideModel).uvLock(true).rotationY(270).addModel().condition(PipeBlock.WEST, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(sideModel).uvLock(true).rotationX(270).addModel().condition(PipeBlock.UP, PipeBlock.Connection.CONNECTED).end().part()
            .modelFile(sideModel).uvLock(true).rotationX(90).addModel().condition(PipeBlock.DOWN, PipeBlock.Connection.CONNECTED).end().part()
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

        itemModels().withExistingParent("item/" + pipeBlock.registryName.getPath(), new ResourceLocation(FluidTank.modID, "item/pipe_base"))
            .texture("texture", frameTexture);
    }

    void reservoir(ReservoirItem reservoirItem) {
        ResourceLocation item = reservoirItem.registryName();
        itemModels().getBuilder(item.toString())
            .parent(new ModelFile.UncheckedModelFile("item/generated"))
            .texture("layer0", new ResourceLocation(item.getNamespace(), "items/" + item.getPath()));
    }
}
