package net.thanachot.yurushi.discord;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICommand {

    CommandData getData();

    void execute(SlashCommandInteractionEvent event);
}
