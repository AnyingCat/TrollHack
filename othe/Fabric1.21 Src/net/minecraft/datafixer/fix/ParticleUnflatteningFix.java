/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ParticleUnflatteningFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ParticleUnflatteningFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.PARTICLE);
        Type<?> type2 = this.getOutputSchema().getType(TypeReferences.PARTICLE);
        return this.writeFixAndRead("ParticleUnflatteningFix", type, type2, this::fixParticle);
    }

    private <T> Dynamic<T> fixParticle(Dynamic<T> dynamic) {
        Optional<String> optional = dynamic.asString().result();
        if (optional.isEmpty()) {
            return dynamic;
        }
        String string = optional.get();
        String[] strings = string.split(" ", 2);
        String string2 = IdentifierNormalizingSchema.normalize(strings[0]);
        Dynamic dynamic2 = dynamic.createMap(Map.of(dynamic.createString("type"), dynamic.createString(string2)));
        return switch (string2) {
            case "minecraft:item" -> {
                if (strings.length > 1) {
                    yield this.fixItemParticle(dynamic2, strings[1]);
                }
                yield dynamic2;
            }
            case "minecraft:block", "minecraft:block_marker", "minecraft:falling_dust", "minecraft:dust_pillar" -> {
                if (strings.length > 1) {
                    yield this.fixBlockParticle(dynamic2, strings[1]);
                }
                yield dynamic2;
            }
            case "minecraft:dust" -> {
                if (strings.length > 1) {
                    yield this.fixDustParticle(dynamic2, strings[1]);
                }
                yield dynamic2;
            }
            case "minecraft:dust_color_transition" -> {
                if (strings.length > 1) {
                    yield this.fixDustColorTransitionParticle(dynamic2, strings[1]);
                }
                yield dynamic2;
            }
            case "minecraft:sculk_charge" -> {
                if (strings.length > 1) {
                    yield this.fixSculkChargeParticle(dynamic2, strings[1]);
                }
                yield dynamic2;
            }
            case "minecraft:vibration" -> {
                if (strings.length > 1) {
                    yield this.fixVibrationParticle(dynamic2, strings[1]);
                }
                yield dynamic2;
            }
            case "minecraft:shriek" -> {
                if (strings.length > 1) {
                    yield this.fixShriekParticle(dynamic2, strings[1]);
                }
                yield dynamic2;
            }
            default -> dynamic2;
        };
    }

    private <T> Dynamic<T> fixItemParticle(Dynamic<T> dynamic, String params) {
        int i = params.indexOf("{");
        Dynamic dynamic2 = dynamic.createMap(Map.of(dynamic.createString("Count"), dynamic.createInt(1)));
        if (i == -1) {
            dynamic2 = dynamic2.set("id", dynamic.createString(params));
        } else {
            dynamic2 = dynamic2.set("id", dynamic.createString(params.substring(0, i)));
            NbtCompound nbtCompound = ParticleUnflatteningFix.tryParse(params.substring(i));
            if (nbtCompound != null) {
                dynamic2 = dynamic2.set("tag", new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbtCompound).convert(dynamic.getOps()));
            }
        }
        return dynamic.set("item", dynamic2);
    }

    @Nullable
    private static NbtCompound tryParse(String snbt) {
        try {
            return StringNbtReader.parse(snbt);
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse tag: {}", (Object)snbt, (Object)exception);
            return null;
        }
    }

    private <T> Dynamic<T> fixBlockParticle(Dynamic<T> dynamic, String params) {
        int i = params.indexOf("[");
        Dynamic dynamic2 = dynamic.emptyMap();
        if (i == -1) {
            dynamic2 = dynamic2.set("Name", dynamic.createString(IdentifierNormalizingSchema.normalize(params)));
        } else {
            dynamic2 = dynamic2.set("Name", dynamic.createString(IdentifierNormalizingSchema.normalize(params.substring(0, i))));
            Map<Dynamic<T>, Dynamic<T>> map = ParticleUnflatteningFix.parseBlockProperties(dynamic, params.substring(i));
            if (!map.isEmpty()) {
                dynamic2 = dynamic2.set("Properties", dynamic.createMap(map));
            }
        }
        return dynamic.set("block_state", dynamic2);
    }

    private static <T> Map<Dynamic<T>, Dynamic<T>> parseBlockProperties(Dynamic<T> dynamic, String propertiesStr) {
        try {
            HashMap<Dynamic<T>, Dynamic<T>> map = new HashMap<Dynamic<T>, Dynamic<T>>();
            StringReader stringReader = new StringReader(propertiesStr);
            stringReader.expect('[');
            stringReader.skipWhitespace();
            while (stringReader.canRead() && stringReader.peek() != ']') {
                stringReader.skipWhitespace();
                String string = stringReader.readString();
                stringReader.skipWhitespace();
                stringReader.expect('=');
                stringReader.skipWhitespace();
                String string2 = stringReader.readString();
                stringReader.skipWhitespace();
                map.put(dynamic.createString(string), dynamic.createString(string2));
                if (!stringReader.canRead()) continue;
                if (stringReader.peek() != ',') break;
                stringReader.skip();
            }
            stringReader.expect(']');
            return map;
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse block properties: {}", (Object)propertiesStr, (Object)exception);
            return Map.of();
        }
    }

    private static <T> Dynamic<T> parseColor(Dynamic<T> dynamic, StringReader paramsReader) throws CommandSyntaxException {
        float f = paramsReader.readFloat();
        paramsReader.expect(' ');
        float g = paramsReader.readFloat();
        paramsReader.expect(' ');
        float h = paramsReader.readFloat();
        return dynamic.createList(Stream.of(Float.valueOf(f), Float.valueOf(g), Float.valueOf(h)).map(dynamic::createFloat));
    }

    private <T> Dynamic<T> fixDustParticle(Dynamic<T> dynamic, String params) {
        try {
            StringReader stringReader = new StringReader(params);
            Dynamic<T> dynamic2 = ParticleUnflatteningFix.parseColor(dynamic, stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            return dynamic.set("color", dynamic2).set("scale", dynamic.createFloat(f));
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)params, (Object)exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> fixDustColorTransitionParticle(Dynamic<T> dynamic, String params) {
        try {
            StringReader stringReader = new StringReader(params);
            Dynamic<T> dynamic2 = ParticleUnflatteningFix.parseColor(dynamic, stringReader);
            stringReader.expect(' ');
            float f = stringReader.readFloat();
            stringReader.expect(' ');
            Dynamic<T> dynamic3 = ParticleUnflatteningFix.parseColor(dynamic, stringReader);
            return dynamic.set("from_color", dynamic2).set("to_color", dynamic3).set("scale", dynamic.createFloat(f));
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)params, (Object)exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> fixSculkChargeParticle(Dynamic<T> dynamic, String params) {
        try {
            StringReader stringReader = new StringReader(params);
            float f = stringReader.readFloat();
            return dynamic.set("roll", dynamic.createFloat(f));
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)params, (Object)exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> fixVibrationParticle(Dynamic<T> dynamic, String params) {
        try {
            StringReader stringReader = new StringReader(params);
            float f = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float g = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float h = (float)stringReader.readDouble();
            stringReader.expect(' ');
            int i = stringReader.readInt();
            Dynamic<?> dynamic2 = dynamic.createIntList(IntStream.of(MathHelper.floor(f), MathHelper.floor(g), MathHelper.floor(h)));
            Dynamic dynamic3 = dynamic.createMap(Map.of(dynamic.createString("type"), dynamic.createString("minecraft:block"), dynamic.createString("pos"), dynamic2));
            return dynamic.set("destination", dynamic3).set("arrival_in_ticks", dynamic.createInt(i));
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)params, (Object)exception);
            return dynamic;
        }
    }

    private <T> Dynamic<T> fixShriekParticle(Dynamic<T> dynamic, String params) {
        try {
            StringReader stringReader = new StringReader(params);
            int i = stringReader.readInt();
            return dynamic.set("delay", dynamic.createInt(i));
        } catch (Exception exception) {
            LOGGER.warn("Failed to parse particle options: {}", (Object)params, (Object)exception);
            return dynamic;
        }
    }
}

