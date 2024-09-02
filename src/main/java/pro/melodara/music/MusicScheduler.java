package pro.melodara.music;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MusicScheduler {
    private final MusicManager musicManager;
    private final Queue<Track> trackQueue = new LinkedList<>();

    public MusicScheduler(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    public void addTrack(Track track) {
        musicManager.getPlayer().ifPresentOrElse(
                player -> {
                    if (player.getTrack() == null) startTrack(track);
                    else this.trackQueue.offer(track);
                },
                () -> {
                    startTrack(this.trackQueue.poll());
                }
        );
    }

    public void addPlaylist(List<Track> tracks) {
        this.trackQueue.addAll(tracks);

        this.musicManager.getPlayer().ifPresentOrElse(
                (player) -> {
                    if (player.getTrack() == null) this.startTrack(this.trackQueue.poll());
                },
                () -> {
                    this.startTrack(this.trackQueue.poll());
                }
        );
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            final var nextTrack = this.trackQueue.poll();

            if (nextTrack != null) {
                this.startTrack(nextTrack);
            }
        }
    }

    public void startTrack(Track track) {
        musicManager.getLink().ifPresent(
                link -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(75)
                        .subscribe()
        );
    }

    public Queue<Track> getQueue() {
        return trackQueue;
    }
}
