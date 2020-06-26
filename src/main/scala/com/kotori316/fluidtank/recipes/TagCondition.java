package com.kotori316.fluidtank.recipes;

import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import com.kotori316.fluidtank.FluidTank;

public class TagCondition implements ICondition {
    public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "tag");
    private final ResourceLocation tag_name;

    public TagCondition(ResourceLocation tag_name) {
        this.tag_name = tag_name;
    }

    @Override
    public ResourceLocation getID() {
        return LOCATION;
    }

    @Override
    public boolean test() {
        ITag<Item> tag = TagCollectionManager.func_232928_e_().func_232925_b_().get(tag_name);
        return tag != null;
    }

    public static class Serializer implements IConditionSerializer<TagCondition> {
        public static final TagCondition.Serializer INSTANCE = new TagCondition.Serializer();

        @Override
        public void write(JsonObject json, TagCondition value) {
            json.addProperty("tag", value.tag_name.toString());
        }

        @Override
        public TagCondition read(JsonObject json) {
            return new TagCondition(new ResourceLocation(JSONUtils.getString(json, "tag")));
        }

        @Override
        public ResourceLocation getID() {
            return TagCondition.LOCATION;
        }
    }
}
