package com.kotori316.fluidtank.mixin;

import java.util.Map;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import scala.jdk.javaapi.CollectionConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.ModObjects;
import com.kotori316.fluidtank.render.ModelCustomWrapper;

@Mixin(ModelManager.class)
public final class MixinModelManager {
    @Shadow
    private Map<ResourceLocation, BakedModel> bakedRegistry;

    @Inject(
        method = "apply(Lnet/minecraft/client/resources/model/ModelBakery;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
        at = @At("RETURN"))
    public void injectReservoirModel(ModelBakery modelBakery, ResourceManager resourceManager, ProfilerFiller profilerFiller, CallbackInfo ci) {
        var reservoirItems = CollectionConverters.asJava(ModObjects.itemReservoirs())
            .stream().map(Registry.ITEM::getKey)
            .map(n -> new ModelResourceLocation(n, "inventory"))
            .toList();
        for (ModelResourceLocation location : reservoirItems) {
            var before = bakedRegistry.get(location);
            var wrapped = new ModelCustomWrapper(before);
            bakedRegistry.put(location, wrapped);
            FluidTank.LOGGER.debug("Replaced {} to {}", location, wrapped);
        }
    }
}
