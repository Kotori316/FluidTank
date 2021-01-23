package com.kotori316.fluidtank;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.item.DyeColor;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.maven.artifact.versioning.ComparableVersion;
import scala.Option;
import scala.jdk.javaapi.OptionConverters;

public class Utils {
    public static int toInt(long l) {
        int i = (int) l;
        if (i == l) {
            return i;
        } else {
            return l > 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        }
    }

    public static <T> Optional<T> toJava(Option<T> option) {
        return OptionConverters.toJava(option);
    }

    public static boolean isInDev() {
        return !FMLLoader.isProduction();
    }

    @SuppressWarnings({"unused", "SpellCheckingInspection"})
    private static void dummy() {
        /*
        Cheat code to get filled tank.
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 16000L, fluid: "silents_mechanisms:diesel", capacity: 16000}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 8000L, fluid: "silents_mechanisms:ethane", capacity: 16000}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 8000L, fluid: "minecraft:water", capacity: 16000}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 10000L, fluid: "fluidtank:vanilla_milk", capacity: 16000}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: "Stone", id: "fluidtank:tiletank", tank: {amount: 10000L, fluid: "fluidtank:vanilla_milk", capacity: 16000}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier:"stone",tank:{amount:10000L,fluid:"fluidtank:vanilla_milk",capacity:16000}}}
        */
    }

    public static OptionalInt getItemColor(ItemStack stack) {
        DyeColor color = DyeColor.getColor(stack);
        if (color != null)
            return OptionalInt.of(color.getColorValue());
        if (stack.getItem() instanceof IDyeableArmorItem) {
            IDyeableArmorItem item = (IDyeableArmorItem) stack.getItem();
            return OptionalInt.of(item.getColor(stack));
        }
        return OptionalInt.empty();
    }

    private static final AtomicInteger VanillaMilkEnabled = new AtomicInteger(-1);
    private static final AtomicInteger LogCount = new AtomicInteger(0);

    public static boolean isVanillaMilkEnabled() {
        if (VanillaMilkEnabled.get() == -1) {
            ComparableVersion currentForge = new ComparableVersion(net.minecraftforge.versions.forge.ForgeVersion.getVersion());
            ComparableVersion milkImplemented = new ComparableVersion("36.0.1");
            int compared = currentForge.compareTo(milkImplemented);
            if (System.getenv().containsKey("CI_FORGE"))
                FluidTank.LOGGER.warn("Current {}, milk in forge is not available.", currentForge);
            VanillaMilkEnabled.set(compared >= 0 ? 1 : 0);
        }
        return VanillaMilkEnabled.get() == 1;
    }

    public static net.minecraft.util.ResourceLocation mapMilkName(net.minecraft.util.ResourceLocation maybeOldMilk) {
        if (isVanillaMilkEnabled() && maybeOldMilk.toString().equals(FluidTank.modID + ":vanilla_milk")) {
            if (LogCount.get() < 15) {
                LogCount.incrementAndGet();
                FluidTank.LOGGER.info("Converted {} to {}", maybeOldMilk, "minecraft:milk");
            }
            return new net.minecraft.util.ResourceLocation("minecraft", "milk");
        } else {
            return maybeOldMilk;
        }
    }

    public static void enableMilk() {
        if (isVanillaMilkEnabled())
            VanillaMilkAccessor.enableMilk();
    }

    private static class VanillaMilkAccessor {
        private static void enableMilk() {
            net.minecraftforge.common.ForgeMod.enableMilkFluid();
        }
    }
}
