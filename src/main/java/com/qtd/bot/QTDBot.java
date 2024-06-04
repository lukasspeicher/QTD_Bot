package com.qtd.bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.qtd.bot.music.Music;
import com.qtd.bot.stonks.Crypto;
import com.qtd.bot.stonks.Stocks;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class QTDBot {

    public final static Logger LOGGER = Logger.getLogger("QTDBotLogger");

    final static String COMMAND_PREFIX = "!";

    public final static String DEFAULT_ACTIVITY = "!help | Zeigt alle Commands";

    static ArrayList<Command> commands = new ArrayList<>();

    public static void main(String[] args) {

        try {
            // This block configure the logger with handler and formatter
            FileHandler fileHandler = new FileHandler("./" + System.currentTimeMillis() + ".log");
            LOGGER.addHandler(fileHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);

            // First logging
            LOGGER.info("Logger initialised");
        } catch (SecurityException | IOException e) {
            LOGGER.severe("Logging initialisation failed: " + e.getMessage());
        }

        DiscordApi api = new DiscordApiBuilder().setToken(args[0]).setAllIntents().login().join();
        api.updateActivity(DEFAULT_ACTIVITY);

        commands.add(new Help());
        commands.add(new Crypto());
        commands.add(new Stocks());
        commands.add(new Music());

        for (Command command : commands) {
            api.addMessageCreateListener(event -> {
                if (event.getMessageContent().contains(COMMAND_PREFIX + command.getCommand())) {
                    command.sendEventMessage(event, event.getMessageContent().substring(event.getMessageContent().indexOf(' ') + 1));
                }
            });
        }

        LOGGER.info("Bot started");

        // Prints the invite url
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }

}
