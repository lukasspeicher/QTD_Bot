package com.qtd.bot;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.time.Instant;

public class Help implements Command{

    final String COMMAND = "help";

    final String DESCRIPTION = "Zeigt diese Hilfe an";

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

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Bot Commands")
            .setDescription("Hier der Überblick über alle Commands:")
            .setAuthor("QTD-Bot", "", "")
            .setColor(Color.BLUE)
            .setTimestamp(Instant.now());

        for (Command command: QTDBot.commands) {
            embed.addField(QTDBot.COMMAND_PREFIX + command.getCommand() + command.getCommandOptional(), command.getDescription());
        }

        event.getChannel().sendMessage(embed);
        QTDBot.LOGGER.info("Help command sent to channel");
    }
}
