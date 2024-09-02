package pro.melodara.music;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MusicScheduler {
    private final MusicManager musicManager;
    private final Queue<Track> trackQueue = new LinkedList<>();
    private Track currentTrack = null;
    private Track previousTrack = null;

    public MusicScheduler(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    public void addTrack(Track track) {
        musicManager.getPlayer().ifPresentOrElse(
                player -> {
                    if (player.getTrack() == null) startTrack(track);
                    else this.trackQueue.offer(track);
                },
                () -> startTrack(this.trackQueue.poll())
        );
    }

    public void addPlaylist(List<Track> tracks) {
        this.trackQueue.addAll(tracks);

        this.musicManager.getPlayer().ifPresentOrElse(
                (player) -> {
                    if (player.getTrack() == null) this.startTrack(this.trackQueue.poll());
                },
                () -> this.startTrack(this.trackQueue.poll())
        );
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            Track nextTrack = this.trackQueue.poll();

            if (nextTrack != null) {
                this.startTrack(nextTrack);
            }
        }
    }

    public void onTrackStart(Track track) {
        previousTrack = currentTrack;
        currentTrack = track;
    }

    public void startTrack(Track track) {
        musicManager.getLink().ifPresent(
                link -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(75)
                        .subscribe(
                                l -> musicManager.getMusicMessage().sendMessage(),
                                f -> {
                                    // todo: message
                                }
                        )
        );
    }

    public Queue<Track> getQueue() {
        return trackQueue;
    }

    public @Nullable Track getCurrentTrack() {
        return currentTrack;
    }

    public @Nullable Track getPreviousTrack() {
        return previousTrack;
    }

    public @Nullable Track getNextTrack() {
        if (trackQueue.isEmpty()) return null;
        return trackQueue.stream().toList().get(0);
    }
}
