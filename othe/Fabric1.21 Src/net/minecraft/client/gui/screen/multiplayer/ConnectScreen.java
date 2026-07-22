/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.gui.screen.multiplayer;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.QuickPlay;
import net.minecraft.client.QuickPlayLogger;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.server.ServerResourcePackManager;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.state.LoginStates;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * The connection screen is used to initiate a connection to a remote server.
 * This is only used when connecting over LAN or to a remote dedicated server.
 */
@Environment(value=EnvType.CLIENT)
public class ConnectScreen
extends Screen {
    private static final AtomicInteger CONNECTOR_THREADS_COUNT = new AtomicInteger(0);
    static final Logger LOGGER = LogUtils.getLogger();
    private static final long NARRATOR_INTERVAL = 2000L;
    public static final Text ABORTED_TEXT = Text.translatable("connect.aborted");
    public static final Text UNKNOWN_HOST_TEXT = Text.translatable("disconnect.genericReason", Text.translatable("disconnect.unknownHost"));
    /**
     * The client connection to the remote server.
     * This is not used when connecting to the client's own integrated server.
     * 
     * @see net.minecraft.client.MinecraftClient#integratedServerConnection
     */
    @Nullable
    volatile ClientConnection connection;
    @Nullable
    ChannelFuture future;
    volatile boolean connectingCancelled;
    final Screen parent;
    private Text status = Text.translatable("connect.connecting");
    private long lastNarrationTime = -1L;
    final Text failureErrorMessage;

    private ConnectScreen(Screen parent, Text failureErrorMessage) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
        this.failureErrorMessage = failureErrorMessage;
    }

    public static void connect(Screen screen, MinecraftClient client, ServerAddress address, ServerInfo info, boolean quickPlay, @Nullable CookieStorage cookieStorage) {
        if (client.currentScreen instanceof ConnectScreen) {
            LOGGER.error("Attempt to connect while already connecting");
            return;
        }
        Text text = cookieStorage != null ? ScreenTexts.CONNECT_FAILED_TRANSFER : (quickPlay ? QuickPlay.ERROR_TITLE : ScreenTexts.CONNECT_FAILED);
        ConnectScreen connectScreen = new ConnectScreen(screen, text);
        if (cookieStorage != null) {
            connectScreen.setStatus(Text.translatable("connect.transferring"));
        }
        client.disconnect();
        client.loadBlockList();
        client.ensureAbuseReportContext(ReporterEnvironment.ofThirdPartyServer(info.address));
        client.getQuickPlayLogger().setWorld(QuickPlayLogger.WorldType.MULTIPLAYER, info.address, info.name);
        client.setScreen(connectScreen);
        connectScreen.connect(client, address, info, cookieStorage);
    }

    private void connect(final MinecraftClient client, final ServerAddress address, final ServerInfo info, final @Nullable CookieStorage cookieStorage) {
        LOGGER.info("Connecting to {}, {}", (Object)address.getAddress(), (Object)address.getPort());
        Thread thread = new Thread("Server Connector #" + CONNECTOR_THREADS_COUNT.incrementAndGet()){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                InetSocketAddress inetSocketAddress = null;
                try {
                    ClientConnection clientConnection;
                    if (ConnectScreen.this.connectingCancelled) {
                        return;
                    }
                    Optional<InetSocketAddress> optional = AllowedAddressResolver.DEFAULT.resolve(address).map(Address::getInetSocketAddress);
                    if (ConnectScreen.this.connectingCancelled) {
                        return;
                    }
                    if (optional.isEmpty()) {
                        client.execute(() -> client.setScreen(new DisconnectedScreen(ConnectScreen.this.parent, ConnectScreen.this.failureErrorMessage, UNKNOWN_HOST_TEXT)));
                        return;
                    }
                    inetSocketAddress = optional.get();
                    ConnectScreen connectScreen = ConnectScreen.this;
                    synchronized (connectScreen) {
                        if (ConnectScreen.this.connectingCancelled) {
                            return;
                        }
                        clientConnection = new ClientConnection(NetworkSide.CLIENTBOUND);
                        clientConnection.resetPacketSizeLog(client.getDebugHud().getPacketSizeLog());
                        ConnectScreen.this.future = ClientConnection.connect(inetSocketAddress, client.options.shouldUseNativeTransport(), clientConnection);
                    }
                    ConnectScreen.this.future.syncUninterruptibly();
                    connectScreen = ConnectScreen.this;
                    synchronized (connectScreen) {
                        if (ConnectScreen.this.connectingCancelled) {
                            clientConnection.disconnect(ABORTED_TEXT);
                            return;
                        }
                        ConnectScreen.this.connection = clientConnection;
                        client.getServerResourcePackProvider().init(clientConnection, _1.toAcceptanceStatus(info.getResourcePackPolicy()));
                    }
                    ConnectScreen.this.connection.connect(inetSocketAddress.getHostName(), inetSocketAddress.getPort(), LoginStates.C2S, LoginStates.S2C, new ClientLoginNetworkHandler(ConnectScreen.this.connection, client, info, ConnectScreen.this.parent, false, null, ConnectScreen.this::setStatus, cookieStorage), cookieStorage != null);
                    ConnectScreen.this.connection.send(new LoginHelloC2SPacket(client.getSession().getUsername(), client.getSession().getUuidOrNull()));
                } catch (Exception exception) {
                    Exception exception2;
                    if (ConnectScreen.this.connectingCancelled) {
                        return;
                    }
                    Throwable throwable = exception.getCause();
                    Exception exception3 = throwable instanceof Exception ? (exception2 = (Exception)throwable) : exception;
                    LOGGER.error("Couldn't connect to server", exception);
                    String string = inetSocketAddress == null ? exception3.getMessage() : exception3.getMessage().replaceAll(inetSocketAddress.getHostName() + ":" + inetSocketAddress.getPort(), "").replaceAll(inetSocketAddress.toString(), "");
                    client.execute(() -> client.setScreen(new DisconnectedScreen(ConnectScreen.this.parent, ConnectScreen.this.failureErrorMessage, Text.translatable("disconnect.genericReason", string))));
                }
            }

            private static ServerResourcePackManager.AcceptanceStatus toAcceptanceStatus(ServerInfo.ResourcePackPolicy policy) {
                return switch (policy) {
                    default -> throw new MatchException(null, null);
                    case ServerInfo.ResourcePackPolicy.ENABLED -> ServerResourcePackManager.AcceptanceStatus.ALLOWED;
                    case ServerInfo.ResourcePackPolicy.DISABLED -> ServerResourcePackManager.AcceptanceStatus.DECLINED;
                    case ServerInfo.ResourcePackPolicy.PROMPT -> ServerResourcePackManager.AcceptanceStatus.PENDING;
                };
            }
        };
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        thread.start();
    }

    private void setStatus(Text status) {
        this.status = status;
    }

    @Override
    public void tick() {
        if (this.connection != null) {
            if (this.connection.isOpen()) {
                this.connection.tick();
            } else {
                this.connection.handleDisconnection();
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            ConnectScreen connectScreen = this;
            synchronized (connectScreen) {
                this.connectingCancelled = true;
                if (this.future != null) {
                    this.future.cancel(true);
                    this.future = null;
                }
                if (this.connection != null) {
                    this.connection.disconnect(ABORTED_TEXT);
                }
            }
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        long l = Util.getMeasuringTimeMs();
        if (l - this.lastNarrationTime > 2000L) {
            this.lastNarrationTime = l;
            this.client.getNarratorManager().narrate(Text.translatable("narrator.joining"));
        }
        context.drawCenteredTextWithShadow(this.textRenderer, this.status, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
    }
}

