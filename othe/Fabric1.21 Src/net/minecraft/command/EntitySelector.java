/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.predicate.NumberRange;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EntitySelector {
    public static final int MAX_VALUE = Integer.MAX_VALUE;
    public static final BiConsumer<Vec3d, List<? extends Entity>> ARBITRARY = (pos, entities) -> {};
    private static final TypeFilter<Entity, ?> PASSTHROUGH_FILTER = new TypeFilter<Entity, Entity>(){

        @Override
        public Entity downcast(Entity entity) {
            return entity;
        }

        @Override
        public Class<? extends Entity> getBaseClass() {
            return Entity.class;
        }
    };
    private final int limit;
    private final boolean includesNonPlayers;
    private final boolean localWorldOnly;
    private final List<Predicate<Entity>> predicates;
    private final NumberRange.DoubleRange distance;
    private final Function<Vec3d, Vec3d> positionOffset;
    @Nullable
    private final Box box;
    private final BiConsumer<Vec3d, List<? extends Entity>> sorter;
    private final boolean senderOnly;
    @Nullable
    private final String playerName;
    @Nullable
    private final UUID uuid;
    private final TypeFilter<Entity, ?> entityFilter;
    private final boolean usesAt;

    public EntitySelector(int count, boolean includesNonPlayers, boolean localWorldOnly, List<Predicate<Entity>> predicates, NumberRange.DoubleRange distance, Function<Vec3d, Vec3d> positionOffset, @Nullable Box box, BiConsumer<Vec3d, List<? extends Entity>> sorter, boolean senderOnly, @Nullable String playerName, @Nullable UUID uuid, @Nullable EntityType<?> type, boolean usesAt) {
        this.limit = count;
        this.includesNonPlayers = includesNonPlayers;
        this.localWorldOnly = localWorldOnly;
        this.predicates = predicates;
        this.distance = distance;
        this.positionOffset = positionOffset;
        this.box = box;
        this.sorter = sorter;
        this.senderOnly = senderOnly;
        this.playerName = playerName;
        this.uuid = uuid;
        this.entityFilter = type == null ? PASSTHROUGH_FILTER : type;
        this.usesAt = usesAt;
    }

    public int getLimit() {
        return this.limit;
    }

    public boolean includesNonPlayers() {
        return this.includesNonPlayers;
    }

    public boolean isSenderOnly() {
        return this.senderOnly;
    }

    public boolean isLocalWorldOnly() {
        return this.localWorldOnly;
    }

    public boolean usesAt() {
        return this.usesAt;
    }

    private void checkSourcePermission(ServerCommandSource source) throws CommandSyntaxException {
        if (this.usesAt && !source.hasPermissionLevel(2)) {
            throw EntityArgumentType.NOT_ALLOWED_EXCEPTION.create();
        }
    }

    public Entity getEntity(ServerCommandSource source) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        List<? extends Entity> list = this.getEntities(source);
        if (list.isEmpty()) {
            throw EntityArgumentType.ENTITY_NOT_FOUND_EXCEPTION.create();
        }
        if (list.size() > 1) {
            throw EntityArgumentType.TOO_MANY_ENTITIES_EXCEPTION.create();
        }
        return list.get(0);
    }

    public List<? extends Entity> getEntities(ServerCommandSource source) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        if (!this.includesNonPlayers) {
            return this.getPlayers(source);
        }
        if (this.playerName != null) {
            ServerPlayerEntity serverPlayerEntity = source.getServer().getPlayerManager().getPlayer(this.playerName);
            if (serverPlayerEntity == null) {
                return List.of();
            }
            return List.of(serverPlayerEntity);
        }
        if (this.uuid != null) {
            for (ServerWorld serverWorld : source.getServer().getWorlds()) {
                Entity entity = serverWorld.getEntity(this.uuid);
                if (entity == null) continue;
                if (!entity.getType().isEnabled(source.getEnabledFeatures())) break;
                return List.of(entity);
            }
            return List.of();
        }
        Vec3d vec3d = this.positionOffset.apply(source.getPosition());
        Box box = this.getOffsetBox(vec3d);
        if (this.senderOnly) {
            Predicate<Entity> predicate = this.getPositionPredicate(vec3d, box, null);
            if (source.getEntity() != null && predicate.test(source.getEntity())) {
                return List.of(source.getEntity());
            }
            return List.of();
        }
        Predicate<Entity> predicate = this.getPositionPredicate(vec3d, box, source.getEnabledFeatures());
        ObjectArrayList<Entity> list = new ObjectArrayList<Entity>();
        if (this.isLocalWorldOnly()) {
            this.appendEntitiesFromWorld(list, source.getWorld(), box, predicate);
        } else {
            for (ServerWorld serverWorld2 : source.getServer().getWorlds()) {
                this.appendEntitiesFromWorld(list, serverWorld2, box, predicate);
            }
        }
        return this.getEntities(vec3d, list);
    }

    private void appendEntitiesFromWorld(List<Entity> entities, ServerWorld world, @Nullable Box box, Predicate<Entity> predicate) {
        int i = this.getAppendLimit();
        if (entities.size() >= i) {
            return;
        }
        if (box != null) {
            world.collectEntitiesByType(this.entityFilter, box, predicate, entities, i);
        } else {
            world.collectEntitiesByType(this.entityFilter, predicate, entities, i);
        }
    }

    private int getAppendLimit() {
        return this.sorter == ARBITRARY ? this.limit : Integer.MAX_VALUE;
    }

    public ServerPlayerEntity getPlayer(ServerCommandSource source) throws CommandSyntaxException {
        this.checkSourcePermission(source);
        List<ServerPlayerEntity> list = this.getPlayers(source);
        if (list.size() != 1) {
            throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        }
        return list.get(0);
    }

    public List<ServerPlayerEntity> getPlayers(ServerCommandSource source) throws CommandSyntaxException {
        List<Object> list;
        this.checkSourcePermission(source);
        if (this.playerName != null) {
            ServerPlayerEntity serverPlayerEntity = source.getServer().getPlayerManager().getPlayer(this.playerName);
            if (serverPlayerEntity == null) {
                return List.of();
            }
            return List.of(serverPlayerEntity);
        }
        if (this.uuid != null) {
            ServerPlayerEntity serverPlayerEntity = source.getServer().getPlayerManager().getPlayer(this.uuid);
            if (serverPlayerEntity == null) {
                return List.of();
            }
            return List.of(serverPlayerEntity);
        }
        Vec3d vec3d = this.positionOffset.apply(source.getPosition());
        Box box = this.getOffsetBox(vec3d);
        Predicate<Entity> predicate = this.getPositionPredicate(vec3d, box, null);
        if (this.senderOnly) {
            ServerPlayerEntity serverPlayerEntity2;
            Entity entity = source.getEntity();
            if (entity instanceof ServerPlayerEntity && predicate.test(serverPlayerEntity2 = (ServerPlayerEntity)entity)) {
                return List.of(serverPlayerEntity2);
            }
            return List.of();
        }
        int i = this.getAppendLimit();
        if (this.isLocalWorldOnly()) {
            list = source.getWorld().getPlayers(predicate, i);
        } else {
            list = new ObjectArrayList();
            for (ServerPlayerEntity serverPlayerEntity3 : source.getServer().getPlayerManager().getPlayerList()) {
                if (!predicate.test(serverPlayerEntity3)) continue;
                list.add(serverPlayerEntity3);
                if (list.size() < i) continue;
                return list;
            }
        }
        return this.getEntities(vec3d, list);
    }

    @Nullable
    private Box getOffsetBox(Vec3d offset) {
        return this.box != null ? this.box.offset(offset) : null;
    }

    private Predicate<Entity> getPositionPredicate(Vec3d pos, @Nullable Box box, @Nullable FeatureSet enabledFeatures) {
        List<Predicate<Entity>> list;
        boolean bl3;
        boolean bl2;
        boolean bl = enabledFeatures != null;
        int i = (bl ? 1 : 0) + ((bl2 = box != null) ? 1 : 0) + ((bl3 = !this.distance.isDummy()) ? 1 : 0);
        if (i == 0) {
            list = this.predicates;
        } else {
            ObjectArrayList<Predicate<Entity>> list2 = new ObjectArrayList<Predicate<Entity>>(this.predicates.size() + i);
            list2.addAll(this.predicates);
            if (bl) {
                list2.add(entity -> entity.getType().isEnabled(enabledFeatures));
            }
            if (bl2) {
                list2.add(entity -> box.intersects(entity.getBoundingBox()));
            }
            if (bl3) {
                list2.add(entity -> this.distance.testSqrt(entity.squaredDistanceTo(pos)));
            }
            list = list2;
        }
        return Util.allOf(list);
    }

    private <T extends Entity> List<T> getEntities(Vec3d pos, List<T> entities) {
        if (entities.size() > 1) {
            this.sorter.accept(pos, entities);
        }
        return entities.subList(0, Math.min(this.limit, entities.size()));
    }

    public static Text getNames(List<? extends Entity> entities) {
        return Texts.join(entities, Entity::getDisplayName);
    }
}

