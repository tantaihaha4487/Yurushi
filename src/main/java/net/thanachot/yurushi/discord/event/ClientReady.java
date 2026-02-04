package net.thanachot.yurushi.discord.event;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.thanachot.yurushi.Yurushi;

public class ClientReady extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {
        Yurushi.LOGGER.info("Bot online as {}", event.getJDA().getSelfUser().getAsTag());
    }
}
