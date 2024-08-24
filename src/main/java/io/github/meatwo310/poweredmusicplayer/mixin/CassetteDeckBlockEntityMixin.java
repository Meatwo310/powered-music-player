package io.github.meatwo310.poweredmusicplayer.mixin;

import com.mojang.logging.LogUtils;
import dev.felnull.imp.blockentity.CassetteDeckBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CassetteDeckBlockEntity.class, remap = false)
public abstract class CassetteDeckBlockEntityMixin {
    @Shadow public abstract void setPlaying(boolean playing);
    @Shadow public abstract void setLoop(boolean loop);

    @Unique private static final Logger poweredmusicplayer$LOGGER = LogUtils.getLogger();
    @Unique private static final int poweredmusicplayer$POWERED_TIME_THRESHOLD = 3;

    @Unique private boolean poweredmusicplayer$wasPowered = false;
    /**
     * The time the block has been powered
     * Must be equal to or less than {@link #poweredmusicplayer$POWERED_TIME_THRESHOLD}
     * */
    @Unique private int poweredmusicplayer$poweredTime = 0;

    @Inject(method = "tick(" +
            "Lnet/minecraft/world/level/Level;" +
            "Lnet/minecraft/core/BlockPos;" +
            "Lnet/minecraft/world/level/block/state/BlockState;" +
            "Ldev/felnull/imp/blockentity/CassetteDeckBlockEntity;" +
            ")V",
            at = @At("HEAD")
    )
    private static void tick(Level level, BlockPos blockPos, BlockState blockState, CassetteDeckBlockEntity cassetteDeckBlockEntity, CallbackInfo info) {
        if (level.isClientSide()) return;

        CassetteDeckBlockEntityMixin be = (CassetteDeckBlockEntityMixin) (Object) cassetteDeckBlockEntity;
        if (be == null) {
            poweredmusicplayer$LOGGER.error("Failed to cast CassetteDeckBlockEntity to CassetteDeckBlockEntityMixin");
            return;
        }

//        if (be.poweredmusicplayer$poweredTime >= poweredmusicplayer$POWERED_TIME_THRESHOLD) {
//            poweredmusicplayer$LOGGER.debug("Powered {} ticks or more", poweredmusicplayer$POWERED_TIME_THRESHOLD);
//        } else if (be.poweredmusicplayer$poweredTime > 0) {
//            poweredmusicplayer$LOGGER.debug("Powered {} ticks", be.poweredmusicplayer$poweredTime);
//        }

        boolean powered = level.hasNeighborSignal(blockPos);
        if (powered && be.poweredmusicplayer$poweredTime < poweredmusicplayer$POWERED_TIME_THRESHOLD) {
            be.poweredmusicplayer$poweredTime++;
            if (be.poweredmusicplayer$poweredTime == poweredmusicplayer$POWERED_TIME_THRESHOLD) {
//                poweredmusicplayer$LOGGER.debug(
//                        "Powered {} ticks! Enabling loop",
//                        poweredmusicplayer$POWERED_TIME_THRESHOLD
//                );
                be.setLoop(true);
            }
        }

        if (powered == be.poweredmusicplayer$wasPowered) return;
        be.poweredmusicplayer$wasPowered = powered;

        if (powered) {
            cassetteDeckBlockEntity.setMusicPositionAndRestart(0L);
            cassetteDeckBlockEntity.setPlaying(true);
        } else {
            if (be.poweredmusicplayer$poweredTime >= poweredmusicplayer$POWERED_TIME_THRESHOLD) {
//                poweredmusicplayer$LOGGER.debug("Unpowered! Stopping playback");
                cassetteDeckBlockEntity.setPlaying(false);
            } else {
//                poweredmusicplayer$LOGGER.debug(
//                        "Powered less than {} tick! Just plays once",
//                        poweredmusicplayer$POWERED_TIME_THRESHOLD
//                );
                be.setLoop(false);
            }
            be.poweredmusicplayer$poweredTime = 0;
        }
    }
}
