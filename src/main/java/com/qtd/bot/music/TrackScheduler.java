package com.qtd.bot.music;

import com.qtd.bot.QTDBot;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;

public class TrackScheduler extends AudioEventAdapter {

    AudioPlayer player;

    ArrayList<AudioTrack> queue = new ArrayList<>();

    MessageCreateEvent event;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
    }

    // AudioEventAdapter
    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        event.getApi().updateActivity(player.getPlayingTrack().getInfo().title);
    }

    // AudioEventAdapter
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        event.getApi().updateActivity(QTDBot.DEFAULT_ACTIVITY);

        if (endReason.mayStartNext) {
            queue.remove(0);
            if(!queue.isEmpty()){
                this.player.playTrack(queue.get(0));
            }
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    public void queue(MessageCreateEvent event, AudioTrack track){
        this.event = event;

        if(queue.isEmpty()){
            queue.add(track);
            player.playTrack(track);
        } else {
            queue.add(track);
            event.getChannel().sendMessage("Track wurde zur Queue hinzugefügt :white_check_mark: ");
        }
    }

    public void skip() {
        if(queue.size() >= 2){
            player.stopTrack();
            queue.remove(0);
            this.player.playTrack(queue.get(0));
            event.getChannel().sendMessage("Track skipped :white_check_mark: ");
        } else {
            event.getChannel().sendMessage("Nichts zu skippen :x: ");
        }
    }

    public void queueInfo() {

        StringBuilder queueMessage = new StringBuilder();

        if (!queue.isEmpty()){
            for (AudioTrack track: queue) {
                int index = queue.indexOf(track)+1;

                // After 10 entries the string gets sent and a new is created to prevent char[] overflows
                if(index % 10 == 0){
                    queueMessage.append(index + ". " + track.getInfo().title).append("\n");
                    event.getChannel().sendMessage(queueMessage.toString());
                    queueMessage = new StringBuilder();
                } else {
                    queueMessage.append(index + ". " + track.getInfo().title).append("\n");
                }
            }

            event.getChannel().sendMessage(queueMessage.toString());
        } else {
            event.getChannel().sendMessage("Keine Queue vorhanden :x: ");
        }

    }
}
