package com.kotori316.fluidtank;

import java.util.Locale;
import java.util.OptionalLong;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import com.kotori316.fluidtank.tank.Tiers;

@Config(name = ModTank.modID)
public final class TankConfig implements ConfigData {

    @ConfigEntry.Gui.CollapsibleObject
    public Capacity capacity = new Capacity();
    public boolean enableUpdateRecipe = true;

    public static class Capacity {
        public long wood = Tiers.WOOD.buckets;
        public long stone = Tiers.STONE.buckets;
        public long iron = Tiers.IRON.buckets;
        public long gold = Tiers.GOLD.buckets;
        public long diamond = Tiers.DIAMOND.buckets;
        public long emerald = Tiers.EMERALD.buckets;
        public long star = Tiers.STAR.buckets;
        public long copper = Tiers.COPPER.buckets;
        public long tin = Tiers.TIN.buckets;
        public long bronze = Tiers.BRONZE.buckets;
        public long lead = Tiers.LEAD.buckets;
        public long silver = Tiers.SILVER.buckets;

        public OptionalLong get(String name) {
            return switch (name.toLowerCase(Locale.ROOT)) {
                case "wood" -> OptionalLong.of(wood);
                case "stone" -> OptionalLong.of(stone);
                case "iron" -> OptionalLong.of(iron);
                case "gold" -> OptionalLong.of(gold);
                case "diamond" -> OptionalLong.of(diamond);
                case "emerald" -> OptionalLong.of(emerald);
                case "star" -> OptionalLong.of(star);
                case "copper" -> OptionalLong.of(copper);
                case "tin" -> OptionalLong.of(tin);
                case "bronze" -> OptionalLong.of(bronze);
                case "lead" -> OptionalLong.of(lead);
                case "silver" -> OptionalLong.of(silver);
                default -> OptionalLong.empty();
            };
        }
    }
}
