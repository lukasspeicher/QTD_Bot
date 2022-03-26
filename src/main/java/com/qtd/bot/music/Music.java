package com.qtd.bot.music;

import com.qtd.bot.Command;
import com.qtd.bot.QTDBot;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;
import java.util.NoSuchElementException;

public class Music implements Command {

    final String COMMAND = "music";

    final String COMMAND_OPTIONAL = " <option>";

    final String DESCRIPTION = "Joint in den Voice Channel und kann Musik abspielen.\n\n<option> = Audio-URL (z.B. YouTube)\nFügt weitere Lieder automatisch zur Queue hinzu\n\nskip = Springt zum nächsten Lied der Queue" +
            "\nqueue = Zeigt alle Lieder der aktuellen Queue an\n\nstop = Beendet die Wiedergabe";

    ServerVoiceChannel channel;

    AudioPlayerManager playerManager;

    AudioPlayer player;

    TrackScheduler trackScheduler;

    @Override
    public String getCommand() { return COMMAND; }

    @Override
    public String getCommandOptional() { return COMMAND_OPTIONAL; }

    @Override
    public String getDescription() { return DESCRIPTION; }

    @Override
    public void sendEventMessage(MessageCreateEvent event, String option) {

        if(option.equals("stop")){
            try {
                event.getServer().get().getAudioConnection().get().close();
                channel = null;
            } catch (NoSuchElementException e){
                event.getChannel().sendMessage("Mit keinem Voice-Channel verbunden");
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

            if(option.equals("skip")){
                if(channel != null && trackScheduler != null){
                    trackScheduler.skip();
                    return;
                } else {
                    event.getChannel().sendMessage("Nichts zu skippen :x: ");
                    return;
                }
            } else if(option.equals("queue")){
                if(channel != null && trackScheduler != null){
                    trackScheduler.queueInfo();
                    return;
                } else {
                    event.getChannel().sendMessage("Keine Queue vorhanden :x: ");
                    return;
                }
            }

            if(channel != null){
                channel.getLatestInstance().thenAccept(audioConnection -> {

                    play(event, option);

                }).exceptionally(e -> {
                    // Failed to connect to voice channel (no permissions?)
                    e.printStackTrace();
                    event.getChannel().sendMessage("Verbindung zum Channel fehlgeschlagen");
                    return null;
                });
            } else {
                try {

                    channel = event.getApi().getServerVoiceChannelById(userVoiceChannelId).get();
                    channel.connect().thenAccept(audioConnection -> {

                        // Create a player manager
                        playerManager = new DefaultAudioPlayerManager();
                        AudioSourceManagers.registerRemoteSources(playerManager);
                        player = playerManager.createPlayer();

                        trackScheduler = new TrackScheduler(player);
                        player.addListener(trackScheduler);

                        // Create an audio source and add it to the audio connection's queue
                        AudioSource source = new LavaplayerAudioSource(event.getApi(), player);
                        audioConnection.setAudioSource(source);

                        play(event, option);

                    }).exceptionally(e -> {
                        // Failed to connect to voice channel (no permissions?)
                        e.printStackTrace();
                        event.getChannel().sendMessage("Verbindung zum Channel fehlgeschlagen");
                        return null;
                    });

                } catch (Exception ex) {
                    ex.printStackTrace();
                    event.getChannel().sendMessage("Verbindungsaufbau fehlgeschlagen");
                }
            }

        } else {
            event.getChannel().sendMessage("Du musst im Voice-Channel sein, um music-Befehle zu geben");
        }
    }

    private void play(MessageCreateEvent event, String option) {
        playerManager.loadItem(option, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.queue(event, track, false);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    trackScheduler.queue(event, track, true);
                }
                event.getChannel().sendMessage("Playlist wurde zur Queue hinzugefügt :white_check_mark: ");
            }

            @Override
            public void noMatches() {
                // nothing found
                event.getChannel().sendMessage("Kein Inhalt gefunden");
            }

            @Override
            public void loadFailed(FriendlyException throwable) {
                // Loading failed
                event.getChannel().sendMessage("Laden fehlgeschlagen");
            }
        });
    }
}
