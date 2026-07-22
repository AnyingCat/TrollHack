/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.network;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.PublicPlayerSession;
import net.minecraft.network.message.MessageVerifier;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class PlayerListEntry {
    private final GameProfile profile;
    private final java.util.function.Supplier<SkinTextures> texturesSupplier;
    private GameMode gameMode = GameMode.DEFAULT;
    private int latency;
    @Nullable
    private Text displayName;
    @Nullable
    private PublicPlayerSession session;
    private MessageVerifier messageVerifier;

    public PlayerListEntry(GameProfile profile, boolean secureChatEnforced) {
        this.profile = profile;
        this.messageVerifier = PlayerListEntry.getInitialVerifier(secureChatEnforced);
        Supplier<java.util.function.Supplier> supplier = Suppliers.memoize(() -> PlayerListEntry.texturesSupplier(profile));
        this.texturesSupplier = () -> (SkinTextures)((java.util.function.Supplier)supplier.get()).get();
    }

    private static java.util.function.Supplier<SkinTextures> texturesSupplier(GameProfile profile) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerSkinProvider playerSkinProvider = minecraftClient.getSkinProvider();
        CompletableFuture<SkinTextures> completableFuture = playerSkinProvider.fetchSkinTextures(profile);
        boolean bl = !minecraftClient.uuidEquals(profile.getId());
        SkinTextures skinTextures = DefaultSkinHelper.getSkinTextures(profile);
        return () -> {
            SkinTextures skinTextures2 = completableFuture.getNow(skinTextures);
            if (bl && !skinTextures2.secure()) {
                return skinTextures;
            }
            return skinTextures2;
        };
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    @Nullable
    public PublicPlayerSession getSession() {
        return this.session;
    }

    public MessageVerifier getMessageVerifier() {
        return this.messageVerifier;
    }

    public boolean hasPublicKey() {
        return this.session != null;
    }

    protected void setSession(PublicPlayerSession session) {
        this.session = session;
        this.messageVerifier = session.createVerifier(PlayerPublicKey.EXPIRATION_GRACE_PERIOD);
    }

    protected void resetSession(boolean secureChatEnforced) {
        this.session = null;
        this.messageVerifier = PlayerListEntry.getInitialVerifier(secureChatEnforced);
    }

    private static MessageVerifier getInitialVerifier(boolean secureChatEnforced) {
        return secureChatEnforced ? MessageVerifier.UNVERIFIED : MessageVerifier.NO_SIGNATURE;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    protected void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public int getLatency() {
        return this.latency;
    }

    protected void setLatency(int latency) {
        this.latency = latency;
    }

    public SkinTextures getSkinTextures() {
        return this.texturesSupplier.get();
    }

    @Nullable
    public Team getScoreboardTeam() {
        return MinecraftClient.getInstance().world.getScoreboard().getScoreHolderTeam(this.getProfile().getName());
    }

    public void setDisplayName(@Nullable Text displayName) {
        this.displayName = displayName;
    }

    @Nullable
    public Text getDisplayName() {
        return this.displayName;
    }
}

