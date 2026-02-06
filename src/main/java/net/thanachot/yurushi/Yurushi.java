package net.thanachot.yurushi;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.thanachot.yurushi.command.ReloadConfigCommand;
import net.thanachot.yurushi.config.ModConfig;
import net.thanachot.yurushi.discord.event.ButtonListener;
import net.thanachot.yurushi.discord.event.ClientReady;
import net.thanachot.yurushi.discord.event.ModalListener;
import net.thanachot.yurushi.discord.manager.CommandManager;
import net.thanachot.yurushi.manager.ServerAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Yurushi implements ModInitializer {

    public static final String MOD_ID = "yurushi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static JDA jda;

    public static JDA getJda() {
        return jda;
    }

    @Override
    public void onInitialize() {

        ModConfig.load();

        CommandRegistrationCallback.EVENT
                .register((dispatcher, registryAccess, environment) -> ReloadConfigCommand.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerAccessor.setServer(server);
            LOGGER.info("Yurushi's WhitelistManager initialized");
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ServerAccessor.setServer(null);
            LOGGER.info("Yurushi's WhitelistManager cleared");
        });

        if (ModConfig.hasErrors()) {
            for (String error : ModConfig.validate()) {
                LOGGER.error("[Config Error] {}", error);
            }
            LOGGER.error("Bot will not start due to configuration errors");
            return;
        }

        try {
            CommandManager commandManager = new CommandManager();

            jda = JDABuilder.createDefault(ModConfig.botToken)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(
                            new ClientReady(),
                            commandManager,
                            new ButtonListener(),
                            new ModalListener())
                    .build();

            jda.awaitReady();

            var jdaErrors = ModConfig.validateWithJda(jda);
            if (!jdaErrors.isEmpty()) {
                for (String error : jdaErrors) {
                    LOGGER.warn("[Config Warning] {}", error);
                }
            }

            commandManager.registerCommands(jda);

            LOGGER.info("Yurushi initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Discord bot", e);
        }
    }
}
