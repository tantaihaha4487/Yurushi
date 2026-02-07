package net.thanachot.yurushi.discord.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.thanachot.yurushi.config.MessageConfig;
import net.thanachot.yurushi.config.ModConfig;
import net.thanachot.yurushi.discord.ICommand;
import net.thanachot.yurushi.util.MinotarUtil;

import java.awt.*;
import java.time.Instant;

public class DebugCommand implements ICommand {

    @Override
    public CommandData getData() {
        return Commands.slash("debug", "Yurushi debug commands")
                .addSubcommands(new SubcommandData("embeds", "Preview whitelist configuration embeds"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!ModConfig.hasWhitelistPermission(event.getMember())) {
            event.reply("❌ You do not have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        if ("embeds".equals(event.getSubcommandName())) {
            showEmbeds(event);
        } else {
            event.reply("Unknown subcommand").setEphemeral(true).queue();
        }
    }

    private void showEmbeds(SlashCommandInteractionEvent event) {
        String username = "Steve";
        String userMention = event.getUser().getAsMention();
        String adminName = event.getUser().getName();

        // Request Embed
        EmbedBuilder requestEmbed = new EmbedBuilder()
                .setTitle(MessageConfig.get("embed.request.title"))
                .setColor(new Color(233, 136, 255))
                .setThumbnail(event.getUser().getEffectiveAvatarUrl())
                .addField(MessageConfig.get("embed.request.fields.discord_user"), userMention, true)
                .addField(MessageConfig.get("embed.request.fields.minecraft_username"), "`" + username + "`", true)
                .addField(MessageConfig.get("embed.request.fields.description"), "``Example description``", false)
                .setFooter(MessageConfig.get("embed.request.footer", "user_id", event.getUser().getId()))
                .setTimestamp(Instant.now());

        // Approved Embed (Channel)
        EmbedBuilder approvedEmbed = new EmbedBuilder()
                .setTitle(MessageConfig.get("embed.approved.title"))
                .setColor(new Color(87, 242, 135))
                .setThumbnail(MinotarUtil.getAvatarUrl(username))
                .addField(MessageConfig.get("embed.request.fields.minecraft_username"), "`" + username + "`", true)
                .addField(MessageConfig.get("embed.approved.fields.approved_by"), adminName, false)
                .setFooter(MessageConfig.get("embed.approved.footer"))
                .setTimestamp(Instant.now());

        // Denied Embed (Channel)
        EmbedBuilder deniedEmbed = new EmbedBuilder()
                .setTitle(MessageConfig.get("embed.denied.title"))
                .setColor(new Color(237, 66, 69))
                .setThumbnail(MinotarUtil.getAvatarUrl(username))
                .addField(MessageConfig.get("embed.request.fields.minecraft_username"), "`" + username + "`", false)
                .addField(MessageConfig.get("embed.denied.fields.reason"), "Example reason", false)
                .addField(MessageConfig.get("embed.denied.fields.denied_by"), adminName, false)
                .setFooter(MessageConfig.get("embed.denied.footer"))
                .setTimestamp(Instant.now());

        // Already Whitelisted Embed (Channel)
        // Constructs by copying Request Embed, then updating fields
        EmbedBuilder alreadyWhitelistedEmbed = new EmbedBuilder(requestEmbed.build())
                .setTitle(MessageConfig.get("embed.already_whitelisted.title"))
                .setColor(Color.YELLOW)
                .setFooter(MessageConfig.get("embed.already_whitelisted.footer"))
                .setTimestamp(Instant.now());

        // Approved DM Embed
        EmbedBuilder approvedDMEmbed = new EmbedBuilder()
                .setTitle(MessageConfig.get("dm.approved.title"))
                .setColor(new Color(87, 242, 135))
                .setDescription(MessageConfig.get("dm.approved.description"))
                .addField(MessageConfig.get("embed.request.fields.minecraft_username"), "`" + username + "`", false)
                .setFooter(MessageConfig.get("embed.approved.footer"))
                .setTimestamp(Instant.now());

        // Denied DM Embed
        EmbedBuilder deniedDMEmbed = new EmbedBuilder()
                .setTitle(MessageConfig.get("dm.denied.title"))
                .setColor(new Color(237, 66, 69))
                .setDescription(MessageConfig.get("dm.denied.description"))
                .addField(MessageConfig.get("embed.request.fields.minecraft_username"), "`" + username + "`", false)
                .addField(MessageConfig.get("embed.denied.fields.reason"), "Example reason", false)
                .setFooter(MessageConfig.get("embed.denied.footer"))
                .setTimestamp(Instant.now());

        // Success Reply Embed
        EmbedBuilder successReplyEmbed = new EmbedBuilder()
                .setTitle(MessageConfig.get("modal.register.success"))
                .setColor(new Color(233, 136, 255))
                .addField(MessageConfig.get("embed.request.fields.discord_user"), userMention, true)
                .addField(MessageConfig.get("embed.request.fields.minecraft_username"), "`" + username + "`", true)
                .setTimestamp(Instant.now());

        event.reply("## Embed Previews\n" +
                        "1. **Request Embed** (Sent to Admin Channel)\n" +
                        "2. **Approved Embed** (Updated Request Embed)\n" +
                        "3. **Denied Embed** (Updated Request Embed)\n" +
                        "4. **Already Whitelisted** (Updated Request Embed)\n" +
                        "5. **Approved DM** (Sent to User)\n" +
                        "6. **Denied DM** (Sent to User)\n" +
                        "7. **Success Reply** (Ephemeral to User)")
                .addEmbeds(requestEmbed.build(), approvedEmbed.build(), deniedEmbed.build(),
                        alreadyWhitelistedEmbed.build(), approvedDMEmbed.build(), deniedDMEmbed.build(),
                        successReplyEmbed.build())
                .setEphemeral(true)
                .queue();
    }
}
