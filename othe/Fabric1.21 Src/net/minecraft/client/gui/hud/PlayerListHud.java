/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.hud;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nullables;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

/**
 * Responsible for rendering the player list while the {@linkplain
 * net.minecraft.client.option.GameOptions#playerListKey player list
 * key} is pressed.
 * 
 * <p>The current instance used by the client can be obtained by {@code
 * MinecraftClient.getInstance().inGameHud.getPlayerListHud()}.
 */
@Environment(value=EnvType.CLIENT)
public class PlayerListHud {
    private static final Identifier PING_UNKNOWN_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_unknown");
    private static final Identifier PING_1_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_1");
    private static final Identifier PING_2_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_2");
    private static final Identifier PING_3_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_3");
    private static final Identifier PING_4_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_4");
    private static final Identifier PING_5_ICON_TEXTURE = Identifier.ofVanilla("icon/ping_5");
    private static final Identifier CONTAINER_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/container_blinking");
    private static final Identifier CONTAINER_HEART_TEXTURE = Identifier.ofVanilla("hud/heart/container");
    private static final Identifier FULL_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/full_blinking");
    private static final Identifier HALF_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/half_blinking");
    private static final Identifier ABSORBING_FULL_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/absorbing_full_blinking");
    private static final Identifier FULL_HEART_TEXTURE = Identifier.ofVanilla("hud/heart/full");
    private static final Identifier ABSORBING_HALF_HEART_BLINKING_TEXTURE = Identifier.ofVanilla("hud/heart/absorbing_half_blinking");
    private static final Identifier HALF_HEART_TEXTURE = Identifier.ofVanilla("hud/heart/half");
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator.comparingInt(entry -> entry.getGameMode() == GameMode.SPECTATOR ? 1 : 0).thenComparing(entry -> Nullables.mapOrElse(entry.getScoreboardTeam(), Team::getName, "")).thenComparing(entry -> entry.getProfile().getName(), String::compareToIgnoreCase);
    public static final int MAX_ROWS = 20;
    private final MinecraftClient client;
    private final InGameHud inGameHud;
    @Nullable
    private Text footer;
    @Nullable
    private Text header;
    private boolean visible;
    private final Map<UUID, Heart> hearts = new Object2ObjectOpenHashMap<UUID, Heart>();

    public PlayerListHud(MinecraftClient client, InGameHud inGameHud) {
        this.client = client;
        this.inGameHud = inGameHud;
    }

    /**
     * {@return the player name rendered by this HUD}
     */
    public Text getPlayerName(PlayerListEntry entry) {
        if (entry.getDisplayName() != null) {
            return this.applyGameModeFormatting(entry, entry.getDisplayName().copy());
        }
        return this.applyGameModeFormatting(entry, Team.decorateName(entry.getScoreboardTeam(), Text.literal(entry.getProfile().getName())));
    }

