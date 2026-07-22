/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.LightBlock;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.item.CompassAnglePredicateProvider;
import net.minecraft.client.item.ModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BundleItem;
import net.minecraft.item.CompassItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ModelPredicateProviderRegistry {
    private static final Map<Identifier, ModelPredicateProvider> GLOBAL = Maps.newHashMap();
    private static final Identifier DAMAGED_ID = Identifier.ofVanilla("damaged");
    private static final Identifier DAMAGE_ID = Identifier.ofVanilla("damage");
    private static final ClampedModelPredicateProvider DAMAGED_PROVIDER = (stack, world, entity, seed) -> stack.isDamaged() ? 1.0f : 0.0f;
    private static final ClampedModelPredicateProvider DAMAGE_PROVIDER = (stack, world, entity, seed) -> MathHelper.clamp((float)stack.getDamage() / (float)stack.getMaxDamage(), 0.0f, 1.0f);
    private static final Map<Item, Map<Identifier, ModelPredicateProvider>> ITEM_SPECIFIC = Maps.newHashMap();

    public static ClampedModelPredicateProvider register(Identifier id, ClampedModelPredicateProvider provider) {
        GLOBAL.put(id, provider);
        return provider;
    }

    public static void registerCustomModelData(ModelPredicateProvider provider) {
        GLOBAL.put(Identifier.ofVanilla("custom_model_data"), provider);
    }

    public static void register(Item item, Identifier id, ClampedModelPredicateProvider provider) {
        ITEM_SPECIFIC.computeIfAbsent(item, key -> Maps.newHashMap()).put(id, provider);
    }

    @Nullable
    public static ModelPredicateProvider get(ItemStack stack, Identifier id) {
        ModelPredicateProvider modelPredicateProvider;
        if (stack.getMaxDamage() > 0) {
            if (DAMAGE_ID.equals(id)) {
                return DAMAGE_PROVIDER;
            }
            if (DAMAGED_ID.equals(id)) {
                return DAMAGED_PROVIDER;
            }
        }
        if ((modelPredicateProvider = GLOBAL.get(id)) != null) {
            return modelPredicateProvider;
        }
        Map<Identifier, ModelPredicateProvider> map = ITEM_SPECIFIC.get(stack.getItem());
        if (map == null) {
            return null;
        }
        return map.get(id);
    }

    static {
        ModelPredicateProviderRegistry.register(Identifier.ofVanilla("lefthanded"), (stack, world, entity, seed) -> entity == null || entity.getMainArm() == Arm.RIGHT ? 0.0f : 1.0f);
        ModelPredicateProviderRegistry.register(Identifier.ofVanilla("cooldown"), (stack, world, entity, seed) -> entity instanceof PlayerEntity ? ((PlayerEntity)entity).getItemCooldownManager().getCooldownProgress(stack.getItem(), 0.0f) : 0.0f);
        ClampedModelPredicateProvider clampedModelPredicateProvider = (stack, world, entity, seed) -> {
            ArmorTrim armorTrim = stack.get(DataComponentTypes.TRIM);
            if (armorTrim != null) {
                return armorTrim.getMaterial().value().itemModelIndex();
            }
            return Float.NEGATIVE_INFINITY;
        };
        ModelPredicateProviderRegistry.register(ItemModelGenerator.TRIM_TYPE, clampedModelPredicateProvider);
        ModelPredicateProviderRegistry.registerCustomModelData((stack, world, entity, seed) -> stack.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT).value());
        ModelPredicateProviderRegistry.register(Items.BOW, Identifier.ofVanilla("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0f;
            }
            if (entity.getActiveItem() != stack) {
                return 0.0f;
            }
            return (float)(stack.getMaxUseTime(entity) - entity.getItemUseTimeLeft()) / 20.0f;
        });
        ModelPredicateProviderRegistry.register(Items.BRUSH, Identifier.ofVanilla("brushing"), (stack, world, entity, seed) -> {
            if (entity == null || entity.getActiveItem() != stack) {
                return 0.0f;
            }
            return (float)(entity.getItemUseTimeLeft() % 10) / 10.0f;
        });
        ModelPredicateProviderRegistry.register(Items.BOW, Identifier.ofVanilla("pulling"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0f : 0.0f);
        ModelPredicateProviderRegistry.register(Items.BUNDLE, Identifier.ofVanilla("filled"), (stack, world, entity, seed) -> BundleItem.getAmountFilled(stack));
        ModelPredicateProviderRegistry.register(Items.CLOCK, Identifier.ofVanilla("time"), new ClampedModelPredicateProvider(){
            private double time;
            private double step;
            private long lastTick;

            @Override
            public float unclampedCall(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int i) {
                Entity entity;
                Entity entity2 = entity = livingEntity != null ? livingEntity : itemStack.getHolder();
                if (entity == null) {
                    return 0.0f;
                }
                if (clientWorld == null && entity.getWorld() instanceof ClientWorld) {
                    clientWorld = (ClientWorld)entity.getWorld();
                }
                if (clientWorld == null) {
                    return 0.0f;
                }
                double d = clientWorld.getDimension().natural() ? (double)clientWorld.getSkyAngle(1.0f) : Math.random();
                d = this.getTime(clientWorld, d);
                return (float)d;
            }

            private double getTime(World world, double skyAngle) {
                if (world.getTime() != this.lastTick) {
                    this.lastTick = world.getTime();
                    double d = skyAngle - this.time;
                    d = MathHelper.floorMod(d + 0.5, 1.0) - 0.5;
                    this.step += d * 0.1;
                    this.step *= 0.9;
                    this.time = MathHelper.floorMod(this.time + this.step, 1.0);
                }
                return this.time;
            }
        });
        ModelPredicateProviderRegistry.register(Items.COMPASS, Identifier.ofVanilla("angle"), new CompassAnglePredicateProvider((world, stack, entity) -> {
            LodestoneTrackerComponent lodestoneTrackerComponent = stack.get(DataComponentTypes.LODESTONE_TRACKER);
            if (lodestoneTrackerComponent != null) {
                return lodestoneTrackerComponent.target().orElse(null);
            }
            return CompassItem.createSpawnPos(world);
        }));
        ModelPredicateProviderRegistry.register(Items.RECOVERY_COMPASS, Identifier.ofVanilla("angle"), new CompassAnglePredicateProvider((world, stack, entity) -> {
            if (entity instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity)entity;
                return playerEntity.getLastDeathPos().orElse(null);
            }
            return null;
        }));
        ModelPredicateProviderRegistry.register(Items.CROSSBOW, Identifier.ofVanilla("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0f;
            }
            if (CrossbowItem.isCharged(stack)) {
                return 0.0f;
            }
            return (float)(stack.getMaxUseTime(entity) - entity.getItemUseTimeLeft()) / (float)CrossbowItem.getPullTime(stack, entity);
        });
        ModelPredicateProviderRegistry.register(Items.CROSSBOW, Identifier.ofVanilla("pulling"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
        ModelPredicateProviderRegistry.register(Items.CROSSBOW, Identifier.ofVanilla("charged"), (stack, world, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0f : 0.0f);
        ModelPredicateProviderRegistry.register(Items.CROSSBOW, Identifier.ofVanilla("firework"), (stack, world, entity, seed) -> {
            ChargedProjectilesComponent chargedProjectilesComponent = stack.get(DataComponentTypes.CHARGED_PROJECTILES);
            return chargedProjectilesComponent != null && chargedProjectilesComponent.contains(Items.FIREWORK_ROCKET) ? 1.0f : 0.0f;
        });
        ModelPredicateProviderRegistry.register(Items.ELYTRA, Identifier.ofVanilla("broken"), (stack, world, entity, seed) -> ElytraItem.isUsable(stack) ? 0.0f : 1.0f);
        ModelPredicateProviderRegistry.register(Items.FISHING_ROD, Identifier.ofVanilla("cast"), (stack, world, entity, seed) -> {
            boolean bl2;
            if (entity == null) {
                return 0.0f;
            }
            boolean bl = entity.getMainHandStack() == stack;
            boolean bl3 = bl2 = entity.getOffHandStack() == stack;
            if (entity.getMainHandStack().getItem() instanceof FishingRodItem) {
                bl2 = false;
            }
            return (bl || bl2) && entity instanceof PlayerEntity && ((PlayerEntity)entity).fishHook != null ? 1.0f : 0.0f;
        });
        ModelPredicateProviderRegistry.register(Items.SHIELD, Identifier.ofVanilla("blocking"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0f : 0.0f);
        ModelPredicateProviderRegistry.register(Items.TRIDENT, Identifier.ofVanilla("throwing"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0f : 0.0f);
        ModelPredicateProviderRegistry.register(Items.LIGHT, Identifier.ofVanilla("level"), (stack, world, entity, seed) -> {
            BlockStateComponent blockStateComponent = stack.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT);
            Integer integer = blockStateComponent.getValue(LightBlock.LEVEL_15);
            if (integer != null) {
                return (float)integer.intValue() / 16.0f;
            }
            return 1.0f;
        });
        ModelPredicateProviderRegistry.register(Items.GOAT_HORN, Identifier.ofVanilla("tooting"), (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0f : 0.0f);
    }
}

