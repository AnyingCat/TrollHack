/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.scoreboard;

import com.mojang.authlib.GameProfile;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface ScoreHolder {
    public static final String WILDCARD_NAME = "*";
    public static final ScoreHolder WILDCARD = new ScoreHolder(){

        @Override
        public String getNameForScoreboard() {
            return ScoreHolder.WILDCARD_NAME;
        }
    };

    /**
     * {@return the name uniquely identifying the score holder}
     * 
     * <p>Unlike {@link net.minecraft.entity.Entity#getName}, this is guaranteed to be unique. This is the UUID
     * for all entities except players (which use the player's username).
     * 
     * @see net.minecraft.entity.Entity#getName
     * @see net.minecraft.entity.Entity#getUuidAsString
     */
    public String getNameForScoreboard();

    @Nullable
    default public Text getDisplayName() {
        return null;
    }

    default public Text getStyledDisplayName() {
        Text text = this.getDisplayName();
        if (text != null) {
            return text.copy().styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(this.getNameForScoreboard()))));
        }
        return Text.literal(this.getNameForScoreboard());
    }

    public static ScoreHolder fromName(final String name) {
        if (name.equals(WILDCARD_NAME)) {
            return WILDCARD;
        }
        final MutableText text = Text.literal(name);
        return new ScoreHolder(){

            @Override
            public String getNameForScoreboard() {
                return name;
            }

            @Override
            public Text getStyledDisplayName() {
                return text;
            }
        };
    }

    public static ScoreHolder fromProfile(GameProfile gameProfile) {
        final String string = gameProfile.getName();
        return new ScoreHolder(){

            @Override
            public String getNameForScoreboard() {
                return string;
            }
        };
    }
}

