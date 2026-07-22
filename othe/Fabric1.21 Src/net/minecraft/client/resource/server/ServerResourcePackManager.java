/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.resource.server;

import com.google.common.hash.HashCode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.server.DownloadQueuer;
import net.minecraft.client.resource.server.PackStateChangeCallback;
import net.minecraft.client.resource.server.ReloadScheduler;
import net.minecraft.util.Downloader;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ServerResourcePackManager {
    private final DownloadQueuer queuer;
    final PackStateChangeCallback stateChangeCallback;
    private final ReloadScheduler reloadScheduler;
    private final Runnable packChangeCallback;
    private AcceptanceStatus acceptanceStatus;
    final List<PackEntry> packs = new ArrayList<PackEntry>();

    public ServerResourcePackManager(DownloadQueuer queuer, PackStateChangeCallback stateChangeCallback, ReloadScheduler reloadScheduler, Runnable packChangeCallback, AcceptanceStatus acceptanceStatus) {
        this.queuer = queuer;
        this.stateChangeCallback = stateChangeCallback;
        this.reloadScheduler = reloadScheduler;
        this.packChangeCallback = packChangeCallback;
        this.acceptanceStatus = acceptanceStatus;
    }

    void onPackChanged() {
        this.packChangeCallback.run();
    }

    private void markReplaced(UUID id) {
        for (PackEntry packEntry : this.packs) {
            if (!packEntry.id.equals(id)) continue;
            packEntry.discard(DiscardReason.SERVER_REPLACED);
        }
    }

    public void addResourcePack(UUID id, URL url, @Nullable HashCode hashCode) {
        if (this.acceptanceStatus == AcceptanceStatus.DECLINED) {
            this.stateChangeCallback.onFinish(id, PackStateChangeCallback.FinishState.DECLINED);
            return;
        }
        this.onAdd(id, new PackEntry(id, url, hashCode));
    }

    public void addResourcePack(UUID id, Path path) {
        URL uRL;
        if (this.acceptanceStatus == AcceptanceStatus.DECLINED) {
            this.stateChangeCallback.onFinish(id, PackStateChangeCallback.FinishState.DECLINED);
            return;
        }
        try {
            uRL = path.toUri().toURL();
        } catch (MalformedURLException malformedURLException) {
            throw new IllegalStateException("Can't convert path to URL " + String.valueOf(path), malformedURLException);
        }
        PackEntry packEntry = new PackEntry(id, uRL, null);
        packEntry.loadStatus = LoadStatus.DONE;
        packEntry.path = path;
        this.onAdd(id, packEntry);
    }

    private void onAdd(UUID id, PackEntry pack) {
        this.markReplaced(id);
        this.packs.add(pack);
        if (this.acceptanceStatus == AcceptanceStatus.ALLOWED) {
            this.accept(pack);
        }
        this.onPackChanged();
    }

    private void accept(PackEntry pack) {
        this.stateChangeCallback.onStateChanged(pack.id, PackStateChangeCallback.State.ACCEPTED);
        pack.accepted = true;
    }

    @Nullable
    private PackEntry get(UUID id) {
        for (PackEntry packEntry : this.packs) {
            if (packEntry.isDiscarded() || !packEntry.id.equals(id)) continue;
            return packEntry;
        }
        return null;
    }

    public void remove(UUID id) {
        PackEntry packEntry = this.get(id);
        if (packEntry != null) {
            packEntry.discard(DiscardReason.SERVER_REMOVED);
            this.onPackChanged();
        }
    }

    public void removeAll() {
        for (PackEntry packEntry : this.packs) {
            packEntry.discard(DiscardReason.SERVER_REMOVED);
        }
        this.onPackChanged();
    }

    public void acceptAll() {
        this.acceptanceStatus = AcceptanceStatus.ALLOWED;
        for (PackEntry packEntry : this.packs) {
            if (packEntry.accepted || packEntry.isDiscarded()) continue;
            this.accept(packEntry);
        }
        this.onPackChanged();
    }

    public void declineAll() {
        this.acceptanceStatus = AcceptanceStatus.DECLINED;
        for (PackEntry packEntry : this.packs) {
            if (packEntry.accepted) continue;
            packEntry.discard(DiscardReason.DECLINED);
        }
        this.onPackChanged();
    }

    public void resetAcceptanceStatus() {
        this.acceptanceStatus = AcceptanceStatus.PENDING;
    }

    public void update() {
        boolean bl = this.enqueueDownloads();
        if (!bl) {
            this.applyDownloadedPacks();
        }
        this.removeInactivePacks();
    }

    private void removeInactivePacks() {
        this.packs.removeIf(pack -> {
            if (pack.status != Status.INACTIVE) {
                return false;
            }
            if (pack.discardReason != null) {
                PackStateChangeCallback.FinishState finishState = pack.discardReason.state;
                if (finishState != null) {
                    this.stateChangeCallback.onFinish(pack.id, finishState);
                }
                return true;
            }
            return false;
        });
    }

    private void onDownload(Collection<PackEntry> packs, Downloader.DownloadResult result) {
        if (!result.failed().isEmpty()) {
            for (PackEntry packEntry : this.packs) {
                if (packEntry.status == Status.ACTIVE) continue;
                if (result.failed().contains(packEntry.id)) {
                    packEntry.discard(DiscardReason.DOWNLOAD_FAILED);
                    continue;
                }
                packEntry.discard(DiscardReason.DISCARDED);
            }
        }
        for (PackEntry packEntry : packs) {
            Path path = result.downloaded().get(packEntry.id);
            if (path == null) continue;
            packEntry.loadStatus = LoadStatus.DONE;
            packEntry.path = path;
            if (packEntry.isDiscarded()) continue;
            this.stateChangeCallback.onStateChanged(packEntry.id, PackStateChangeCallback.State.DOWNLOADED);
        }
        this.onPackChanged();
    }

    private boolean enqueueDownloads() {
        ArrayList<PackEntry> list = new ArrayList<PackEntry>();
        boolean bl = false;
        for (PackEntry packEntry : this.packs) {
            if (packEntry.isDiscarded() || !packEntry.accepted) continue;
            if (packEntry.loadStatus != LoadStatus.DONE) {
                bl = true;
            }
            if (packEntry.loadStatus != LoadStatus.REQUESTED) continue;
            packEntry.loadStatus = LoadStatus.PENDING;
            list.add(packEntry);
        }
        if (!list.isEmpty()) {
            HashMap<UUID, Downloader.DownloadEntry> map = new HashMap<UUID, Downloader.DownloadEntry>();
            for (PackEntry packEntry2 : list) {
                map.put(packEntry2.id, new Downloader.DownloadEntry(packEntry2.url, packEntry2.hashCode));
            }
            this.queuer.enqueue(map, result -> this.onDownload((Collection<PackEntry>)list, (Downloader.DownloadResult)result));
        }
        return bl;
    }

    private void applyDownloadedPacks() {
        boolean bl = false;
        final ArrayList<PackEntry> list = new ArrayList<PackEntry>();
        final ArrayList<PackEntry> list2 = new ArrayList<PackEntry>();
        for (PackEntry packEntry : this.packs) {
            boolean bl2;
            if (packEntry.status == Status.PENDING) {
                return;
            }
            boolean bl3 = bl2 = packEntry.accepted && packEntry.loadStatus == LoadStatus.DONE && !packEntry.isDiscarded();
            if (bl2 && packEntry.status == Status.INACTIVE) {
                list.add(packEntry);
                bl = true;
            }
            if (packEntry.status != Status.ACTIVE) continue;
            if (!bl2) {
                bl = true;
                list2.add(packEntry);
                continue;
            }
            list.add(packEntry);
        }
        if (bl) {
            for (PackEntry packEntry : list) {
                if (packEntry.status == Status.ACTIVE) continue;
                packEntry.status = Status.PENDING;
            }
            for (PackEntry packEntry : list2) {
                packEntry.status = Status.PENDING;
            }
            this.reloadScheduler.scheduleReload(new ReloadScheduler.ReloadContext(){

                @Override
                public void onSuccess() {
                    for (PackEntry packEntry : list) {
                        packEntry.status = Status.ACTIVE;
                        if (packEntry.discardReason != null) continue;
                        ServerResourcePackManager.this.stateChangeCallback.onFinish(packEntry.id, PackStateChangeCallback.FinishState.APPLIED);
                    }
                    for (PackEntry packEntry : list2) {
                        packEntry.status = Status.INACTIVE;
                    }
                    ServerResourcePackManager.this.onPackChanged();
                }

                @Override
                public void onFailure(boolean force) {
                    if (!force) {
                        list.clear();
                        for (PackEntry packEntry : ServerResourcePackManager.this.packs) {
                            switch (packEntry.status.ordinal()) {
                                case 2: {
                                    list.add(packEntry);
                                    break;
                                }
                                case 1: {
                                    packEntry.status = Status.INACTIVE;
                                    packEntry.discard(DiscardReason.ACTIVATION_FAILED);
                                    break;
                                }
                                case 0: {
                                    packEntry.discard(DiscardReason.DISCARDED);
                                }
                            }
                        }
                        ServerResourcePackManager.this.onPackChanged();
                    } else {
                        for (PackEntry packEntry : ServerResourcePackManager.this.packs) {
                            if (packEntry.status != Status.PENDING) continue;
                            packEntry.status = Status.INACTIVE;
                        }
                    }
                }

                @Override
                public List<ReloadScheduler.PackInfo> getPacks() {
                    return list.stream().map(pack -> new ReloadScheduler.PackInfo(pack.id, pack.path)).toList();
                }
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum AcceptanceStatus {
        PENDING,
        ALLOWED,
        DECLINED;

    }

    @Environment(value=EnvType.CLIENT)
    static class PackEntry {
        final UUID id;
        final URL url;
        @Nullable
        final HashCode hashCode;
        @Nullable
        Path path;
        @Nullable
        DiscardReason discardReason;
        LoadStatus loadStatus = LoadStatus.REQUESTED;
        Status status = Status.INACTIVE;
        boolean accepted;

        PackEntry(UUID id, URL url, @Nullable HashCode hashCode) {
            this.id = id;
            this.url = url;
            this.hashCode = hashCode;
        }

        public void discard(DiscardReason reason) {
            if (this.discardReason == null) {
                this.discardReason = reason;
            }
        }

        public boolean isDiscarded() {
            return this.discardReason != null;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum DiscardReason {
        DOWNLOAD_FAILED(PackStateChangeCallback.FinishState.DOWNLOAD_FAILED),
        ACTIVATION_FAILED(PackStateChangeCallback.FinishState.ACTIVATION_FAILED),
        DECLINED(PackStateChangeCallback.FinishState.DECLINED),
        DISCARDED(PackStateChangeCallback.FinishState.DISCARDED),
        SERVER_REMOVED(null),
        SERVER_REPLACED(null);

        @Nullable
        final PackStateChangeCallback.FinishState state;

        private DiscardReason(PackStateChangeCallback.FinishState state) {
            this.state = state;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum LoadStatus {
        REQUESTED,
        PENDING,
        DONE;

    }

    @Environment(value=EnvType.CLIENT)
    static enum Status {
        INACTIVE,
        PENDING,
        ACTIVE;

    }
}

