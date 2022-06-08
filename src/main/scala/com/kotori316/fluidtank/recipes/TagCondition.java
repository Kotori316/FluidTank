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
    private final ResourceLocation tagName;
    private final TagEmptyCondition condition;

    public TagCondition(ResourceLocation tagName) {
        this.tagName = tagName;
        condition = new TagEmptyCondition(tagName);
    }

    public TagCondition(String tagName) {
        this(new ResourceLocation(tagName));
    }

    @Override
    public ResourceLocation getID() {
        return LOCATION;
    }

    @Override
    public boolean test(IContext context) {
        return !this.condition.test(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagCondition that = (TagCondition) o;
        return tagName.equals(that.tagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName);
    }

    @Override
    public String toString() {
        return "TagCondition{" +
            "tag_name=" + tagName +
            '}';
    }

    public static class Serializer implements IConditionSerializer<TagCondition> {
        public static final TagCondition.Serializer INSTANCE = new TagCondition.Serializer();

        @Override
        public void write(JsonObject json, TagCondition value) {
            json.addProperty("tag", value.tagName.toString());
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
