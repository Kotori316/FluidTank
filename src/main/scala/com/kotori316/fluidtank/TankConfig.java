package com.kotori316.fluidtank;

import java.util.Locale;
import java.util.OptionalLong;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import com.kotori316.fluidtank.tiles.Tier;

@Config(name = FluidTank.modID)
public final class TankConfig implements ConfigData {

    @ConfigEntry.Gui.CollapsibleObject
    public Capacity capacity = new Capacity();
    public boolean enableUpdateRecipe = true;
    public boolean enableAE2Integration = true;
    public boolean enableFluidSupplier = false;
    public int pipeColor = 0xFFFFFF;
    public boolean easyRecipe = false;
    public boolean debug = false;

    public static class Capacity {
        public long wood = Tier.WOOD.getDefaultAmount();
        public long stone = Tier.STONE.getDefaultAmount();
        public long iron = Tier.IRON.getDefaultAmount();
        public long gold = Tier.GOLD.getDefaultAmount();
        public long diamond = Tier.DIAMOND.getDefaultAmount();
        public long emerald = Tier.EMERALD.getDefaultAmount();
        public long star = Tier.STAR.getDefaultAmount();
        public long copper = Tier.COPPER.getDefaultAmount();
        public long tin = Tier.TIN.getDefaultAmount();
        public long bronze = Tier.BRONZE.getDefaultAmount();
        public long lead = Tier.LEAD.getDefaultAmount();
        public long silver = Tier.SILVER.getDefaultAmount();

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
