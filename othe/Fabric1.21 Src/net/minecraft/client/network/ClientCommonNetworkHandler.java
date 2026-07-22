/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.network;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.resource.server.ServerResourcePackLoader;
import net.minecraft.client.session.telemetry.WorldSession;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.listener.ServerPacketListener;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.UnknownCustomPayload;
import net.minecraft.network.packet.c2s.common.CommonPongC2SPacket;
import net.minecraft.network.packet.c2s.common.CookieResponseC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.common.CustomReportDetailsS2CPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerLinksS2CPacket;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.network.packet.s2c.common.StoreCookieS2CPacket;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.ServerLinks;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.crash.ReportType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class ClientCommonNetworkHandler
implements ClientCommonPacketListener {
    private static final Text LOST_CONNECTION_TEXT = Text.translatable("disconnect.lost");
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final MinecraftClient client;
    protected final ClientConnection connection;
    @Nullable
    protected final ServerInfo serverInfo;
    @Nullable
    protected String brand;
    protected final WorldSession worldSession;
    @Nullable
    protected final Screen postDisconnectScreen;
    protected boolean transferring;
    @Deprecated(forRemoval=true)
    protected final boolean strictErrorHandling;
    private final List<QueuedPacket> queuedPackets = new ArrayList<QueuedPacket>();
    protected final Map<Identifier, byte[]> serverCookies;
    protected Map<String, String> customReportDetails;
    protected ServerLinks serverLinks;

    protected ClientCommonNetworkHandler(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        this.client = client;
        this.connection = connection;
        this.serverInfo = connectionState.serverInfo();
        this.brand = connectionState.serverBrand();
        this.worldSession = connectionState.worldSession();
        this.postDisconnectScreen = connectionState.postDisconnectScreen();
        this.serverCookies = connectionState.serverCookies();
        this.strictErrorHandling = connectionState.strictErrorHandling();
        this.customReportDetails = connectionState.customReportDetails();
        this.serverLinks = connectionState.serverLinks();
    }

    @Override
    public void onPacketException(Packet packet, Exception exception) {
        LOGGER.error("Failed to handle packet {}", (Object)packet, (Object)exception);
        Optional<Path> optional = this.savePacketErrorReport(packet, exception);
        Optional<URI> optional2 = this.serverLinks.getEntryFor(ServerLinks.Known.BUG_REPORT).map(ServerLinks.Entry::link);
        if (this.strictErrorHandling) {
            this.connection.disconnect(new DisconnectionInfo(Text.translatable("disconnect.packetError"), optional, optional2));
        }
    }

    @Override
    public DisconnectionInfo createDisconnectionInfo(Text reason, Throwable exception) {
        Optional<Path> optional = this.savePacketErrorReport(null, exception);
        Optional<URI> optional2 = this.serverLinks.getEntryFor(ServerLinks.Known.BUG_REPORT).map(ServerLinks.Entry::link);
        return new DisconnectionInfo(reason, optional, optional2);
    }

    private Optional<Path> savePacketErrorReport(@Nullable Packet packet, Throwable exception) {
        CrashReport crashReport = CrashReport.create(exception, "Packet handling error");
        NetworkThreadUtils.fillCrashReport(crashReport, this, packet);
        Path path = this.client.runDirectory.toPath().resolve("debug");
        Path path2 = path.resolve("disconnect-" + Util.getFormattedCurrentTime() + "-client.txt");
        Optional<ServerLinks.Entry> optional = this.serverLinks.getEntryFor(ServerLinks.Known.BUG_REPORT);
        List<String> list = optional.map(bugReportEntry -> List.of("Server bug reporting link: " + String.valueOf(bugReportEntry.link()))).orElse(List.of());
        if (crashReport.writeToFile(path2, ReportType.MINECRAFT_NETWORK_PROTOCOL_ERROR_REPORT, list)) {
            return Optional.of(path2);
        }
        return Optional.empty();
    }

    @Override
    public boolean accepts(Packet<?> packet) {
        if (ClientCommonPacketListener.super.accepts(packet)) {
            return true;
        }
        return this.transferring && (packet instanceof StoreCookieS2CPacket || packet instanceof ServerTransferS2CPacket);
    }

    @Override
    public void onKeepAlive(KeepAliveS2CPacket packet) {
        this.send(new KeepAliveC2SPacket(packet.getId()), () -> !RenderSystem.isFrozenAtPollEvents(), Duration.ofMinutes(1L));
    }

    @Override
    public void onPing(CommonPingS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.sendPacket(new CommonPongC2SPacket(packet.getParameter()));
    }

    @Override
    public void onCustomPayload(CustomPayloadS2CPacket packet) {
        CustomPayload customPayload = packet.payload();
        if (customPayload instanceof UnknownCustomPayload) {
            return;
        }
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (customPayload instanceof BrandCustomPayload) {
            BrandCustomPayload brandCustomPayload = (BrandCustomPayload)customPayload;
            this.brand = brandCustomPayload.brand();
            this.worldSession.setBrand(brandCustomPayload.brand());
        } else {
            this.onCustomPayload(customPayload);
        }
    }

    protected abstract void onCustomPayload(CustomPayload var1);

    @Override
    public void onResourcePackSend(ResourcePackSendS2CPacket packet) {
        ServerInfo.ResourcePackPolicy resourcePackPolicy;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        UUID uUID = packet.id();
        URL uRL = ClientCommonNetworkHandler.getParsedResourcePackUrl(packet.url());
        if (uRL == null) {
            this.connection.send(new ResourcePackStatusC2SPacket(uUID, ResourcePackStatusC2SPacket.Status.INVALID_URL));
            return;
        }
        String string = packet.hash();
        boolean bl = packet.required();
        ServerInfo.ResourcePackPolicy resourcePackPolicy2 = resourcePackPolicy = this.serverInfo != null ? this.serverInfo.getResourcePackPolicy() : ServerInfo.ResourcePackPolicy.PROMPT;
        if (resourcePackPolicy == ServerInfo.ResourcePackPolicy.PROMPT || bl && resourcePackPolicy == ServerInfo.ResourcePackPolicy.DISABLED) {
            this.client.setScreen(this.createConfirmServerResourcePackScreen(uUID, uRL, string, bl, packet.prompt().orElse(null)));
        } else {
            this.client.getServerResourcePackProvider().addResourcePack(uUID, uRL, string);
        }
    }

    @Override
    public void onResourcePackRemove(ResourcePackRemoveS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        packet.id().ifPresentOrElse(id -> this.client.getServerResourcePackProvider().remove((UUID)id), () -> this.client.getServerResourcePackProvider().removeAll());
    }

    static Text getPrompt(Text requirementPrompt, @Nullable Text customPrompt) {
        if (customPrompt == null) {
            return requirementPrompt;
        }
        return Text.translatable("multiplayer.texturePrompt.serverPrompt", requirementPrompt, customPrompt);
    }

    @Nullable
    private static URL getParsedResourcePackUrl(String url) {
        try {
            URL uRL = new URL(url);
            String string = uRL.getProtocol();
            if ("http".equals(string) || "https".equals(string)) {
                return uRL;
            }
        } catch (MalformedURLException malformedURLException) {
            return null;
        }
        return null;
    }

    @Override
    public void onCookieRequest(CookieRequestS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.connection.send(new CookieResponseC2SPacket(packet.key(), this.serverCookies.get(packet.key())));
    }

    @Override
    public void onStoreCookie(StoreCookieS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.serverCookies.put(packet.key(), packet.payload());
    }

    @Override
    public void onCustomReportDetails(CustomReportDetailsS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        this.customReportDetails = packet.details();
    }

    @Override
    public void onServerLinks(ServerLinksS2CPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        List<ServerLinks.StringifiedEntry> list = packet.links();
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize(list.size());
        for (ServerLinks.StringifiedEntry stringifiedEntry : list) {
            try {
                URI uRI = Util.validateUri(stringifiedEntry.link());
                builder.add(new ServerLinks.Entry(stringifiedEntry.type(), uRI));
            } catch (Exception exception) {
                LOGGER.warn("Received invalid link for type {}:{}", stringifiedEntry.type(), stringifiedEntry.link(), exception);
            }
        }
        this.serverLinks = new ServerLinks((List<ServerLinks.Entry>)((Object)builder.build()));
    }

    @Override
    public void onServerTransfer(ServerTransferS2CPacket packet) {
        this.transferring = true;
        NetworkThreadUtils.forceMainThread(packet, this, this.client);
        if (this.serverInfo == null) {
            throw new IllegalStateException("Cannot transfer to server from singleplayer");
        }
        this.connection.disconnect(Text.translatable("disconnect.transfer"));
        this.connection.tryDisableAutoRead();
        this.connection.handleDisconnection();
        ServerAddress serverAddress = new ServerAddress(packet.host(), packet.port());
        ConnectScreen.connect(Objects.requireNonNullElseGet(this.postDisconnectScreen, TitleScreen::new), this.client, serverAddress, this.serverInfo, false, new CookieStorage(this.serverCookies));
    }

    @Override
    public void onDisconnect(DisconnectS2CPacket packet) {
        this.connection.disconnect(packet.reason());
    }

    protected void sendQueuedPackets() {
        Iterator<QueuedPacket> iterator = this.queuedPackets.iterator();
        while (iterator.hasNext()) {
            QueuedPacket queuedPacket = iterator.next();
            if (queuedPacket.sendCondition().getAsBoolean()) {
                this.sendPacket(queuedPacket.packet);
                iterator.remove();
                continue;
            }
            if (queuedPacket.expirationTime() > Util.getMeasuringTimeMs()) continue;
            iterator.remove();
        }
    }

    public void sendPacket(Packet<?> packet) {
        this.connection.send(packet);
    }

    @Override
    public void onDisconnected(DisconnectionInfo info) {
        this.worldSession.onUnload();
        this.client.disconnect(this.createDisconnectedScreen(info), this.transferring);
        LOGGER.warn("Client disconnected with reason: {}", (Object)info.reason().getString());
    }

    @Override
    public void addCustomCrashReportInfo(CrashReport report, CrashReportSection section) {
        section.add("Server type", () -> this.serverInfo != null ? this.serverInfo.getServerType().toString() : "<none>");
        section.add("Server brand", () -> this.brand);
        if (!this.customReportDetails.isEmpty()) {
            CrashReportSection crashReportSection = report.addElement("Custom Server Details");
            this.customReportDetails.forEach(crashReportSection::add);
        }
    }

    protected Screen createDisconnectedScreen(DisconnectionInfo info) {
        Screen screen = Objects.requireNonNullElseGet(this.postDisconnectScreen, () -> new MultiplayerScreen(new TitleScreen()));
        if (this.serverInfo != null && this.serverInfo.isRealm()) {
            return new DisconnectedRealmsScreen(screen, LOST_CONNECTION_TEXT, info.reason());
        }
        return new DisconnectedScreen(screen, LOST_CONNECTION_TEXT, info);
    }

    @Nullable
    public String getBrand() {
        return this.brand;
    }

    private void send(Packet<? extends ServerPacketListener> packet, BooleanSupplier sendCondition, Duration expiry) {
        if (sendCondition.getAsBoolean()) {
            this.sendPacket(packet);
        } else {
            this.queuedPackets.add(new QueuedPacket(packet, sendCondition, Util.getMeasuringTimeMs() + expiry.toMillis()));
        }
    }

    private Screen createConfirmServerResourcePackScreen(UUID id, URL url, String hash, boolean required, @Nullable Text prompt) {
        Screen screen = this.client.currentScreen;
        if (screen instanceof ConfirmServerResourcePackScreen) {
            ConfirmServerResourcePackScreen confirmServerResourcePackScreen = (ConfirmServerResourcePackScreen)screen;
            return confirmServerResourcePackScreen.add(this.client, id, url, hash, required, prompt);
        }
        return new ConfirmServerResourcePackScreen(this.client, screen, List.of(new ConfirmServerResourcePackScreen.Pack(id, url, hash)), required, prompt);
    }

    @Environment(value=EnvType.CLIENT)
    record QueuedPacket(Packet<? extends ServerPacketListener> packet, BooleanSupplier sendCondition, long expirationTime) {
    }

    @Environment(value=EnvType.CLIENT)
    class ConfirmServerResourcePackScreen
    extends ConfirmScreen {
        private final List<Pack> packs;
        @Nullable
        private final Screen parent;

        ConfirmServerResourcePackScreen(@Nullable MinecraftClient client, Screen parent, List<Pack> pack, @Nullable boolean required, Text prompt) {
            super(confirmed -> {
                client.setScreen(parent);
                ServerResourcePackLoader serverResourcePackLoader = client.getServerResourcePackProvider();
                if (confirmed) {
                    if (clientCommonNetworkHandler.serverInfo != null) {
                        clientCommonNetworkHandler.serverInfo.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.ENABLED);
                    }
                    serverResourcePackLoader.acceptAll();
                } else {
                    serverResourcePackLoader.declineAll();
                    if (required) {
                        clientCommonNetworkHandler.connection.disconnect(Text.translatable("multiplayer.requiredTexturePrompt.disconnect"));
                    } else if (clientCommonNetworkHandler.serverInfo != null) {
                        clientCommonNetworkHandler.serverInfo.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.DISABLED);
                    }
                }
                for (Pack pack : pack) {
                    serverResourcePackLoader.addResourcePack(pack.id, pack.url, pack.hash);
                }
                if (clientCommonNetworkHandler.serverInfo != null) {
                    ServerList.updateServerListEntry(clientCommonNetworkHandler.serverInfo);
                }
            }, required ? Text.translatable("multiplayer.requiredTexturePrompt.line1") : Text.translatable("multiplayer.texturePrompt.line1"), ClientCommonNetworkHandler.getPrompt(required ? Text.translatable("multiplayer.requiredTexturePrompt.line2").formatted(Formatting.YELLOW, Formatting.BOLD) : Text.translatable("multiplayer.texturePrompt.line2"), prompt), required ? ScreenTexts.PROCEED : ScreenTexts.YES, required ? ScreenTexts.DISCONNECT : ScreenTexts.NO);
            this.packs = pack;
            this.parent = parent;
        }

        public ConfirmServerResourcePackScreen add(MinecraftClient client, UUID id, URL url, String hash, boolean required, @Nullable Text prompt) {
            ImmutableCollection list = ((ImmutableList.Builder)((ImmutableList.Builder)ImmutableList.builderWithExpectedSize(this.packs.size() + 1).addAll(this.packs)).add(new Pack(id, url, hash))).build();
            return new ConfirmServerResourcePackScreen(client, this.parent, (List<Pack>)((Object)list), required, prompt);
        }

        @Environment(value=EnvType.CLIENT)
        record Pack(UUID id, URL url, String hash) {
        }
    }
}

