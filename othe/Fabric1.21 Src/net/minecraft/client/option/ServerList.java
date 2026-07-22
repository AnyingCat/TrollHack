/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.option;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Util;
import net.minecraft.util.thread.TaskExecutor;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A list of {@link ServerInfo}. The list can contain an unlimited amount of
 * {@linkplain #servers server entries that are displayed on the multiplayer screen},
 * and up to {@value #MAX_HIDDEN_ENTRIES} {@linkplain #hiddenServers entries of servers}
 * that are created when using "Direct Connection" and is hidden from the screen.
 */
@Environment(value=EnvType.CLIENT)
public class ServerList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final TaskExecutor<Runnable> IO_EXECUTOR = TaskExecutor.create(Util.getMainWorkerExecutor(), "server-list-io");
    private static final int MAX_HIDDEN_ENTRIES = 16;
    private final MinecraftClient client;
    private final List<ServerInfo> servers = Lists.newArrayList();
    private final List<ServerInfo> hiddenServers = Lists.newArrayList();

    public ServerList(MinecraftClient client) {
        this.client = client;
    }

    public void loadFile() {
        try {
            this.servers.clear();
            this.hiddenServers.clear();
            NbtCompound nbtCompound = NbtIo.read(this.client.runDirectory.toPath().resolve("servers.dat"));
            if (nbtCompound == null) {
                return;
            }
            NbtList nbtList = nbtCompound.getList("servers", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < nbtList.size(); ++i) {
                NbtCompound nbtCompound2 = nbtList.getCompound(i);
                ServerInfo serverInfo = ServerInfo.fromNbt(nbtCompound2);
                if (nbtCompound2.getBoolean("hidden")) {
                    this.hiddenServers.add(serverInfo);
                    continue;
                }
                this.servers.add(serverInfo);
            }
        } catch (Exception exception) {
            LOGGER.error("Couldn't load server list", exception);
        }
    }

    public void saveFile() {
        try {
            NbtCompound nbtCompound;
            NbtList nbtList = new NbtList();
            for (ServerInfo serverInfo : this.servers) {
                nbtCompound = serverInfo.toNbt();
                nbtCompound.putBoolean("hidden", false);
                nbtList.add(nbtCompound);
            }
            for (ServerInfo serverInfo : this.hiddenServers) {
                nbtCompound = serverInfo.toNbt();
                nbtCompound.putBoolean("hidden", true);
                nbtList.add(nbtCompound);
            }
            NbtCompound nbtCompound2 = new NbtCompound();
            nbtCompound2.put("servers", nbtList);
            Path path = this.client.runDirectory.toPath();
            Path path2 = Files.createTempFile(path, "servers", ".dat", new FileAttribute[0]);
            NbtIo.write(nbtCompound2, path2);
            Path path3 = path.resolve("servers.dat_old");
            Path path4 = path.resolve("servers.dat");
            Util.backupAndReplace(path4, path2, path3);
        } catch (Exception exception) {
            LOGGER.error("Couldn't save server list", exception);
        }
    }

    public ServerInfo get(int index) {
        return this.servers.get(index);
    }

    /**
     * {@return the server info for {@code address}, or {@code null} if there is no such one}
     */
    @Nullable
    public ServerInfo get(String address) {
        for (ServerInfo serverInfo : this.servers) {
            if (!serverInfo.address.equals(address)) continue;
            return serverInfo;
        }
        for (ServerInfo serverInfo : this.hiddenServers) {
            if (!serverInfo.address.equals(address)) continue;
            return serverInfo;
        }
        return null;
    }

    /**
     * {@return the previously hidden server info for the address {@code address}, or
     * {@code null} if there is no such info}
     * 
     * <p>This "unhides" the server info and is used when adding the entry to the
     * multiplayer screen to unhide any existing server info created when connecting
     * directly.
     */
    @Nullable
    public ServerInfo tryUnhide(String address) {
        for (int i = 0; i < this.hiddenServers.size(); ++i) {
            ServerInfo serverInfo = this.hiddenServers.get(i);
            if (!serverInfo.address.equals(address)) continue;
            this.hiddenServers.remove(i);
            this.servers.add(serverInfo);
            return serverInfo;
        }
        return null;
    }

    public void remove(ServerInfo serverInfo) {
        if (!this.servers.remove(serverInfo)) {
            this.hiddenServers.remove(serverInfo);
        }
    }

    /**
     * Adds a server info to this list.
     * 
     * @param hidden whether the info should not be listed in the multiplayer screen (also called
     * "direct connection")
     */
    public void add(ServerInfo serverInfo, boolean hidden) {
        if (hidden) {
            this.hiddenServers.add(0, serverInfo);
            while (this.hiddenServers.size() > 16) {
                this.hiddenServers.remove(this.hiddenServers.size() - 1);
            }
        } else {
            this.servers.add(serverInfo);
        }
    }

    public int size() {
        return this.servers.size();
    }

    public void swapEntries(int index1, int index2) {
        ServerInfo serverInfo = this.get(index1);
        this.servers.set(index1, this.get(index2));
        this.servers.set(index2, serverInfo);
        this.saveFile();
    }

    public void set(int index, ServerInfo serverInfo) {
        this.servers.set(index, serverInfo);
    }

    /**
     * Replaces the server info in {@code serverInfos} whose name and address match
     * {@code serverInfo}'s with {@code serverInfo}.
     */
    private static boolean replace(ServerInfo serverInfo, List<ServerInfo> serverInfos) {
        for (int i = 0; i < serverInfos.size(); ++i) {
            ServerInfo serverInfo2 = serverInfos.get(i);
            if (!serverInfo2.name.equals(serverInfo.name) || !serverInfo2.address.equals(serverInfo.address)) continue;
            serverInfos.set(i, serverInfo);
            return true;
        }
        return false;
    }

    public static void updateServerListEntry(ServerInfo serverInfo) {
        IO_EXECUTOR.send(() -> {
            ServerList serverList = new ServerList(MinecraftClient.getInstance());
            serverList.loadFile();
            if (!ServerList.replace(serverInfo, serverList.servers)) {
                ServerList.replace(serverInfo, serverList.hiddenServers);
            }
            serverList.saveFile();
        });
    }
}

