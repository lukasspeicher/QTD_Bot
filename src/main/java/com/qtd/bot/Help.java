package com.qtd.bot;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.time.Instant;

public class Help implements Command{

    final String COMMAND = "help";

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public void sendEventMessage(MessageCreateEvent event, String option) {
        event.getChannel().sendMessage( new EmbedBuilder()
                .setTitle("Bot Commands")
                .setDescription("Hier der Überblick über alle Commands:")
                .setAuthor("QTD-Bot", "", "")
                .addInlineField("!help", "Zeigt diese Hilfe an")
                .addInlineField("!ping", "Spielt Pong! zurück :O")
                .setColor(Color.BLUE)
                .setTimestamp(Instant.now()));
    }
}
