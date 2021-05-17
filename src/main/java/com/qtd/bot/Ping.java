package com.qtd.bot;

import org.javacord.api.event.message.MessageCreateEvent;

public class Ping implements Command{

    final String COMMAND = "ping";

    final String DESCRIPTION = "Spielt Pong! zurück :O";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public String getCommandOptional() { return ""; }

    @Override
    public String getDescription() { return DESCRIPTION; }

    @Override
    public void sendEventMessage(MessageCreateEvent event, String option) {
        event.getChannel().sendMessage("Pong!");
    }
}
