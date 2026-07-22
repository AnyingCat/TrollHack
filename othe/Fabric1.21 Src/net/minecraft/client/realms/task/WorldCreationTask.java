/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.client.realms.task;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.task.LongRunningTask;
import net.minecraft.text.Text;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class WorldCreationTask
extends LongRunningTask {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Text TITLE = Text.translatable("mco.create.world.wait");
    private final String name;
    private final String motd;
    private final long worldId;

    public WorldCreationTask(long worldId, String name, String motd) {
        this.worldId = worldId;
        this.name = name;
        this.motd = motd;
    }

    @Override
    public void run() {
        RealmsClient realmsClient = RealmsClient.create();
        try {
            realmsClient.initializeWorld(this.worldId, this.name, this.motd);
        } catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't create world", realmsServiceException);
            this.error(realmsServiceException);
        } catch (Exception exception) {
            LOGGER.error("Could not create world", exception);
            this.error(exception);
        }
    }

    @Override
    public Text getTitle() {
        return TITLE;
    }
}

