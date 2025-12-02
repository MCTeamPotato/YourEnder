package me.kall.yourender;

import com.google.common.collect.Lists;
import me.kall.duplicationless.config.JsonConfig;
import me.kall.duplicationless.event.BlockChangeEvent;
import me.kall.yourender.data.EndableBlocks;
import me.kall.yourender.ext.Endable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

@Mod(YourEnder.MOD_ID)
public final class YourEnder {
    public static final String MOD_ID = "yourender";

    private static final JsonConfig CONFIG = JsonConfig.create(MOD_ID, "1")
            .put("EnderManPickable", Lists.newArrayList(
                    "create",
                    "mekanism", "mekanismgenerators", "mekanismadditions",
                    "gtceu",
                    "immersiveengineering",
                    "enderio", "endercore", "enderio_endergy",
                    "industrialforegoing",
                    "thermal_foundation", "thermal_expansion", "thermal_cultivation", "thermal_innovation", "thermal_dynamics", "thermal_locomotion",
                    "refinedstorage"
            ))
            .put("DistanceForPicking", 6)
            .initialize();

    public static final Set<String> PICKABLE = CONFIG.getSet("EnderManPickable", String.class);
    public static final double DIST = CONFIG.getDouble("DistanceForPicking")  * CONFIG.getDouble("DistanceForPicking");

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = MOD_ID)
    public static final class ForgeEvents {
        @SubscribeEvent
        public static void chunkLoad(ChunkEvent.@NotNull Load event) {
            LevelAccessor level = event.getLevel();
            if (level instanceof ServerLevel) {
                EndableBlocks.get((ServerLevel) level).rebuildChunk((ServerLevel) level, event.getChunk().getPos());
            }
        }

        @SubscribeEvent
        public static void blockChange(BlockChangeEvent event) {
            ServerLevel level = event.level();
            long chunk = event.chunkPos();
            long block = event.blockPos();

            boolean was = ((Endable)event.oldState().getBlock()).yourEnder$get();
            boolean is = ((Endable)event.newState().getBlock()).yourEnder$get();

            MinecraftServer server = level.getServer();

            if (was) server.execute(() -> EndableBlocks.get(level).remove(level, chunk, block));
            if (is) server.execute(() -> EndableBlocks.get(level).add(level, chunk, block));
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MOD_ID)
    public static final class ModEvents {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
                    ((Endable)entry.getValue()).yourEnder$set(PICKABLE.contains(entry.getKey().location().getNamespace()));
                }
            });
        }
    }
}
