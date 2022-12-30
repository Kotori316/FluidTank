package com.kotori316.fluidtank.mixin;

import java.util.Map;

import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.render.ModelCustomWrapper;

@Mixin(ModelManager.class)
public final class MixinModelManager {

    @Inject(
        method = "loadModels(Lnet/minecraft/util/profiling/ProfilerFiller;Ljava/util/Map;Lnet/minecraft/client/resources/model/ModelBakery;)Lnet/minecraft/client/resources/model/ModelManager$ReloadState;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakeModels(Ljava/util/function/BiFunction;)V", shift = At.Shift.AFTER))
    public void injectReservoirModel(ProfilerFiller profilerFiller, Map<ResourceLocation, AtlasSet.StitchResult> map, ModelBakery modelBakery, CallbackInfoReturnable<?> cir) {
        var reservoirItems = CollectionConverters.asJava(ModObjects.itemReservoirs())
            .stream().map(BuiltInRegistries.ITEM::getKey)
            .map(n -> new ModelResourceLocation(n, "inventory"))
            .toList();
        for (ModelResourceLocation location : reservoirItems) {
            var bakedRegistry = modelBakery.getBakedTopLevelModels();
            var before = bakedRegistry.get(location);
            var wrapped = new ModelCustomWrapper(before);
            bakedRegistry.put(location, wrapped);
            FluidTank.LOGGER.debug("Replaced {} to {}", location, wrapped);
        }
    }
}