    /**
     * {@linkplain net.minecraft.util.Formatting#ITALIC Italicizes} the given text if
     * the given player is in {@linkplain net.minecraft.world.GameMode#SPECTATOR spectator mode}.
     */
    private Text applyGameModeFormatting(PlayerListEntry entry, MutableText name) {
        return entry.getGameMode() == GameMode.SPECTATOR ? name.formatted(Formatting.ITALIC) : name;
    }

    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.hearts.clear();
            this.visible = visible;
            if (visible) {
                MutableText text = Texts.join(this.collectPlayerEntries(), Text.literal(", "), this::getPlayerName);
                this.client.getNarratorManager().narrate(Text.translatable("multiplayer.player.list.narration", text));
            }
        }
    }

    private List<PlayerListEntry> collectPlayerEntries() {
        return this.client.player.networkHandler.getListedPlayerListEntries().stream().sorted(ENTRY_ORDERING).limit(80L).toList();
    }

    public void render(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, @Nullable ScoreboardObjective objective) {
        int x;
        int u;
        boolean bl;
        int n;
        int m;
        List<PlayerListEntry> list = this.collectPlayerEntries();
        ArrayList<ScoreDisplayEntry> list2 = new ArrayList<ScoreDisplayEntry>(list.size());
        int i = this.client.textRenderer.getWidth(" ");
        int j = 0;
        int k = 0;
        for (PlayerListEntry playerListEntry : list) {
            Text text = this.getPlayerName(playerListEntry);
            j = Math.max(j, this.client.textRenderer.getWidth(text));
            int l = 0;
            MutableText text2 = null;
            m = 0;
            if (objective != null) {
                ScoreHolder scoreHolder = ScoreHolder.fromProfile(playerListEntry.getProfile());
                ReadableScoreboardScore readableScoreboardScore = scoreboard.getScore(scoreHolder, objective);
                if (readableScoreboardScore != null) {
                    l = readableScoreboardScore.getScore();
                }
                if (objective.getRenderType() != ScoreboardCriterion.RenderType.HEARTS) {
                    NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.YELLOW);
                    text2 = ReadableScoreboardScore.getFormattedScore(readableScoreboardScore, numberFormat);
                    m = this.client.textRenderer.getWidth(text2);
                    k = Math.max(k, m > 0 ? i + m : 0);
                }
            }
            list2.add(new ScoreDisplayEntry(text, l, text2, m));
        }
        if (!this.hearts.isEmpty()) {
            Set set = list.stream().map(playerEntry -> playerEntry.getProfile().getId()).collect(Collectors.toSet());
            this.hearts.keySet().removeIf(uuid -> !set.contains(uuid));
        }
        int o = n = list.size();
        int p = 1;
        while (o > 20) {
            o = (n + ++p - 1) / p;
        }
        boolean bl2 = bl = this.client.isInSingleplayer() || this.client.getNetworkHandler().getConnection().isEncrypted();
        int q = objective != null ? (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS ? 90 : k) : 0;
        m = Math.min(p * ((bl ? 9 : 0) + j + q + 13), scaledWindowWidth - 50) / p;
        int r = scaledWindowWidth / 2 - (m * p + (p - 1) * 5) / 2;
        int s = 10;
        int t = m * p + (p - 1) * 5;
        List<OrderedText> list3 = null;
        if (this.header != null) {
            list3 = this.client.textRenderer.wrapLines(this.header, scaledWindowWidth - 50);
            for (OrderedText orderedText : list3) {
                t = Math.max(t, this.client.textRenderer.getWidth(orderedText));
            }
        }
        List<OrderedText> list4 = null;
        if (this.footer != null) {
            list4 = this.client.textRenderer.wrapLines(this.footer, scaledWindowWidth - 50);
            for (OrderedText orderedText2 : list4) {
                t = Math.max(t, this.client.textRenderer.getWidth(orderedText2));
            }
        }
        if (list3 != null) {
            context.fill(scaledWindowWidth / 2 - t / 2 - 1, s - 1, scaledWindowWidth / 2 + t / 2 + 1, s + list3.size() * this.client.textRenderer.fontHeight, Integer.MIN_VALUE);
            for (OrderedText orderedText2 : list3) {
                u = this.client.textRenderer.getWidth(orderedText2);
                context.drawTextWithShadow(this.client.textRenderer, orderedText2, scaledWindowWidth / 2 - u / 2, s, -1);
                s += this.client.textRenderer.fontHeight;
            }
            ++s;
        }
        context.fill(scaledWindowWidth / 2 - t / 2 - 1, s - 1, scaledWindowWidth / 2 + t / 2 + 1, s + o * 9, Integer.MIN_VALUE);
        int n2 = this.client.options.getTextBackgroundColor(0x20FFFFFF);
        for (int w = 0; w < n; ++w) {
            int aa;
            int ab;
            u = w / o;
            x = w % o;
            int y = r + u * m + u * 5;
            int z = s + x * 9;
            context.fill(y, z, y + m, z + 8, n2);
            RenderSystem.enableBlend();
            if (w >= list.size()) continue;
            PlayerListEntry playerListEntry2 = list.get(w);
            ScoreDisplayEntry scoreDisplayEntry = (ScoreDisplayEntry)list2.get(w);
            GameProfile gameProfile = playerListEntry2.getProfile();
            if (bl) {
                PlayerEntity playerEntity = this.client.world.getPlayerByUuid(gameProfile.getId());
                boolean bl22 = playerEntity != null && LivingEntityRenderer.shouldFlipUpsideDown(playerEntity);
                boolean bl3 = playerEntity != null && playerEntity.isPartVisible(PlayerModelPart.HAT);
                PlayerSkinDrawer.draw(context, playerListEntry2.getSkinTextures().texture(), y, z, 8, bl3, bl22);
                y += 9;
            }
            context.drawTextWithShadow(this.client.textRenderer, scoreDisplayEntry.name, y, z, playerListEntry2.getGameMode() == GameMode.SPECTATOR ? -1862270977 : Colors.WHITE);
            if (objective != null && playerListEntry2.getGameMode() != GameMode.SPECTATOR && (ab = (aa = y + j + 1) + q) - aa > 5) {
                this.renderScoreboardObjective(objective, z, scoreDisplayEntry, aa, ab, gameProfile.getId(), context);
            }
            this.renderLatencyIcon(context, m, y - (bl ? 9 : 0), z, playerListEntry2);
        }
        if (list4 != null) {
            context.fill(scaledWindowWidth / 2 - t / 2 - 1, (s += o * 9 + 1) - 1, scaledWindowWidth / 2 + t / 2 + 1, s + list4.size() * this.client.textRenderer.fontHeight, Integer.MIN_VALUE);
            for (OrderedText orderedText3 : list4) {
                x = this.client.textRenderer.getWidth(orderedText3);
                context.drawTextWithShadow(this.client.textRenderer, orderedText3, scaledWindowWidth / 2 - x / 2, s, -1);
                s += this.client.textRenderer.fontHeight;
            }
        }
    }

    protected void renderLatencyIcon(DrawContext context, int width, int x, int y, PlayerListEntry entry) {
        Identifier identifier = entry.getLatency() < 0 ? PING_UNKNOWN_ICON_TEXTURE : (entry.getLatency() < 150 ? PING_5_ICON_TEXTURE : (entry.getLatency() < 300 ? PING_4_ICON_TEXTURE : (entry.getLatency() < 600 ? PING_3_ICON_TEXTURE : (entry.getLatency() < 1000 ? PING_2_ICON_TEXTURE : PING_1_ICON_TEXTURE))));
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 100.0f);
        context.drawGuiTexture(identifier, x + width - 11, y, 10, 8);
        context.getMatrices().pop();
    }

    private void renderScoreboardObjective(ScoreboardObjective objective, int y, ScoreDisplayEntry scoreDisplayEntry, int left, int right, UUID uuid, DrawContext context) {
        if (objective.getRenderType() == ScoreboardCriterion.RenderType.HEARTS) {
            this.renderHearts(y, left, right, uuid, context, scoreDisplayEntry.score);
        } else if (scoreDisplayEntry.formattedScore != null) {
            context.drawTextWithShadow(this.client.textRenderer, scoreDisplayEntry.formattedScore, right - scoreDisplayEntry.scoreWidth, y, 0xFFFFFF);
        }
    }

    private void renderHearts(int y, int left, int right, UUID uuid, DrawContext context, int score) {
        int l;
        Heart heart = this.hearts.computeIfAbsent(uuid, uuid2 -> new Heart(score));
        heart.tick(score, this.inGameHud.getTicks());
        int i = MathHelper.ceilDiv(Math.max(score, heart.getPrevScore()), 2);
        int j = Math.max(score, Math.max(heart.getPrevScore(), 20)) / 2;
        boolean bl = heart.useHighlighted(this.inGameHud.getTicks());
        if (i <= 0) {
            return;
        }
        int k = MathHelper.floor(Math.min((float)(right - left - 4) / (float)j, 9.0f));
        if (k <= 3) {
            float f = MathHelper.clamp((float)score / 20.0f, 0.0f, 1.0f);
            int l2 = (int)((1.0f - f) * 255.0f) << 16 | (int)(f * 255.0f) << 8;
            float g = (float)score / 2.0f;
            MutableText text = Text.translatable("multiplayer.player.list.hp", Float.valueOf(g));
            MutableText text2 = right - this.client.textRenderer.getWidth(text) >= left ? text : Text.literal(Float.toString(g));
            context.drawTextWithShadow(this.client.textRenderer, text2, (right + left - this.client.textRenderer.getWidth(text2)) / 2, y, l2);
            return;
        }
        Identifier identifier = bl ? CONTAINER_HEART_BLINKING_TEXTURE : CONTAINER_HEART_TEXTURE;
        for (l = i; l < j; ++l) {
            context.drawGuiTexture(identifier, left + l * k, y, 9, 9);
        }
        for (l = 0; l < i; ++l) {
            context.drawGuiTexture(identifier, left + l * k, y, 9, 9);
            if (bl) {
                if (l * 2 + 1 < heart.getPrevScore()) {
                    context.drawGuiTexture(FULL_HEART_BLINKING_TEXTURE, left + l * k, y, 9, 9);
                }
                if (l * 2 + 1 == heart.getPrevScore()) {
                    context.drawGuiTexture(HALF_HEART_BLINKING_TEXTURE, left + l * k, y, 9, 9);
                }
            }
            if (l * 2 + 1 < score) {
                context.drawGuiTexture(l >= 10 ? ABSORBING_FULL_HEART_BLINKING_TEXTURE : FULL_HEART_TEXTURE, left + l * k, y, 9, 9);
            }
            if (l * 2 + 1 != score) continue;
            context.drawGuiTexture(l >= 10 ? ABSORBING_HALF_HEART_BLINKING_TEXTURE : HALF_HEART_TEXTURE, left + l * k, y, 9, 9);
        }
    }

    public void setFooter(@Nullable Text footer) {
        this.footer = footer;
    }

    public void setHeader(@Nullable Text header) {
        this.header = header;
    }

    public void clear() {
        this.header = null;
        this.footer = null;
    }

    @Environment(value=EnvType.CLIENT)
    record ScoreDisplayEntry(Text name, int score, @Nullable Text formattedScore, int scoreWidth) {
        @Nullable
        public Text formattedScore() {
            return this.formattedScore;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Heart {
        private static final long COOLDOWN_TICKS = 20L;
        private static final long SCORE_DECREASE_HIGHLIGHT_TICKS = 20L;
        private static final long SCORE_INCREASE_HIGHLIGHT_TICKS = 10L;
        private int score;
        private int prevScore;
        private long lastScoreChangeTick;
        private long highlightEndTick;

        public Heart(int score) {
            this.prevScore = score;
            this.score = score;
        }

        public void tick(int score, long currentTick) {
            if (score != this.score) {
                long l = score < this.score ? 20L : 10L;
                this.highlightEndTick = currentTick + l;
                this.score = score;
                this.lastScoreChangeTick = currentTick;
            }
            if (currentTick - this.lastScoreChangeTick > 20L) {
                this.prevScore = score;
            }
        }

        public int getPrevScore() {
            return this.prevScore;
        }

        public boolean useHighlighted(long currentTick) {
            return this.highlightEndTick > currentTick && (this.highlightEndTick - currentTick) % 6L >= 3L;
        }
    }
}

