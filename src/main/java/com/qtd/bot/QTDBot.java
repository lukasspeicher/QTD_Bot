package com.qtd.bot;

import java.util.ArrayList;

import com.qtd.bot.music.Music;
import com.qtd.bot.stonks.Crypto;
import com.qtd.bot.stonks.Stocks;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class QTDBot {
    
    final static String COMMAND_PREFIX = "!";

    public final static String DEFAULT_ACTIVITY = "!help | Zeigt alle Commands";

    static ArrayList<Command> commands = new ArrayList<>();

    public static void main(String[] args) {

        DiscordApi api = new DiscordApiBuilder().setToken(args[0]).login().join();
        api.updateActivity(DEFAULT_ACTIVITY);

        commands.add(new Help());
        commands.add(new Crypto());
        commands.add(new Music());
        commands.add(new Stocks());

        for (Command command : commands) {
            api.addMessageCreateListener(event -> {
                if (event.getMessageContent().contains(COMMAND_PREFIX + command.getCommand())) {
                    command.sendEventMessage(event, event.getMessageContent().substring(event.getMessageContent().indexOf(' ')+1));
                }
            });
        }

        // Prints the invite url
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());

    }

}
