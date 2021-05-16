package com.qtd.bot;

import org.javacord.api.event.message.MessageCreateEvent;

public class Ping implements Command{

    final String COMMAND = "ping";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public void sendEventMessage(MessageCreateEvent event, String option) {
        event.getChannel().sendMessage("Pong!");
    }
}
