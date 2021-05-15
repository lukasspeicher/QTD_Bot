package com.qtd.bot;

import java.awt.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;

public class QTDBot {
    
    final static String COMMAND_PREFIX = "!";

    // Reseverd for futher use
    private static final Map<String, Command> commands = new HashMap<>();

    public static void main(String[] args) {

        DiscordApi api = new DiscordApiBuilder().setToken(args[0]).login().join();

        // Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().equalsIgnoreCase(COMMAND_PREFIX + "ping")) {
                event.getChannel().sendMessage("Pong!");
            }
        });

        // Add a listener which answers with the commands if someone writes "!help"
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().equalsIgnoreCase(COMMAND_PREFIX + "help")) {

                // Creates the embed
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Bot Commands")
                        .setDescription("Hier der Überblick über alle Commands:")
                        .setAuthor("QTD-Bot", "", "")
                        .addInlineField("!help", "Zeigt diese Hilfe an")
                        .addInlineField("!ping", "Spielt Pong! zurück :O")
                        .setColor(Color.BLUE)
                        .setTimestamp(Instant.now());

                event.getChannel().sendMessage(embed);
            }
        });

        // Prints the invite url
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());

    }

}
