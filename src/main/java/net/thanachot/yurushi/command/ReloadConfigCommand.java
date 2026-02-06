package net.thanachot.yurushi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.config.ModConfig;

public class ReloadConfigCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("yurushi")
                        .then(CommandManager.literal("reload")
                                .requires(Permissions.require("yurushi.command.reload", PermissionLevel.ADMINS))
                                .executes(ReloadConfigCommand::execute)));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        try {
            ModConfig.load();

            var errors = ModConfig.validate();
            if (!errors.isEmpty()) {
                for (String error : errors) {
                    context.getSource().sendError(Text.literal("[Yurushi] Config Error: " + error));
                }
                return 0;
            }

            context.getSource().sendFeedback(
                    () -> Text.literal("§a[Yurushi] Configuration reloaded successfully!"),
                    true);
            Yurushi.LOGGER.info("Configuration reloaded by {}", context.getSource().getName());
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("§c[Yurushi] Failed to reload config: " + e.getMessage()));
            Yurushi.LOGGER.error("Failed to reload configuration", e);
            return 0;
        }
    }
}
