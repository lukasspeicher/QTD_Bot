package com.qtd.bot.music;

import com.qtd.bot.Command;
import com.qtd.bot.QTDBot;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;
import java.util.NoSuchElementException;

public class Music implements Command {

    final String COMMAND = "music";

    final String COMMAND_OPTIONAL = " <option>";

    final String DESCRIPTION = "Joint in den Voice Channel und spielt Musik ab.\n\n<option> = Audio-URL (z.B. YouTube)\nFügt weitere Lieder automatisch zur Queue hinzu\n\nskip = Springt zum nächsten Lied der Queue" + "\nloop = Aktiviert oder deaktiviert die Loopfunktion des akutellen Liedes\nqueue = Zeigt alle Lieder der aktuellen Queue an\n\nstop = Beendet die Wiedergabe";

    ServerVoiceChannel channel;

    AudioPlayerManager playerManager;

    AudioPlayer player;

    TrackScheduler trackScheduler;

    @Override
    public String getCommand() {
        return COMMAND;
    }

    @Override
    public String getCommandOptional() {
        return COMMAND_OPTIONAL;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void sendEventMessage(MessageCreateEvent event, String option) {

        if (option.equals("stop")) {
            QTDBot.LOGGER.info("Music stopped by user command");
            try {
                event.getServer().get().getAudioConnection().get().close();
                channel = null;
                QTDBot.LOGGER.info("Closed audio connection");
            } catch (NoSuchElementException e) {
                event.getChannel().sendMessage("Mit keinem Voice-Channel verbunden");
                QTDBot.LOGGER.info("User tried to stop music but not connected to any voice channel");
            }
            event.getApi().updateActivity(QTDBot.DEFAULT_ACTIVITY);
            return;
        }

        // Get User
        User user = event.getMessage().getAuthor().asUser().get();
        // Get VoiceChannel where User is
        long userVoiceChannelId = 0;
        List<ServerVoiceChannel> voiceChannels = event.getServer().get().getVoiceChannels();
        for (ServerVoiceChannel channel : voiceChannels) {
            if (user.isConnected(channel)) {
                userVoiceChannelId = channel.getId();
            }
        }
        // If User is connected
        if (userVoiceChannelId != 0) {

            if (option.equals("skip")) {

                if (channel != null && trackScheduler != null) {
                    trackScheduler.skip();
                    QTDBot.LOGGER.info("Song skipped by user command");
                } else {
                    event.getChannel().sendMessage("Gibt nichts zu skippen :x: ");
                    QTDBot.LOGGER.info("User tried to skip but no music is played");
                }

                return;
            } else if (option.equals("queue")) {

                if (channel != null && trackScheduler != null) {
                    trackScheduler.queueInfo();
                    QTDBot.LOGGER.info("Queue info sent to channel by user command");
                } else {
                    event.getChannel().sendMessage("Keine Queue vorhanden :x: ");
                    QTDBot.LOGGER.info("User tried to show queue but no queue available");
                }

                return;
            } else if (option.equals("loop")) {

                if (channel != null && trackScheduler != null && player != null && !player.isPaused()) {

                    if (trackScheduler.loop) {
                        trackScheduler.loop = false;
                        event.getChannel().sendMessage("Loop beendet :white_check_mark: ");
                        QTDBot.LOGGER.info("Ended loop by user command");
                    } else {
                        trackScheduler.loop = true;
                        event.getChannel().sendMessage("Track wird wiederholt :white_check_mark: ");
                        QTDBot.LOGGER.info("Started loop by user command");
                    }

                } else {
                    event.getChannel().sendMessage("Gibt nichts zu loopen :x: ");
                    QTDBot.LOGGER.info("User tried to loop but no loop activated");
                }

                return;
            }

            if (channel != null) {
                channel.getLatestInstance().thenAccept(audioConnection -> {

                    QTDBot.LOGGER.info("User sent music command. Already connected to channel");

                    play(event, option);

                }).exceptionally(e -> {
                    // Failed to connect to voice channel (no permissions?)
                    QTDBot.LOGGER.severe("Failed to retrieve to voice channel although connected to it: " + e.getMessage());
                    event.getChannel().sendMessage("Verbindung zum Channel fehlgeschlagen");
                    return null;
                });
            } else {
                QTDBot.LOGGER.info("User sent music command. Not yet connected to channel");
                try {

                    channel = event.getApi().getServerVoiceChannelById(userVoiceChannelId).get();
                    channel.connect().thenAccept(audioConnection -> {

                        QTDBot.LOGGER.info("Channel received. Initializing player");

                        // Create a player manager
                        playerManager = new DefaultAudioPlayerManager();

                        //AudioSourceManagers.registerRemoteSources(playerManager);

                        playerManager.registerSourceManager(new YoutubeAudioSourceManager());

                        player = playerManager.createPlayer();

                        trackScheduler = new TrackScheduler(player);
                        player.addListener(trackScheduler);

                        // Create an audio source and add it to the audio connection's queue
                        AudioSource source = new LavaplayerAudioSource(event.getApi(), player);
                        audioConnection.setAudioSource(source);

                        QTDBot.LOGGER.info(" Successfully initialized player, trackScheduler and audioSource");

                        play(event, option);

                    }).exceptionally(e -> {
                        // Failed to connect to voice channel (no permissions?)
                        QTDBot.LOGGER.severe("Failed to connect to voice channel: " + e.getMessage());
                        event.getChannel().sendMessage("Verbindung zum Channel fehlgeschlagen");
                        return null;
                    });

                } catch (Exception ex) {
                    QTDBot.LOGGER.severe("Failed to initialize audio connection: " + ex.getMessage());
                    event.getChannel().sendMessage("Verbindungsaufbau fehlgeschlagen");
                }
            }

        } else {
            QTDBot.LOGGER.info(" User sent music command but is not connected to a voice channel");
            event.getChannel().sendMessage("Du musst im Voice-Channel sein, um music-Befehle zu geben");
        }
    }

    private void play(MessageCreateEvent event, String option) {

        QTDBot.LOGGER.info(" User sent the following url to player: " + option);

        playerManager.loadItem(option, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                QTDBot.LOGGER.info(" Added track to player: " + track.getInfo().title);
                trackScheduler.queue(event, track, false);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    trackScheduler.queue(event, track, true);
                }
                QTDBot.LOGGER.info(" Added playlist to queue: " + playlist.getName());
                event.getChannel().sendMessage("Playlist wurde zur Queue hinzugefügt :white_check_mark: ");
            }

            @Override
            public void noMatches() {
                // nothing found
                QTDBot.LOGGER.warning(" No content for given url found");
                event.getChannel().sendMessage("Kein Inhalt gefunden");
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                // Loading failed
                QTDBot.LOGGER.warning(" Content loading failed: " + throwable.getMessage());
                event.getChannel().sendMessage("Laden fehlgeschlagen");
            }
        });
    }
}
