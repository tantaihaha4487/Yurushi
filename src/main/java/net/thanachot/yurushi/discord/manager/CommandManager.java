package net.thanachot.yurushi.discord.manager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thanachot.yurushi.Yurushi;
import net.thanachot.yurushi.discord.ICommand;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;

public class CommandManager extends ListenerAdapter {

    private final Map<String, ICommand> commands = new HashMap<>();

    public CommandManager() {
        loadCommands();
    }

    private void loadCommands() {
        var reflections = new Reflections("net.thanachot.yurushi.discord.commands");

        for (var clazz : reflections.getSubTypesOf(ICommand.class)) {
            try {
                var command = clazz.getDeclaredConstructor().newInstance();
                commands.put(command.getData().getName(), command);
            } catch (Exception e) {
                Yurushi.LOGGER.error("Failed to load command: {}", clazz.getSimpleName(), e);
            }
        }
    }

    public void registerCommands(JDA jda) {
        var commandData = commands.values().stream()
                .map(ICommand::getData)
                .toList();

        jda.updateCommands().addCommands(commandData).queue();
        Yurushi.LOGGER.info("Registered {} command(s)", commandData.size());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        var command = commands.get(event.getName());
        if (command != null) {
            command.execute(event);
        }
    }
}
