package com.qtd.bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioSourceBase;

public class LavaplayerAudioSource extends AudioSourceBase {

    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    private final DiscordApi api;

    /**
     * Creates a new lavaplayer audio source.
     *
     * @param api         A discord api instance.
     * @param audioPlayer An audio player from Lavaplayer.
     */
    public LavaplayerAudioSource(DiscordApi api, AudioPlayer audioPlayer) {
        super(api);
        this.audioPlayer = audioPlayer;
        this.api = api;
    }

    @Override
    public byte[] getNextFrame() {
        if (lastFrame == null) {
            return null;
        }

        if ((lastFrame.getTimecode() / 1000.0) % 4.0 == 0.0) {
            long timeInS = lastFrame.getTimecode() / 1000;

            if (timeInS > 59) {
                int minutes = (int) timeInS / 60;
                int seconds = (int) timeInS - (minutes * 60);

                if (seconds < 10) {
                    api.updateActivity(minutes + ":0" + seconds + " " + audioPlayer.getPlayingTrack().getInfo().title);
                } else {
                    api.updateActivity(minutes + ":" + seconds + " " + audioPlayer.getPlayingTrack().getInfo().title);
                }

            } else {
                if (timeInS < 10) {
                    api.updateActivity("0:0" + timeInS + " " + audioPlayer.getPlayingTrack().getInfo().title);
                } else {
                    api.updateActivity("0:" + timeInS + " " + audioPlayer.getPlayingTrack().getInfo().title);
                }
            }
        }

        return applyTransformers(lastFrame.getData());
    }

    @Override
    public boolean hasFinished() {
        return false;
    }

    @Override
    public boolean hasNextFrame() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public AudioSource copy() {
        return new LavaplayerAudioSource(getApi(), audioPlayer);
    }
}

