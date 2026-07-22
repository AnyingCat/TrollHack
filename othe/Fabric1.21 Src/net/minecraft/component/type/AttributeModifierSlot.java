/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.component.type;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public enum AttributeModifierSlot implements StringIdentifiable
{
    ANY(0, "any", slot -> true),
    MAINHAND(1, "mainhand", EquipmentSlot.MAINHAND),
    OFFHAND(2, "offhand", EquipmentSlot.OFFHAND),
    HAND(3, "hand", slot -> slot.getType() == EquipmentSlot.Type.HAND),
    FEET(4, "feet", EquipmentSlot.FEET),
    LEGS(5, "legs", EquipmentSlot.LEGS),
    CHEST(6, "chest", EquipmentSlot.CHEST),
    HEAD(7, "head", EquipmentSlot.HEAD),
    ARMOR(8, "armor", EquipmentSlot::isArmorSlot),
    BODY(9, "body", EquipmentSlot.BODY);

    public static final IntFunction<AttributeModifierSlot> ID_TO_VALUE;
    public static final Codec<AttributeModifierSlot> CODEC;
    public static final PacketCodec<ByteBuf, AttributeModifierSlot> PACKET_CODEC;
    private final int id;
    private final String name;
    private final Predicate<EquipmentSlot> slotPredicate;

    private AttributeModifierSlot(int id, String name, Predicate<EquipmentSlot> slotPredicate) {
        this.id = id;
        this.name = name;
        this.slotPredicate = slotPredicate;
    }

    private AttributeModifierSlot(int id, String name, EquipmentSlot slot) {
        this(id, name, (EquipmentSlot slotx) -> slotx == slot);
    }

    public static AttributeModifierSlot forEquipmentSlot(EquipmentSlot slot) {
        return switch (slot) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.MAINHAND -> MAINHAND;
            case EquipmentSlot.OFFHAND -> OFFHAND;
            case EquipmentSlot.FEET -> FEET;
            case EquipmentSlot.LEGS -> LEGS;
            case EquipmentSlot.CHEST -> CHEST;
            case EquipmentSlot.HEAD -> HEAD;
            case EquipmentSlot.BODY -> BODY;
        };
    }

    @Override
    public String asString() {
        return this.name;
    }

    public boolean matches(EquipmentSlot slot) {
        return this.slotPredicate.test(slot);
    }

    static {
        ID_TO_VALUE = ValueLists.createIdToValueFunction(id -> id.id, AttributeModifierSlot.values(), ValueLists.OutOfBoundsHandling.ZERO);
        CODEC = StringIdentifiable.createCodec(AttributeModifierSlot::values);
        PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, id -> id.id);
    }
}

