package net.thanachot.yurushi.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.thanachot.yurushi.discord.ICommand;

public class PingCommand implements ICommand {
    @Override
    public CommandData getData() {
        return Commands.slash("ping", "Replies with Pong!");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply("Pong!").queue();
    }
}
