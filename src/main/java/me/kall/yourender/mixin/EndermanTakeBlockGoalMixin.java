package me.kall.yourender.mixin;

import me.kall.yourender.YourEnder;
import me.kall.yourender.data.EndableBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public abstract class EndermanTakeBlockGoalMixin {
    @Shadow @Final private EnderMan enderman;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void pick(CallbackInfo ci) {
        if (this.enderman.level instanceof ServerLevel level) {
            Optional<Long> pos = EndableBlocks.get(level).pick(level, this.enderman.chunkPosition().toLong());
            if (pos.isPresent()) {
                BlockPos endable = BlockPos.of(pos.get());
                if (this.enderman.distanceToSqr(endable.getX(), endable.getY(), endable.getZ()) > YourEnder.DIST) return;
                BlockState state = level.getBlockState(endable);
                level.removeBlock(endable, false);
                level.gameEvent(this.enderman, GameEvent.BLOCK_DESTROY, endable);
                this.enderman.setCarriedBlock(state.getBlock().defaultBlockState());
                ci.cancel();
            }
        }
    }
}
