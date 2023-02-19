package com.kotori316.fluidtank.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.NotImplementedException;

import com.kotori316.fluidtank.FluidTank;
import com.kotori316.fluidtank.Utils;

public interface ClientSync {
    void fromClientTag(CompoundTag tag);

    CompoundTag toClientTag(CompoundTag tag);

    default void sync() {
        if (this instanceof BlockEntity entity) {
            var level = entity.getLevel();
            if (level != null && !level.isClientSide) {
                var clientSyncMessage = new ClientSyncMessage(entity.getBlockPos(), level.dimension(), toClientTag(new CompoundTag()));
                PacketHandler.sendToClientWorld(clientSyncMessage, level);
            }
        } else {
            var message = """
                ClientSync should be implemented for BlockEntity, but currently not.
                Please override ClientSync#sync for this instance(%s).
                """.formatted(getClass());
            Utils.runOnce("%s-ClientSync".formatted(getClass().getName()), () -> FluidTank.LOGGER.fatal(message));
            throw new NotImplementedException(message);
        }
    }
}
