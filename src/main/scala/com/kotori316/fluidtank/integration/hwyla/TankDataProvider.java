package com.kotori316.fluidtank.integration.hwyla;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import scala.jdk.javaapi.OptionConverters;

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
import static com.kotori316.fluidtank.integration.Localize.WAILA_SHORT;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.KEY_SHORT_INFO;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.KEY_TANK_INFO;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_ConnectionAmount;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_ConnectionCapacity;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_ConnectionComparator;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_ConnectionFluidName;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_NonCreative;
import static com.kotori316.fluidtank.integration.hwyla.TankWailaPlugin.NBT_Tier;

public class TankDataProvider implements IServerDataProvider<TileEntity>, IComponentProvider {

    private static final Predicate<Object> NOT_EMPTY = Predicate.isEqual(FLUID_NULL).negate();

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        TileEntity t = accessor.getTileEntity();
        if (t instanceof TileTank && config.get(KEY_TANK_INFO)) {
            TileTank tank = ((TileTank) t);
            CompoundNBT nbtData = accessor.getServerData();
            List<? extends ITextComponent> list;
            if (config.get(KEY_SHORT_INFO)) {
                if (t instanceof TileTankVoid) {
                    list = Collections.emptyList();
                } else {
                    if (nbtData.getBoolean(NBT_NonCreative)) {
                        list = Collections.singletonList(
                            new TranslationTextComponent(WAILA_SHORT,
                                nbtData.getString(NBT_ConnectionFluidName),
                                nbtData.getLong(NBT_ConnectionAmount),
                                nbtData.getLong(NBT_ConnectionCapacity))
                        );
                    } else {
                        String fluidName = getCreativeFluidName(tank);
                        list = java.util.Optional.of(fluidName)
                            .filter(NOT_EMPTY)
                            .map(StringTextComponent::new)
                            .map(Collections::singletonList)
                            .orElse(Collections.emptyList());
                    }
                }
            } else {
                Tier tier = tank.tier();
                if (t instanceof TileTankVoid) {
                    list = Collections.singletonList(new TranslationTextComponent(TIER, tier.toString()));
                } else {
                    if (nbtData.getBoolean(NBT_NonCreative)) {
                        list = Arrays.asList(
                            new TranslationTextComponent(TIER, tier.toString()),
                            new TranslationTextComponent(CONTENT, nbtData.getString(NBT_ConnectionFluidName)),
                            new TranslationTextComponent(AMOUNT, nbtData.getLong(NBT_ConnectionAmount)),
                            new TranslationTextComponent(CAPACITY, nbtData.getLong(NBT_ConnectionCapacity)),
                            new TranslationTextComponent(COMPARATOR, nbtData.getInt(NBT_ConnectionComparator))
                        );
                    } else {
                        String fluidName = getCreativeFluidName(tank);
                        list = Arrays.asList(
                            new TranslationTextComponent(TIER, tier.toString()),
                            new TranslationTextComponent(CONTENT, fluidName)
                        );
                    }
                }
            }

            tooltip.addAll(list);
        }
    }

    @Nonnull
    private static String getCreativeFluidName(TileTank tank) {
        return java.util.Optional.ofNullable(tank.internalTank().getTank().fluidAmount()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL);
    }

    @Override
    public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
        if (te instanceof TileTank) {
            TileTank tank = (TileTank) te;

            tag.putString(NBT_Tier, tank.tier().toString());
            if (te instanceof TileTankVoid) return;
            tag.putString(NBT_ConnectionFluidName,
                OptionConverters.toJava(tank.connection().getFluidStack()).filter(FluidAmount::nonEmpty).map(FluidAmount::getLocalizedName).orElse(FLUID_NULL));
            if (!tank.connection().hasCreative()) {
                tag.putBoolean(NBT_NonCreative, true);
                tag.putLong(NBT_ConnectionAmount, tank.connection().amount());
                tag.putLong(NBT_ConnectionCapacity, tank.connection().capacity());
                tag.putInt(NBT_ConnectionComparator, tank.getComparatorLevel());
            }
        }
    }
}
