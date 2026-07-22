/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.resource.server;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.GameVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.realms.SizeUnit;
import net.minecraft.client.resource.server.DownloadQueuer;
import net.minecraft.client.resource.server.PackStateChangeCallback;
import net.minecraft.client.resource.server.ReloadScheduler;
import net.minecraft.client.resource.server.ServerResourcePackManager;
import net.minecraft.client.session.Session;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ZipResourcePack;
import net.minecraft.text.Text;
import net.minecraft.util.Downloader;
import net.minecraft.util.NetworkUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ServerResourcePackLoader
implements AutoCloseable {
    private static final Text SERVER_NAME_TEXT = Text.translatable("resourcePack.server.name");
    private static final Pattern SHA1_PATTERN = Pattern.compile("^[a-fA-F0-9]{40}$");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourcePackProvider NOOP_PROVIDER = profileAdder -> {};
    private static final ResourcePackPosition POSITION = new ResourcePackPosition(true, ResourcePackProfile.InsertionPosition.TOP, true);
    private static final PackStateChangeCallback DEBUG_PACK_STATE_CHANGE_CALLBACK = new PackStateChangeCallback(){

        @Override
        public void onStateChanged(UUID id, PackStateChangeCallback.State state) {
            LOGGER.debug("Downloaded pack {} changed state to {}", (Object)id, (Object)state);
        }

        @Override
        public void onFinish(UUID id, PackStateChangeCallback.FinishState state) {
            LOGGER.debug("Downloaded pack {} finished with state {}", (Object)id, (Object)state);
        }
    };
    final MinecraftClient client;
    private ResourcePackProvider packProvider = NOOP_PROVIDER;
    @Nullable
    private ReloadScheduler.ReloadContext reloadContext;
    final ServerResourcePackManager manager;
    private final Downloader downloader;
    private ResourcePackSource packSource = ResourcePackSource.SERVER;
    PackStateChangeCallback packStateChangeCallback = DEBUG_PACK_STATE_CHANGE_CALLBACK;
    private int packIndex;

    public ServerResourcePackLoader(MinecraftClient client, Path downloadsDirectory, RunArgs.Network runArgs) {
        this.client = client;
        try {
            this.downloader = new Downloader(downloadsDirectory);
        } catch (IOException iOException) {
            throw new UncheckedIOException("Failed to open download queue in directory " + String.valueOf(downloadsDirectory), iOException);
        }
        Executor executor = client::send;
        this.manager = new ServerResourcePackManager(this.createDownloadQueuer(this.downloader, executor, runArgs.session, runArgs.netProxy), new PackStateChangeCallback(){

            @Override
            public void onStateChanged(UUID id, PackStateChangeCallback.State state) {
                ServerResourcePackLoader.this.packStateChangeCallback.onStateChanged(id, state);
            }

            @Override
            public void onFinish(UUID id, PackStateChangeCallback.FinishState state) {
                ServerResourcePackLoader.this.packStateChangeCallback.onFinish(id, state);
            }
        }, this.getReloadScheduler(), this.createPackChangeCallback(executor), ServerResourcePackManager.AcceptanceStatus.PENDING);
    }

    NetworkUtils.DownloadListener createListener(final int entryCount) {
        return new NetworkUtils.DownloadListener(){
            private final SystemToast.Type toastType = new SystemToast.Type();
            private Text toastTitle = Text.empty();
            @Nullable
            private Text toastDescription = null;
            private int current;
            private int failureCount;
            private OptionalLong contentLength = OptionalLong.empty();

            private void showToast() {
                SystemToast.show(ServerResourcePackLoader.this.client.getToastManager(), this.toastType, this.toastTitle, this.toastDescription);
            }

            private void showProgress(long writtenBytes) {
                this.toastDescription = this.contentLength.isPresent() ? Text.translatable("download.pack.progress.percent", writtenBytes * 100L / this.contentLength.getAsLong()) : Text.translatable("download.pack.progress.bytes", SizeUnit.getUserFriendlyString(writtenBytes));
                this.showToast();
            }

            @Override
            public void onStart() {
                ++this.current;
                this.toastTitle = Text.translatable("download.pack.title", this.current, entryCount);
                this.showToast();
                LOGGER.debug("Starting pack {}/{} download", (Object)this.current, (Object)entryCount);
            }

            @Override
            public void onContentLength(OptionalLong contentLength) {
                LOGGER.debug("File size = {} bytes", (Object)contentLength);
                this.contentLength = contentLength;
                this.showProgress(0L);
            }

            @Override
            public void onProgress(long writtenBytes) {
                LOGGER.debug("Progress for pack {}: {} bytes", (Object)this.current, (Object)writtenBytes);
                this.showProgress(writtenBytes);
            }

            @Override
            public void onFinish(boolean success) {
                if (!success) {
                    LOGGER.info("Pack {} failed to download", (Object)this.current);
                    ++this.failureCount;
                } else {
                    LOGGER.debug("Download ended for pack {}", (Object)this.current);
                }
                if (this.current == entryCount) {
                    if (this.failureCount > 0) {
                        this.toastTitle = Text.translatable("download.pack.failed", this.failureCount, entryCount);
                        this.toastDescription = null;
                        this.showToast();
                    } else {
                        SystemToast.hide(ServerResourcePackLoader.this.client.getToastManager(), this.toastType);
                    }
                }
            }
        };
    }

    private DownloadQueuer createDownloadQueuer(final Downloader downloader, final Executor executor, final Session session, final Proxy proxy) {
        return new DownloadQueuer(){
            private static final int MAX_BYTES = 0xFA00000;
            private static final HashFunction SHA1 = Hashing.sha1();

            private Map<String, String> getHeaders() {
                GameVersion gameVersion = SharedConstants.getGameVersion();
                return Map.of("X-Minecraft-Username", session.getUsername(), "X-Minecraft-UUID", UndashedUuid.toString(session.getUuidOrNull()), "X-Minecraft-Version", gameVersion.getName(), "X-Minecraft-Version-ID", gameVersion.getId(), "X-Minecraft-Pack-Format", String.valueOf(gameVersion.getResourceVersion(ResourceType.CLIENT_RESOURCES)), "User-Agent", "Minecraft Java/" + gameVersion.getName());
            }

            @Override
            public void enqueue(Map<UUID, Downloader.DownloadEntry> entries, Consumer<Downloader.DownloadResult> callback) {
                downloader.downloadAsync(new Downloader.Config(SHA1, 0xFA00000, this.getHeaders(), proxy, ServerResourcePackLoader.this.createListener(entries.size())), entries).thenAcceptAsync((Consumer)callback, executor);
            }
        };
    }

    private Runnable createPackChangeCallback(final Executor executor) {
        return new Runnable(){
            private boolean currentlyRunning;
            private boolean shouldKeepRunning;

            @Override
            public void run() {
                this.shouldKeepRunning = true;
                if (!this.currentlyRunning) {
                    this.currentlyRunning = true;
                    executor.execute(this::runOnExecutor);
                }
            }

            private void runOnExecutor() {
                while (this.shouldKeepRunning) {
                    this.shouldKeepRunning = false;
                    ServerResourcePackLoader.this.manager.update();
                }
                this.currentlyRunning = false;
            }
        };
    }

    private ReloadScheduler getReloadScheduler() {
        return this::reload;
    }

    @Nullable
    private List<ResourcePackProfile> toProfiles(List<ReloadScheduler.PackInfo> packs) {
        ArrayList<ResourcePackProfile> list = new ArrayList<ResourcePackProfile>(packs.size());
        for (ReloadScheduler.PackInfo packInfo : Lists.reverse(packs)) {
            int i;
            ZipResourcePack.ZipBackedFactory packFactory;
            String string = String.format(Locale.ROOT, "server/%08X/%s", this.packIndex++, packInfo.id());
            Path path = packInfo.path();
            ResourcePackInfo resourcePackInfo = new ResourcePackInfo(string, SERVER_NAME_TEXT, this.packSource, Optional.empty());
            ResourcePackProfile.Metadata metadata = ResourcePackProfile.loadMetadata(resourcePackInfo, packFactory = new ZipResourcePack.ZipBackedFactory(path), i = SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES));
            if (metadata == null) {
                LOGGER.warn("Invalid pack metadata in {}, ignoring all", (Object)path);
                return null;
            }
            list.add(new ResourcePackProfile(resourcePackInfo, packFactory, metadata, POSITION));
        }
        return list;
    }

    public ResourcePackProvider getPassthroughPackProvider() {
        return packAdder -> this.packProvider.register(packAdder);
    }

    private static ResourcePackProvider getPackProvider(List<ResourcePackProfile> serverPacks) {
        if (serverPacks.isEmpty()) {
            return NOOP_PROVIDER;
        }
        return serverPacks::forEach;
    }

    private void reload(ReloadScheduler.ReloadContext context) {
        this.reloadContext = context;
        List<ReloadScheduler.PackInfo> list = context.getPacks();
        List<ResourcePackProfile> list2 = this.toProfiles(list);
        if (list2 == null) {
            context.onFailure(false);
            List<ReloadScheduler.PackInfo> list3 = context.getPacks();
            list2 = this.toProfiles(list3);
            if (list2 == null) {
                LOGGER.warn("Double failure in loading server packs");
                list2 = List.of();
            }
        }
        this.packProvider = ServerResourcePackLoader.getPackProvider(list2);
        this.client.reloadResources();
    }

    public void onReloadFailure() {
        if (this.reloadContext != null) {
            this.reloadContext.onFailure(false);
            List<ResourcePackProfile> list = this.toProfiles(this.reloadContext.getPacks());
            if (list == null) {
                LOGGER.warn("Double failure in loading server packs");
                list = List.of();
            }
            this.packProvider = ServerResourcePackLoader.getPackProvider(list);
        }
    }

    public void onForcedReloadFailure() {
        if (this.reloadContext != null) {
            this.reloadContext.onFailure(true);
            this.reloadContext = null;
            this.packProvider = NOOP_PROVIDER;
        }
    }

    public void onReloadSuccess() {
        if (this.reloadContext != null) {
            this.reloadContext.onSuccess();
            this.reloadContext = null;
        }
    }

    @Nullable
    private static HashCode toHashCode(@Nullable String hash) {
        if (hash != null && SHA1_PATTERN.matcher(hash).matches()) {
            return HashCode.fromString(hash.toLowerCase(Locale.ROOT));
        }
        return null;
    }

    public void addResourcePack(UUID id, URL url, @Nullable String hash) {
        HashCode hashCode = ServerResourcePackLoader.toHashCode(hash);
        this.manager.addResourcePack(id, url, hashCode);
    }

    public void addResourcePack(UUID id, Path path) {
        this.manager.addResourcePack(id, path);
    }

    public void remove(UUID id) {
        this.manager.remove(id);
    }

    public void removeAll() {
        this.manager.removeAll();
    }

    private static PackStateChangeCallback getStateChangeCallback(final ClientConnection connection) {
        return new PackStateChangeCallback(){

            @Override
            public void onStateChanged(UUID id, PackStateChangeCallback.State state) {
                LOGGER.debug("Pack {} changed status to {}", (Object)id, (Object)state);
                ResourcePackStatusC2SPacket.Status status = switch (state) {
                    default -> throw new MatchException(null, null);
                    case PackStateChangeCallback.State.ACCEPTED -> ResourcePackStatusC2SPacket.Status.ACCEPTED;
                    case PackStateChangeCallback.State.DOWNLOADED -> ResourcePackStatusC2SPacket.Status.DOWNLOADED;
                };
                connection.send(new ResourcePackStatusC2SPacket(id, status));
            }

            @Override
            public void onFinish(UUID id, PackStateChangeCallback.FinishState state) {
                LOGGER.debug("Pack {} changed status to {}", (Object)id, (Object)state);
                ResourcePackStatusC2SPacket.Status status = switch (state) {
                    default -> throw new MatchException(null, null);
                    case PackStateChangeCallback.FinishState.APPLIED -> ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED;
                    case PackStateChangeCallback.FinishState.DOWNLOAD_FAILED -> ResourcePackStatusC2SPacket.Status.FAILED_DOWNLOAD;
                    case PackStateChangeCallback.FinishState.DECLINED -> ResourcePackStatusC2SPacket.Status.DECLINED;
                    case PackStateChangeCallback.FinishState.DISCARDED -> ResourcePackStatusC2SPacket.Status.DISCARDED;
                    case PackStateChangeCallback.FinishState.ACTIVATION_FAILED -> ResourcePackStatusC2SPacket.Status.FAILED_RELOAD;
                };
                connection.send(new ResourcePackStatusC2SPacket(id, status));
            }
        };
    }

    public void init(ClientConnection connection, ServerResourcePackManager.AcceptanceStatus acceptanceStatus) {
        this.packSource = ResourcePackSource.SERVER;
        this.packStateChangeCallback = ServerResourcePackLoader.getStateChangeCallback(connection);
        switch (acceptanceStatus) {
            case ALLOWED: {
                this.manager.acceptAll();
                break;
            }
            case DECLINED: {
                this.manager.declineAll();
                break;
            }
            case PENDING: {
                this.manager.resetAcceptanceStatus();
            }
        }
    }

    public void initWorldPack() {
        this.packSource = ResourcePackSource.WORLD;
        this.packStateChangeCallback = DEBUG_PACK_STATE_CHANGE_CALLBACK;
        this.manager.acceptAll();
    }

    public void acceptAll() {
        this.manager.acceptAll();
    }

    public void declineAll() {
        this.manager.declineAll();
    }

    public CompletableFuture<Void> getPackLoadFuture(final UUID id) {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<Void>();
        final PackStateChangeCallback packStateChangeCallback = this.packStateChangeCallback;
        this.packStateChangeCallback = new PackStateChangeCallback(){

            @Override
            public void onStateChanged(UUID id2, PackStateChangeCallback.State state) {
                packStateChangeCallback.onStateChanged(id2, state);
            }

            @Override
            public void onFinish(UUID id2, PackStateChangeCallback.FinishState state) {
                if (id.equals(id2)) {
                    ServerResourcePackLoader.this.packStateChangeCallback = packStateChangeCallback;
                    if (state == PackStateChangeCallback.FinishState.APPLIED) {
                        completableFuture.complete(null);
                    } else {
                        completableFuture.completeExceptionally(new IllegalStateException("Failed to apply pack " + String.valueOf(id2) + ", reason: " + String.valueOf((Object)state)));
                    }
                }
                packStateChangeCallback.onFinish(id2, state);
            }
        };
        return completableFuture;
    }

    public void clear() {
        this.manager.removeAll();
        this.packStateChangeCallback = DEBUG_PACK_STATE_CHANGE_CALLBACK;
        this.manager.resetAcceptanceStatus();
    }

    @Override
    public void close() throws IOException {
        this.downloader.close();
    }
}

