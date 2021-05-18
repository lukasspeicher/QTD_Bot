package com.qtd.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
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

    final String DESCRIPTION = "Joint in den Voice Channel und kann Musik abspielen.\n<option> = Audio-URL (z.B. YouTube)\nstop = beendet die Wiedergabe";

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
                event.getApi().updateActivity(QTDBot.DEFAULT_ACTIVITY);
                event.getServer().get().getAudioConnection().get().close();
            } catch (NoSuchElementException e){
                event.getApi().updateActivity(QTDBot.DEFAULT_ACTIVITY);
                event.getChannel().sendMessage("Mit keinem Voice-Channel verbunden");
            }
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

            try {

                ServerVoiceChannel channel = event.getApi().getServerVoiceChannelById(userVoiceChannelId).get();
                channel.connect().thenAccept(audioConnection -> {

                    // Create a player manager
                    AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
                    playerManager.registerSourceManager(new YoutubeAudioSourceManager());
                    AudioPlayer player = playerManager.createPlayer();

                    // Create an audio source and add it to the audio connection's queue
                    AudioSource source = new LavaplayerAudioSource(event.getApi(), player);
                    audioConnection.setAudioSource(source);

                    playerManager.loadItem(option, new AudioLoadResultHandler() {
                        @Override
                        public void trackLoaded(AudioTrack track) {
                            player.playTrack(track);
                            event.getApi().updateActivity(player.getPlayingTrack().getInfo().title);
                        }

                        @Override
                        public void playlistLoaded(AudioPlaylist playlist) {
                            for (AudioTrack track : playlist.getTracks()) {
                                player.playTrack(track);
                                //event.getApi().updateActivity(player.getPlayingTrack().getInfo().title);
                                // TODO Check Playlist Handling with Activity
                            }
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

        } else {
            event.getChannel().sendMessage("Du musst im Voice-Channel sein, um music-Befehle zu geben");
        }
    }
}
