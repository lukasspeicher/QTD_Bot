package com.qtd.bot;

import org.javacord.api.event.message.MessageCreateEvent;

public interface Command{

    String getCommand();

    String getCommandOptional();

    String getDescription();

    void sendEventMessage(MessageCreateEvent event, String option);

}
