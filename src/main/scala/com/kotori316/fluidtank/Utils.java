package com.kotori316.fluidtank;

import java.util.Optional;

import scala.Option;

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
        if (option != null && option.isDefined()) {
            return Optional.ofNullable(option.get());
        } else {
            return Optional.empty();
        }
    }

    @SuppressWarnings({"unused", "SpellCheckingInspection"})
    private static void dummy() {
        /*
        Cheat code to get filled tank.
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 16000L, fluid: "silents_mechanisms:diesel", capacity: 16000}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 8000L, fluid: "silents_mechanisms:ethane", capacity: 16000}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 8000L, fluid: "minecraft:water", capacity: 16000}}}
        /give @p fluidtank:tank_stone{BlockEntityTag:{tier: {string: "Stone"}, id: "fluidtank:tiletank", tank: {amount: 10000L, fluid: "fluidtank:vanilla_milk", capacity: 16000}}}
        */
    }

}
