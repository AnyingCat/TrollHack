/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.Memory;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.custom.DebugGameEventCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugGameTestAddMarkerCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugGameTestClearCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugGoalSelectorCustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.NameGenerator;
import net.minecraft.util.Nameable;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.poi.PointOfInterest;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class DebugInfoSender {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void addGameTestMarker(ServerWorld world, BlockPos pos, String message, int color, int duration) {
        DebugInfoSender.sendToAll(world, new DebugGameTestAddMarkerCustomPayload(pos, color, message, duration));
    }

    public static void clearGameTestMarkers(ServerWorld world) {
        DebugInfoSender.sendToAll(world, new DebugGameTestClearCustomPayload());
    }

    public static void sendChunkWatchingChange(ServerWorld world, ChunkPos pos) {
    }

    public static void sendPoiAddition(ServerWorld world, BlockPos pos) {
        DebugInfoSender.sendPoi(world, pos);
    }

    public static void sendPoiRemoval(ServerWorld world, BlockPos pos) {
        DebugInfoSender.sendPoi(world, pos);
    }

    public static void sendPointOfInterest(ServerWorld world, BlockPos pos) {
        DebugInfoSender.sendPoi(world, pos);
    }

    private static void sendPoi(ServerWorld world, BlockPos pos) {
    }

    public static void sendPathfindingData(World world, MobEntity mob, @Nullable Path path, float nodeReachProximity) {
    }

    public static void sendNeighborUpdate(World world, BlockPos pos) {
    }

    public static void sendStructureStart(StructureWorldAccess world, StructureStart structureStart) {
    }

    public static void sendGoalSelector(World world, MobEntity mob, GoalSelector goalSelector) {
    }

    public static void sendRaids(ServerWorld server, Collection<Raid> raids) {
    }

    public static void sendBrainDebugData(LivingEntity living) {
    }

    public static void sendBeeDebugData(BeeEntity bee) {
    }

    public static void sendBreezeDebugData(BreezeEntity breeze) {
    }

    public static void sendGameEvent(World world, RegistryEntry<GameEvent> event, Vec3d pos) {
    }

    public static void sendGameEventListener(World world, GameEventListener eventListener) {
    }

    public static void sendBeehiveDebugData(World world, BlockPos pos, BlockState state, BeehiveBlockEntity blockEntity) {
    }

    private static List<String> listMemories(LivingEntity entity, long currentTime) {
        Map<MemoryModuleType<?>, Optional<Memory<?>>> map = entity.getBrain().getMemories();
        ArrayList<String> list = Lists.newArrayList();
        for (Map.Entry<MemoryModuleType<?>, Optional<Memory<?>>> entry : map.entrySet()) {
            Object string;
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            Optional<Memory<?>> optional = entry.getValue();
            if (optional.isPresent()) {
                Memory<?> memory = optional.get();
                Object object = memory.getValue();
                if (memoryModuleType == MemoryModuleType.HEARD_BELL_TIME) {
                    long l = currentTime - (Long)object;
                    string = l + " ticks ago";
                } else {
                    string = memory.isTimed() ? DebugInfoSender.format((ServerWorld)entity.getWorld(), object) + " (ttl: " + memory.getExpiry() + ")" : DebugInfoSender.format((ServerWorld)entity.getWorld(), object);
                }
            } else {
                string = "-";
            }
            list.add(Registries.MEMORY_MODULE_TYPE.getId(memoryModuleType).getPath() + ": " + (String)string);
        }
        list.sort(String::compareTo);
        return list;
    }

    private static String format(ServerWorld world, @Nullable Object object) {
        if (object == null) {
            return "-";
        }
        if (object instanceof UUID) {
            return DebugInfoSender.format(world, world.getEntity((UUID)object));
        }
        if (object instanceof LivingEntity) {
            Entity entity = (Entity)object;
            return NameGenerator.name(entity);
        }
        if (object instanceof Nameable) {
            return ((Nameable)object).getName().getString();
        }
        if (object instanceof WalkTarget) {
            return DebugInfoSender.format(world, ((WalkTarget)object).getLookTarget());
        }
        if (object instanceof EntityLookTarget) {
            return DebugInfoSender.format(world, ((EntityLookTarget)object).getEntity());
        }
        if (object instanceof GlobalPos) {
            return DebugInfoSender.format(world, ((GlobalPos)object).pos());
        }
        if (object instanceof BlockPosLookTarget) {
            return DebugInfoSender.format(world, ((BlockPosLookTarget)object).getBlockPos());
        }
        if (object instanceof DamageSource) {
            Entity entity = ((DamageSource)object).getAttacker();
            return entity == null ? object.toString() : DebugInfoSender.format(world, entity);
        }
        if (object instanceof Collection) {
            ArrayList<String> list = Lists.newArrayList();
            for (Object object2 : (Iterable)object) {
                list.add(DebugInfoSender.format(world, object2));
            }
            return ((Object)list).toString();
        }
        return object.toString();
    }

    private static void sendToAll(ServerWorld world, CustomPayload payload) {
        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(payload);
        for (ServerPlayerEntity serverPlayerEntity : world.getPlayers()) {
            serverPlayerEntity.networkHandler.sendPacket(packet);
        }
    }

    private static /* synthetic */ void method_55630(ServerWorld serverWorld, Vec3d vec3d, RegistryKey registryKey) {
        DebugInfoSender.sendToAll(serverWorld, new DebugGameEventCustomPayload(registryKey, vec3d));
    }

    private static /* synthetic */ void method_52277(List list, UUID uUID, Object2IntMap object2IntMap) {
        String string = NameGenerator.name(uUID);
        object2IntMap.forEach((villageGossipType, integer) -> list.add(string + ": " + String.valueOf(villageGossipType) + ": " + integer));
    }

    private static /* synthetic */ String method_52275(String string) {
        return StringHelper.truncate(string, 255, true);
    }

    private static /* synthetic */ void method_36162(List list, PrioritizedGoal goal) {
        list.add(new DebugGoalSelectorCustomPayload.Goal(goal.getPriority(), goal.isRunning(), goal.getGoal().getClass().getSimpleName()));
    }

    private static /* synthetic */ String method_44135(RegistryKey registryKey) {
        return registryKey.getValue().toString();
    }

    private static /* synthetic */ void method_36155(ServerWorld world, PointOfInterest poi) {
        DebugInfoSender.sendPoiAddition(world, poi.getPos());
    }

    private static /* synthetic */ boolean method_36159(RegistryEntry registryEntry) {
        return true;
    }
}

