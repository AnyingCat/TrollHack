/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.task;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.realms.RealmsConnection;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerAddress;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class RealmsConnectTask
extends LongRunningTask {
    private static final Text TITLE = Text.translatable("mco.connect.connecting");
    private final RealmsConnection realmsConnection;
    private final RealmsServer server;
    private final RealmsServerAddress address;

    public RealmsConnectTask(Screen lastScreen, RealmsServer server, RealmsServerAddress address) {
        this.server = server;
        this.address = address;
        this.realmsConnection = new RealmsConnection(lastScreen);
    }

    @Override
    public void run() {
        this.realmsConnection.connect(this.server, ServerAddress.parse(this.address.address));
    }

    @Override
    public void abortTask() {
        super.abortTask();
        this.realmsConnection.abort();
        MinecraftClient.getInstance().getServerResourcePackProvider().clear();
    }

    @Override
    public void tick() {
        this.realmsConnection.tick();
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }
}

