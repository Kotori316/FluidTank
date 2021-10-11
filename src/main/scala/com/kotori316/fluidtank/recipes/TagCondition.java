package com.kotori316.fluidtank.recipes;

import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;

import com.kotori316.fluidtank.FluidTank;

public class TagCondition implements ICondition {
    public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "tag");
    private final ResourceLocation tag_name;
    private final TagEmptyCondition condition;

    public TagCondition(ResourceLocation tag_name) {
        this.tag_name = tag_name;
        condition = new TagEmptyCondition(tag_name);
    }

    public TagCondition(String tagName) {
        this(new ResourceLocation(tagName));
    }

    @Override
    public ResourceLocation getID() {
        return LOCATION;
    }

    @Override
    public boolean test() {
        return !condition.test();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagCondition that = (TagCondition) o;
        return tag_name.equals(that.tag_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag_name);
    }

    @Override
    public String toString() {
        return "TagCondition{" +
            "tag_name=" + tag_name +
            '}';
    }

    public static class Serializer implements IConditionSerializer<TagCondition> {
        public static final TagCondition.Serializer INSTANCE = new TagCondition.Serializer();

        @Override
        public void write(JsonObject json, TagCondition value) {
            json.addProperty("tag", value.tag_name.toString());
        }

        @Override
        public TagCondition read(JsonObject json) {
            return new TagCondition(GsonHelper.getAsString(json, "tag"));
        }

        @Override
        public ResourceLocation getID() {
            return TagCondition.LOCATION;
        }
    }
}
