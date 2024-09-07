package pro.melodara.music;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
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

                    if (!this.trackQueue.isEmpty())
                        musicManager.getMusicMessage().updateMessage();
                },
                () -> startTrack(track)
        );
    }

    public void addPlaylist(List<Track> tracks) {
        this.trackQueue.addAll(tracks);

        this.musicManager.getPlayer().ifPresentOrElse(
                (player) -> {
                    if (player.getTrack() == null) this.startTrack(this.trackQueue.poll());

                    if (!this.trackQueue.isEmpty())
                        musicManager.getMusicMessage().updateMessage();
                },
                () -> this.startTrack(this.trackQueue.poll())
        );
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            Track nextTrack = this.trackQueue.poll();

            if (nextTrack != null) {
                this.startTrack(nextTrack);
            } else {
                musicManager.stop();
            }
        }
    }

    public void onTrackStart(Track track) {
        musicManager.getMusicMessage().sendMessageWhenStarts();
    }
    
    public void insertAs(int index, Track track) {
        List<Track> TEMP = new LinkedList<>(trackQueue);
        TEMP.add(index, track);
        trackQueue.clear();
        TEMP.forEach(trackQueue::offer);
    }

    public void startTrack(Track track) {
        startTrack(track, true);
    }

    public void startTrack(Track track, boolean markAsPrev) {
        if (markAsPrev)
            previousTrack = currentTrack;
        else
            previousTrack = null;

        currentTrack = track;

        musicManager.getLink().ifPresent(
                link -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(100) // default 100
                        .subscribe()
        );
    }
    
    public void skipTrack() {
        if(trackQueue.isEmpty()) {
            musicManager.stop();
        } else {
            startTrack(trackQueue.poll());
        }
    }

    public void playPreviousTrack() {
        Track curr = getCurrentTrack();
        Track prev = getPreviousTrack();

        if (prev == null) return;

        insertAs(0, curr);
        startTrack(prev, false);
    }

    public void clear() {
        trackQueue.clear();
        currentTrack = null;
        previousTrack = null;
    }

    public Queue<Track> getQueue() {
        return trackQueue;
    }

    public void shuffleQueue() {
        List<Track> TEMP = new LinkedList<>(trackQueue);
        Collections.shuffle(TEMP);
        trackQueue.clear();
        TEMP.forEach(trackQueue::offer);
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
