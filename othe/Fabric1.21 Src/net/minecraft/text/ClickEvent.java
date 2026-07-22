/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.text;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringIdentifiable;

public class ClickEvent {
    public static final Codec<ClickEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(Action.CODEC.forGetter(event -> event.action), ((MapCodec)Codec.STRING.fieldOf("value")).forGetter(event -> event.value)).apply((Applicative<ClickEvent, ?>)instance, ClickEvent::new));
    private final Action action;
    private final String value;

    public ClickEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return this.action;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ClickEvent clickEvent = (ClickEvent)o;
        return this.action == clickEvent.action && this.value.equals(clickEvent.value);
    }

    public String toString() {
        return "ClickEvent{action=" + String.valueOf(this.action) + ", value='" + this.value + "'}";
    }

    public int hashCode() {
        int i = this.action.hashCode();
        i = 31 * i + this.value.hashCode();
        return i;
    }

    public static enum Action implements StringIdentifiable
    {
        OPEN_URL("open_url", true),
        OPEN_FILE("open_file", false),
        RUN_COMMAND("run_command", true),
        SUGGEST_COMMAND("suggest_command", true),
        CHANGE_PAGE("change_page", true),
        COPY_TO_CLIPBOARD("copy_to_clipboard", true);

        public static final MapCodec<Action> UNVALIDATED_CODEC;
        public static final MapCodec<Action> CODEC;
        private final boolean userDefinable;
        private final String name;

        private Action(String name, boolean userDefinable) {
            this.name = name;
            this.userDefinable = userDefinable;
        }

        public boolean isUserDefinable() {
            return this.userDefinable;
        }

        @Override
        public String asString() {
            return this.name;
        }

        public static DataResult<Action> validate(Action action) {
            if (!action.isUserDefinable()) {
                return DataResult.error(() -> "Action not allowed: " + String.valueOf(action));
            }
            return DataResult.success(action, Lifecycle.stable());
        }

        static {
            UNVALIDATED_CODEC = StringIdentifiable.createCodec(Action::values).fieldOf("action");
            CODEC = UNVALIDATED_CODEC.validate(Action::validate);
        }
    }
}

