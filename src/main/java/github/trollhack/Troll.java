package github.trollhack;

import github.trollhack.core.Managers;
import github.trollhack.events.EventBusHolder;
import github.trollhack.utils.render.Render2DUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Troll implements ModInitializer {
    public static final String MOD_ID = "Troll";
    public static final String MOD_VERSION = "1.0.0";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Troll Client...");
        Render2DUtil.initShaders();
        EventBusHolder.init();
        Managers.init();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Managers.onShutdown());
        LOGGER.info("Troll Client initialized successfully!");
    }
}
