package com.kotori316.fluidtank.integration;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import com.kotori316.fluidtank.tiles.Tier;
import com.kotori316.fluidtank.tiles.TileTank;
import com.kotori316.fluidtank.tiles.TileTankVoid;

public class Localize {

    public static final String TIER = "fluidtank.waila.tier";
    public static final String CONTENT = "fluidtank.waila.content";
    public static final String AMOUNT = "fluidtank.waila.amount";
    public static final String CAPACITY = "fluidtank.waila.capacity";
    public static final String COMPARATOR = "fluidtank.waila.comparator";
    public static final String WAILA_TANK_INFO = "fluidtank.tank_info";
    public static final String WAILA_SHORT_INFO = "fluidtank.short_info";
    public static final String WAILA_COMPACT_NUMBER = "fluidtank.compact_number";
    public static final String WAILA_SHORT = "fluidtank.waila.short";
    public static final String WAILA_SUPPLIER = "fluidtank.waila.supplier";
    public static final String FLUID_NULL = "None";
    public static final String TOOLTIP = WAILA_SHORT;
    public static final String NBT_Tier = TileTank.NBT_Tier();
    public static final String NBT_ConnectionAmount = "ConnectionAmount";
    public static final String NBT_ConnectionCapacity = "ConnectionCapacity";
    public static final String NBT_ConnectionComparator = "Comparator";
    public static final String NBT_ConnectionFluidName = "FluidName";
    public static final String NBT_Creative = "Creative";

    public static List<? extends Component> getTooltipText(CompoundTag tankData, TileTank tank, boolean shortInfo, boolean compactAmount) {
        var languageSplit = Minecraft.getInstance().getLanguageManager().getSelected().getCode().split("_", 2);
        Locale locale;
        if (languageSplit.length == 2) {
            locale = new Locale(languageSplit[0], languageSplit[1].toUpperCase(Locale.ROOT));
        } else {
            locale = Locale.US;
        }
        return getTooltipText(tankData, tank, shortInfo, compactAmount, locale);
    }

    public static List<? extends Component> getTooltipText(CompoundTag tankData, TileTank tank, boolean shortInfo, boolean compactAmount, Locale locale) {
        Function<Number, String> numberFormat;
        if (compactAmount) {
            var formatter = NumberFormat.getCompactNumberInstance(locale, NumberFormat.Style.SHORT);
            formatter.setMinimumFractionDigits(1);
            formatter.setRoundingMode(RoundingMode.DOWN);
            numberFormat = formatter::format;
        } else {
            numberFormat = String::valueOf;
        }
        UnaryOperator<String> fluidNameFormatter;
        if (compactAmount) fluidNameFormatter = s -> {
            var array = s.split(":", 2);
            if (array.length == 2) return array[1];
            else return array[0];
        };
        else fluidNameFormatter = UnaryOperator.identity();
        if (shortInfo) {
            if (tank instanceof TileTankVoid) {
                return Collections.emptyList();
            } else {
                if (!tankData.contains(NBT_Creative)) {
                    return Collections.singletonList(
                        Component.translatable(WAILA_SHORT,
                            fluidNameFormatter.apply(tank.internalTank().getTank().fluidAmount().getLocalizedName()),
                            numberFormat.apply(tank.internalTank().getTank().amount()),
                            numberFormat.apply(tank.internalTank().getTank().capacity()))
                    );
                } else if (!tankData.getBoolean(NBT_Creative)) {
                    return Collections.singletonList(
                        Component.translatable(WAILA_SHORT,
                            fluidNameFormatter.apply(tankData.getString(NBT_ConnectionFluidName)),
                            numberFormat.apply(tankData.getLong(NBT_ConnectionAmount)),
                            numberFormat.apply(tankData.getLong(NBT_ConnectionCapacity)))
                    );
                } else {
                    return java.util.Optional.of(tankData.getString(NBT_ConnectionFluidName))
                        .map(fluidNameFormatter)
                        .filter(s -> !FLUID_NULL.equals(s))
                        .map(Component::literal)
                        .map(Collections::singletonList)
                        .orElse(Collections.emptyList());
                }
            }
        } else {
            Tier tier = tank.tier();
            if (tank instanceof TileTankVoid) {
                return Collections.singletonList(Component.translatable(TIER, tier.toString()));
            } else {
                if (!tankData.getBoolean(NBT_Creative)) {
                    return Arrays.asList(
                        Component.translatable(TIER, tier.toString()),
                        Component.translatable(CONTENT, fluidNameFormatter.apply(tankData.getString(NBT_ConnectionFluidName))),
                        Component.translatable(AMOUNT, numberFormat.apply(tankData.getLong(NBT_ConnectionAmount))),
                        Component.translatable(CAPACITY, numberFormat.apply(tankData.getLong(NBT_ConnectionCapacity))),
                        Component.translatable(COMPARATOR, tankData.getInt(NBT_ConnectionComparator))
                    );
                } else {
                    return Arrays.asList(
                        Component.translatable(TIER, tier.toString()),
                        Component.translatable(CONTENT, fluidNameFormatter.apply(tankData.getString(NBT_ConnectionFluidName)))
                    );
                }
            }
        }
    }

}
