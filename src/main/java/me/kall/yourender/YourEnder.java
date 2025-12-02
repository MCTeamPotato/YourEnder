package me.kall.yourender;

import com.google.common.collect.Lists;
import me.kall.duplicationless.config.JsonConfig;
import me.kall.duplicationless.event.BlockChangeEvent;
import me.kall.duplicationless.event.ReloadCommandEvent;
import me.kall.yourender.data.EndableBlocks;
import me.kall.yourender.ext.Endable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.Event;
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

    public static Set<String> PICKABLE;
    public static Set<String> BLOCKS;
    public static double DIST;
    public static boolean DESPAWN;
    public static boolean EVERY;

    private static void loadConfig() {
        JsonConfig config = JsonConfig.create(MOD_ID, "1")
                .put("EverythingIsPickableByEnderMan", false)
                .put("EnderManPickable", Lists.newArrayList("create", "ae2", "mekanism", "mekanismgenerators", "mekanismadditions", "draconicevolution"))
                .put("EnderManPickableSpecificBlocks", Lists.newArrayList("mekanismgenerators:fission_reactor_casing", "mekanismgenerators:fission_reactor_port"))
                .put("DistanceForPicking", 6)
                .put("EnderManDespawnWithOurPickableBlocks", false)
                .initialize();
        PICKABLE = config.getSet("EnderManPickable", String.class);
        BLOCKS = config.getSet("EnderManPickableSpecificBlocks", String.class);
        DIST = config.getDouble("DistanceForPicking")  * config.getDouble("DistanceForPicking");
        DESPAWN = config.getBoolean("EnderManDespawnWithOurPickableBlocks");
        EVERY = config.getBoolean("EverythingIsPickableByEnderMan");
    }

    static {
        loadConfig();
    }

    private static void setup() {
        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            ((Endable)entry.getValue()).yourEnder$set(EVERY || PICKABLE.contains(id.getNamespace()) || BLOCKS.contains(id.toString()));
        }
    }

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

        @SubscribeEvent
        public static void enderDespawn(MobSpawnEvent.AllowDespawn event) {
            if (!DESPAWN) {
                Mob entity = event.getEntity();
                if (entity instanceof EnderMan) {
                    BlockState blockState = ((EnderMan) entity).getCarriedBlock();
                    if (blockState == null) return;
                    if (((Endable)blockState.getBlock()).yourEnder$get()) event.setResult(Event.Result.DENY);
                }
            }
        }

        @SubscribeEvent
        public static void configReload(ReloadCommandEvent event) {
            event.server.execute(() -> {
                loadConfig();
                setup();
            });
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MOD_ID)
    public static final class ModEvents {
        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(YourEnder::setup);
        }
    }
}
