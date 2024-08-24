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
    @Unique private boolean poweredmusicplayer$wasPowered = false;

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

        boolean powered = level.hasNeighborSignal(blockPos);
        if (powered == be.poweredmusicplayer$wasPowered) return;

        if (powered) cassetteDeckBlockEntity.setMusicPositionAndRestart(0L);
        cassetteDeckBlockEntity.setPlaying(powered);
        be.setLoop(powered);
        be.poweredmusicplayer$wasPowered = powered;
    }
}
