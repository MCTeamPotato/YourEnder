package me.kall.yourender.mixin;

import me.kall.yourender.ext.Endable;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public abstract class BlockMixin implements Endable {
    @Unique
    private boolean yourEnder$endable;

    @Override
    public boolean yourEnder$get() {
        return this.yourEnder$endable;
    }

    @Override
    public void yourEnder$set(boolean endable) {
        this.yourEnder$endable = endable;
    }
}
