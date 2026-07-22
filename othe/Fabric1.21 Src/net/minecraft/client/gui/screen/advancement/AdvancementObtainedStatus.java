/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen.advancement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public enum AdvancementObtainedStatus {
    OBTAINED(Identifier.ofVanilla("advancements/box_obtained"), Identifier.ofVanilla("advancements/task_frame_obtained"), Identifier.ofVanilla("advancements/challenge_frame_obtained"), Identifier.ofVanilla("advancements/goal_frame_obtained")),
    UNOBTAINED(Identifier.ofVanilla("advancements/box_unobtained"), Identifier.ofVanilla("advancements/task_frame_unobtained"), Identifier.ofVanilla("advancements/challenge_frame_unobtained"), Identifier.ofVanilla("advancements/goal_frame_unobtained"));

    private final Identifier boxTexture;
    private final Identifier taskFrameTexture;
    private final Identifier challengeFrameTexture;
    private final Identifier goalFrameTexture;

    private AdvancementObtainedStatus(Identifier boxTexture, Identifier taskFrameTexture, Identifier challengeFrameTexture, Identifier goalFrameTexture) {
        this.boxTexture = boxTexture;
        this.taskFrameTexture = taskFrameTexture;
        this.challengeFrameTexture = challengeFrameTexture;
        this.goalFrameTexture = goalFrameTexture;
    }

    public Identifier getBoxTexture() {
        return this.boxTexture;
    }

    public Identifier getFrameTexture(AdvancementFrame frame) {
        return switch (frame) {
            default -> throw new MatchException(null, null);
            case AdvancementFrame.TASK -> this.taskFrameTexture;
            case AdvancementFrame.CHALLENGE -> this.challengeFrameTexture;
            case AdvancementFrame.GOAL -> this.goalFrameTexture;
        };
    }
}

