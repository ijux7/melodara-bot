package pro.melodara.music;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MusicScheduler {
    private final MusicManager manager;
    private final Queue<Track> nextQueue = new LinkedList<>();
    private final Queue<Track> previousQueue = new LinkedList<>();
    private Track currentTrack = null;
    private RepeatType repeatType = RepeatType.NONE;

    public MusicScheduler(MusicManager manager) {
        this.manager = manager;
    }

    public void enqueue(Track track) {
        this.nextQueue.offer(track);

        this.manager.getPlayer().ifPresentOrElse(
                player -> {
                    if (player.getTrack() == null)
                        playNextTrack();

                    if (!this.nextQueue.isEmpty())
                        manager.getMusicMessage().updateMessage();
                },
                this::playNextTrack
        );
    }

    public void enqueue(List<Track> tracks, boolean updateMessage) {
        this.nextQueue.addAll(tracks);

        this.manager.getPlayer().ifPresentOrElse(
                (player) -> {
                    if (player.getTrack() == null)
                        playNextTrack();

                    if (!this.nextQueue.isEmpty() && updateMessage)
                        manager.getMusicMessage().updateMessage();
                },
                this::playNextTrack
        );
    }

    public void enqueue(List<Track> tracks) {
        enqueue(tracks, true);
    }

    private void enqueueInNextQueueAsZero(Track track) {
        List<Track> TEMP = new LinkedList<>(nextQueue);
        TEMP.add(0, track);
        nextQueue.clear();
        TEMP.forEach(nextQueue::offer);
    }

    private void enqueueInPreviousQueueAsZero(Track track) {
        List<Track> TEMP = new LinkedList<>(previousQueue);
        TEMP.add(0, track);
        previousQueue.clear();
        TEMP.forEach(previousQueue::offer);
    }

    private void startTrack(Track track) {
        manager.getLink().ifPresent(
                link -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(100) // default 100
                        .subscribe(s -> manager.getMusicMessage().sendMessageWhenStarts(), f -> playNextTrack())
        );
    }

    public void playNextTrack() {
        if (nextQueue.isEmpty()) {
            manager.stop();
            return;
        }

        if (this.currentTrack != null) {
            this.enqueueInPreviousQueueAsZero(this.currentTrack);
        }

        this.currentTrack = nextQueue.poll();
        startTrack(this.currentTrack);
    }

    public void playPreviousTrack() {
        enqueueInNextQueueAsZero(this.currentTrack);
        this.currentTrack = previousQueue.poll();
        startTrack(this.currentTrack);
    }

    public void clear() {
        this.nextQueue.clear();
        this.previousQueue.clear();
        this.currentTrack = null;
        this.repeatType = RepeatType.NONE;
    }

    public void shuffleQueue() {
        List<Track> TEMP = new LinkedList<>(nextQueue);
        Collections.shuffle(TEMP);
        nextQueue.clear();
        TEMP.forEach(nextQueue::offer);
    }

    public Track getCurrentTrack() {
        return this.currentTrack;
    }

    public List<Track> getNextQueue() {
        return new LinkedList<>(this.nextQueue);
    }

    public Track getNextTrack() {
        List<Track> queue = getNextQueue();
        return queue.isEmpty() ? null : queue.get(0);
    }

    public List<Track> getPreviousQueue() {
        return new LinkedList<>(this.previousQueue);
    }

    public Track getPreviousTrack() {
        List<Track> queue = getPreviousQueue();
        return queue.isEmpty() ? null : queue.get(0);
    }

    public void repeatQueue() {
        if (repeatType == RepeatType.NONE) {
            repeatType = RepeatType.TRACK;
        } else if (repeatType == RepeatType.TRACK) {
            repeatType = RepeatType.QUEUE;
        } else {
            repeatType = RepeatType.NONE;
        }
    }

    public RepeatType getRepeatType() {
        return this.repeatType;
    }

    // events

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            if (getRepeatType().equals(RepeatType.TRACK)) {
                startTrack(lastTrack);
            } else if (getRepeatType().equals(RepeatType.QUEUE) && nextQueue.isEmpty()) {
                enqueueInPreviousQueueAsZero(lastTrack);
                List<Track> queue = getPreviousQueue();
                previousQueue.clear();
                Collections.reverse(queue);

                enqueue(queue, false);
            } else {
                playNextTrack();
            }
        }
    }

    public void onTrackStart(Track track) {
    }
}
