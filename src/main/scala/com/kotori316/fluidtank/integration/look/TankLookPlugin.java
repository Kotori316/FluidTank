package com.kotori316.fluidtank.integration.look;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import me.geek.tom.lat.modapi.IProvidesLATInfo;
import net.minecraft.nbt.EndNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import scala.jdk.javaapi.OptionConverters;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.fluids.FluidAmount;
import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;
import com.kotori316.fluidtank.tiles.TileTankVoid;

import static com.kotori316.fluidtank.integration.Localize.AMOUNT;
import static com.kotori316.fluidtank.integration.Localize.CAPACITY;
import static com.kotori316.fluidtank.integration.Localize.COMPARATOR;
import static com.kotori316.fluidtank.integration.Localize.CONTENT;
import static com.kotori316.fluidtank.integration.Localize.FLUID_NULL;
import static com.kotori316.fluidtank.integration.Localize.TIER;

@Mod.EventBusSubscriber(modid = FluidTank.modID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TankLookPlugin {
    public static final ResourceLocation LOCATION = new ResourceLocation(FluidTank.modID, "attach_look");

    @SuppressWarnings("SpellCheckingInspection")
    private static final String MOD_TARGET = "lookatthat";

    @SubscribeEvent
    public static void onInit(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded(MOD_TARGET)) {
            MinecraftForge.EVENT_BUS.register(new CapHandler());
        }
    }

    static class CapHandler {
        @SubscribeEvent
        public void event(AttachCapabilitiesEvent<TileEntity> event) {
            if (event.getObject() instanceof TileTank) {
                TileTank tank = (TileTank) event.getObject();
                LookAtThatCapabilityProvider provider = new LookAtThatCapabilityProvider(tank);
                event.addCapability(LOCATION, provider);
            }
        }
    }
}

class LookAtThatCapabilityProvider implements ICapabilityProvider, IProvidesLATInfo {
    @CapabilityInject(IProvidesLATInfo.class)
    public static Capability<IProvidesLATInfo> CAPABILITY = null;
    private final TileTank tank;

    LookAtThatCapabilityProvider(TileTank tank) {
        this.tank = tank;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return CAPABILITY.orEmpty(cap, LazyOptional.of(() -> this).cast());
    }

    @Override
    public String getInfo() {
        return getShortMessage();
    }

    @Nonnull
    private String getShortMessage() {
        List<? extends ITextComponent> list;
        if (tank instanceof TileTankVoid) {
            list = Collections.emptyList();
        } else {
            if (!tank.connection().hasCreative()) {
                list = Collections.singletonList(
                    new StringTextComponent(
                        OptionConverters.toJava(tank.connection().getFluidStack()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL)
                            + ", " + tank.connection().amount() + "mB")
                );
            } else {
                String fluidName = getCreativeFluidName(tank);
                list = java.util.Optional.of(fluidName)
                    .filter(Predicate.isEqual(FLUID_NULL).negate())
                    .map(StringTextComponent::new)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
            }
        }
        return list.stream().map(ITextComponent::getString).collect(Collectors.joining());
    }

    @SuppressWarnings("unused")
    @Nonnull
    private String getLongMessage() {
        List<? extends ITextComponent> list;
        Tier tier = tank.tier();
        if (tank instanceof TileTankVoid) {
            list = Collections.singletonList(new TranslationTextComponent(TIER, tier.toString()));
        } else {
            if (!tank.connection().hasCreative()) {
                list = Arrays.asList(
                    new TranslationTextComponent(TIER, tier.toString()),
                    new TranslationTextComponent(CONTENT,
                        OptionConverters.toJava(tank.connection().getFluidStack()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL)),
                    new TranslationTextComponent(AMOUNT, tank.connection().amount()),
                    new TranslationTextComponent(CAPACITY, tank.connection().capacity()),
                    new TranslationTextComponent(COMPARATOR, tank.connection().getComparatorLevel())
                );
            } else {
                String fluidName = getCreativeFluidName(tank);
                list = Arrays.asList(
                    new TranslationTextComponent(TIER, tier.toString()),
                    new TranslationTextComponent(CONTENT, fluidName)
                );
            }
        }
        return list.stream().map(ITextComponent::getString).collect(Collectors.joining(System.lineSeparator()));
    }

    @Nonnull
    private static String getCreativeFluidName(TileTank tank) {
        return java.util.Optional.ofNullable(tank.internalTank().getTank().fluidAmount()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL);
    }

    @Override
    public void read(INBT inbt) {
    }

    @Override
    public INBT write() {
        return EndNBT.INSTANCE;
    }

    @Override
    public void setMessage(String s) {
    }
}
