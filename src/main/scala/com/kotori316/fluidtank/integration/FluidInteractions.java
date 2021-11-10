package com.kotori316.fluidtank.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.kotori316.fluidtank.tank.Connection;

public final class FluidInteractions {
    private FluidInteractions() {
    }

    private static final List<FluidInteraction> FLUID_INTERACTIONS;

    static {
        List<FluidInteraction> list = new ArrayList<>();
        list.add(new BCAttributeInteraction());
        if (FabricLoader.getInstance().isModLoaded("TechReborn".toLowerCase())) {
            list.add(techRebornInteraction());
        }

        FLUID_INTERACTIONS = Collections.unmodifiableList(list);
    }

    private static FluidInteraction techRebornInteraction() {
        return new TechRebornCellInteraction();
    }

    public static InteractionResult interact(Connection connection, Player player, InteractionHand hand, ItemStack stack) {
        for (FluidInteraction interaction : FLUID_INTERACTIONS) {
            if (interaction.isFluidContainer(stack)) {
                FluidVolumeUtil.FluidTankInteraction interact = interaction.interact(connection, player, hand, stack);
                if (interact.didMoveAny())
                    return interact.asActionResult();
            }
        }
        return InteractionResult.PASS;
    }
}
