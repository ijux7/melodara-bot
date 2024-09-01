package pro.melodara.utils.music;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MusicQueue {
    private final MusicManager mg;
    private final Queue<Track> queue = new LinkedList<>();
    private Track currentTrack = null;

    public MusicQueue(MusicManager guildMusicManager) {
        mg = guildMusicManager;
    }

    public void enqueue(Track track) {
        mg.getPlayer().ifPresentOrElse(
                player -> {
                    if (player.getTrack() == null) startTrack(track);
                    else queue.offer(track);
                },
                () -> startTrack(track)
        );
    }

    public void enqueuePlaylist(List<Track> tracks) {
        queue.addAll(tracks);

        mg.getPlayer().ifPresentOrElse(
                player -> {
                    if (player.getTrack() == null) startTrack(queue.poll());
                },
                () -> startTrack(queue.poll())
        );
    }

    public void onTrackStart(Track track) {
        setCurrentTrack(track);
        mg.getMessagePlayer().send(true);
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        mg.getMessagePlayer().stopUpdatingPlayerMessage();

        if (endReason.getMayStartNext()) {
            Track nextTrack = queue.poll();

            if (nextTrack != null) {
                startTrack(nextTrack);
            } else {
                mg.getMessagePlayer().finish();
                mg.stop();
            }
        }
    }

    public Queue<Track> getQueue() {
        return queue;
    }

    private void startTrack(Track track) {
        mg.getLink().ifPresent(
                (link) -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(75)
                        .subscribe()
        );
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setCurrentTrack(Track currentTrack) {
        this.currentTrack = currentTrack;
    }
}
