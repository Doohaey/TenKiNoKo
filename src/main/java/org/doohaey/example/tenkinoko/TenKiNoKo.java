package org.doohaey.example.tenkinoko;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.doohaey.example.tenkinoko.commands.CommandRegister;
import org.doohaey.example.tenkinoko.util.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenKiNoKo implements ModInitializer {
    public static final String MOD_ID = "TenKiNoKo";
    public static final Logger TK_LOGGER = LoggerFactory.getLogger("TenKiNoKo");
    public static final ModConfig TK_CONFIG = new ModConfig();

    @Override
    public void onInitialize() {
        TK_CONFIG.loadAndSave();
        CommandRegistrationCallback.EVENT.register(CommandRegister::register);
        ServerTickEvents.START_SERVER_TICK.register(CommandRegister::serverTick);

        TK_LOGGER.info("TenKiNoKo initialized.");
    }
}
