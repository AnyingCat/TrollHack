/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.JsonHelper;

public class TextFixes {
    private static final String EMPTY_TEXT = TextFixes.text("");

    public static <T> Dynamic<T> text(DynamicOps<T> ops, String string) {
        String string2 = TextFixes.text(string);
        return new Dynamic<T>(ops, ops.createString(string2));
    }

    public static <T> Dynamic<T> empty(DynamicOps<T> ops) {
        return new Dynamic<T>(ops, ops.createString(EMPTY_TEXT));
    }

    private static String text(String string) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", string);
        return JsonHelper.toSortedString(jsonObject);
    }

    public static <T> Dynamic<T> translate(DynamicOps<T> ops, String key) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("translate", key);
        return new Dynamic<T>(ops, ops.createString(JsonHelper.toSortedString(jsonObject)));
    }

    public static <T> Dynamic<T> fixText(Dynamic<T> dynamic) {
        return DataFixUtils.orElse(dynamic.asString().map(string -> TextFixes.text(dynamic.getOps(), string)).result(), dynamic);
    }

    public static Dynamic<?> text(Dynamic<?> dynamic) {
        Optional<String> optional = dynamic.asString().result();
        if (optional.isEmpty()) {
            return dynamic;
        }
        String string = optional.get();
        if (string.isEmpty() || string.equals("null")) {
            return TextFixes.empty(dynamic.getOps());
        }
        char c = string.charAt(0);
        char d = string.charAt(string.length() - 1);
        if (c == '\"' && d == '\"' || c == '{' && d == '}' || c == '[' && d == ']') {
            try {
                JsonElement jsonElement = JsonParser.parseString(string);
                if (jsonElement.isJsonPrimitive()) {
                    return TextFixes.text(dynamic.getOps(), jsonElement.getAsString());
                }
                return dynamic.createString(JsonHelper.toSortedString(jsonElement));
            } catch (JsonParseException jsonParseException) {
                // empty catch block
            }
        }
        return TextFixes.text(dynamic.getOps(), string);
    }

    public static Optional<String> getTranslate(String json) {
        try {
            JsonObject jsonObject;
            JsonElement jsonElement2;
            JsonElement jsonElement = JsonParser.parseString(json);
            if (jsonElement.isJsonObject() && (jsonElement2 = (jsonObject = jsonElement.getAsJsonObject()).get("translate")) != null && jsonElement2.isJsonPrimitive()) {
                return Optional.of(jsonElement2.getAsString());
            }
        } catch (JsonParseException jsonParseException) {
            // empty catch block
        }
        return Optional.empty();
    }
}

