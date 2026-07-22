/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.task;

import com.mojang.logging.LogUtils;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerAddress;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.exception.RetryCallException;
import net.minecraft.client.realms.gui.RealmsPopups;
import net.minecraft.client.realms.gui.screen.RealmsBrokenWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningTickableTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsTermsScreen;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.client.realms.task.RealmsConnectTask;
import net.minecraft.client.resource.server.ServerResourcePackLoader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsPrepareConnectionTask
extends LongRunningTask {
    private static final Text APPLYING_PACK_TEXT = Text.translatable("multiplayer.applyingPack");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text TITLE = Text.translatable("mco.connect.connecting");
    private final RealmsServer server;
    private final Screen lastScreen;

    public RealmsPrepareConnectionTask(Screen lastScreen, RealmsServer server) {
        this.lastScreen = lastScreen;
        this.server = server;
    }

    @Override
    public void run() {
        RealmsServerAddress realmsServerAddress;
        try {
            realmsServerAddress = this.join();
        } catch (CancellationException cancellationException) {
            LOGGER.info("User aborted connecting to realms");
            return;
        } catch (RealmsServiceException realmsServiceException) {
            switch (realmsServiceException.error.getErrorCode()) {
                case 6002: {
                    RealmsPrepareConnectionTask.setScreen(new RealmsTermsScreen(this.lastScreen, this.server));
                    return;
                }
                case 6006: {
                    boolean bl = MinecraftClient.getInstance().uuidEquals(this.server.ownerUUID);
                    RealmsPrepareConnectionTask.setScreen(bl ? new RealmsBrokenWorldScreen(this.lastScreen, this.server.id, this.server.isMinigame()) : new RealmsGenericErrorScreen(Text.translatable("mco.brokenworld.nonowner.title"), Text.translatable("mco.brokenworld.nonowner.error"), this.lastScreen));
                    return;
                }
            }
            this.error(realmsServiceException);
            LOGGER.error("Couldn't connect to world", realmsServiceException);
            return;
        } catch (TimeoutException timeoutException) {
            this.error(Text.translatable("mco.errorMessage.connectionFailure"));
            return;
        } catch (Exception exception) {
            LOGGER.error("Couldn't connect to world", exception);
            this.error(exception);
            return;
        }
        boolean bl2 = realmsServerAddress.resourcePackUrl != null && realmsServerAddress.resourcePackHash != null;
        RealmsLongRunningMcoTaskScreen screen = bl2 ? this.createResourcePackConfirmationScreen(realmsServerAddress, RealmsPrepareConnectionTask.getResourcePackId(this.server), this::createConnectingScreen) : this.createConnectingScreen(realmsServerAddress);
        RealmsPrepareConnectionTask.setScreen(screen);
    }

    private static UUID getResourcePackId(RealmsServer server) {
        if (server.minigameName != null) {
            return UUID.nameUUIDFromBytes(("minigame:" + server.minigameName).getBytes(StandardCharsets.UTF_8));
        }
        return UUID.nameUUIDFromBytes(("realms:" + server.name + ":" + server.activeSlot).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }

    private RealmsServerAddress join() throws RealmsServiceException, TimeoutException, CancellationException {
        RealmsClient realmsClient = RealmsClient.create();
        for (int i = 0; i < 40; ++i) {
            if (this.aborted()) {
                throw new CancellationException();
            }
            try {
                return realmsClient.join(this.server.id);
            } catch (RetryCallException retryCallException) {
                RealmsPrepareConnectionTask.pause(retryCallException.delaySeconds);
                continue;
            }
        }
        throw new TimeoutException();
    }

    public RealmsLongRunningMcoTaskScreen createConnectingScreen(RealmsServerAddress address) {
        return new RealmsLongRunningTickableTaskScreen(this.lastScreen, (LongRunningTask)new RealmsConnectTask(this.lastScreen, this.server, address));
    }

    private PopupScreen createResourcePackConfirmationScreen(RealmsServerAddress address, UUID id, Function<RealmsServerAddress, Screen> connectingScreenCreator) {
        MutableText text = Text.translatable("mco.configure.world.resourcepack.question");
        return RealmsPopups.createInfoPopup(this.lastScreen, text, popup -> {
            RealmsPrepareConnectionTask.setScreen(new MessageScreen(APPLYING_PACK_TEXT));
            ((CompletableFuture)this.downloadResourcePack(address, id).thenRun(() -> RealmsPrepareConnectionTask.setScreen((Screen)connectingScreenCreator.apply(address)))).exceptionally(throwable -> {
                MinecraftClient.getInstance().getServerResourcePackProvider().clear();
                LOGGER.error("Failed to download resource pack from {}", (Object)address, throwable);
                RealmsPrepareConnectionTask.setScreen(new RealmsGenericErrorScreen(Text.translatable("mco.download.resourcePack.fail"), this.lastScreen));
                return null;
            });
        });
    }

    private CompletableFuture<?> downloadResourcePack(RealmsServerAddress address, UUID id) {
        try {
            ServerResourcePackLoader serverResourcePackLoader = MinecraftClient.getInstance().getServerResourcePackProvider();
            CompletableFuture<Void> completableFuture = serverResourcePackLoader.getPackLoadFuture(id);
            serverResourcePackLoader.acceptAll();
            serverResourcePackLoader.addResourcePack(id, new URL(address.resourcePackUrl), address.resourcePackHash);
            return completableFuture;
        } catch (Exception exception) {
            CompletableFuture completableFuture = new CompletableFuture();
            completableFuture.completeExceptionally(exception);
            return completableFuture;
        }
    }
}

