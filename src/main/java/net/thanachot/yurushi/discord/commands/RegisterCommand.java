package net.thanachot.yurushi.discord.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.thanachot.yurushi.discord.ICommand;
import net.thanachot.yurushi.discord.ModalManager;

public class RegisterCommand implements ICommand {

    @Override
    public CommandData getData() {
        return Commands.slash("register", "Register for whitelist on our Minecraft server");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyModal(ModalManager.getRegisterModal()).queue();
    }
}
