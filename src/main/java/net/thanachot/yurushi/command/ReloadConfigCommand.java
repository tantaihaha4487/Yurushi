package net.thanachot.yurushi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.config.ModConfig;

public class ReloadConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("yurushi")
                        .then(Commands.literal("reload")
                                .requires(Permissions.require("yurushi.command.reload", PermissionLevel.ADMINS))
                                .executes(ReloadConfigCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            ModConfig.load();

            var errors = ModConfig.validate();
            if (!errors.isEmpty()) {
                for (String error : errors) {
                    context.getSource().sendFailure(Component.literal("[Yurushi] Config Error: " + error));
                }
                return 0;
            }

            context.getSource().sendSuccess(
                    () -> Component.literal("§a[Yurushi] Configuration reloaded successfully!"),
                    true);
            Yurushi.LOGGER.info("Configuration reloaded by {}", context.getSource().getTextName());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§c[Yurushi] Failed to reload config: " + e.getMessage()));
            Yurushi.LOGGER.error("Failed to reload configuration", e);
            return 0;
        }
    }
}
