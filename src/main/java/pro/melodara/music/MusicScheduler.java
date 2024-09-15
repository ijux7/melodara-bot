package pro.melodara.music;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;

import java.util.ArrayList;
import java.util.List;

public class MusicScheduler {
    private final MusicManager musicManager;

    public MusicScheduler(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    private final List<Track> tracks = new ArrayList<>();
    private int currentTrackIndex = -1;
    private RepeatType repeatType = RepeatType.NONE;

    public void enqueue(Track track) {
        this.tracks.add(track);

        this.musicManager.getPlayerFromLink().ifPresent(player -> {
            if (player.getTrack() == null) {
                playNextTrack();
            }

            if (!this.tracks.isEmpty())
                this.musicManager.getMusicMessage().updateMessage();
        });
    }

    public void enqueue(List<Track> tracks, boolean updateMessage) {
        this.tracks.addAll(tracks);

        this.musicManager.getPlayerFromLink().ifPresent(player -> {
            if (player.getTrack() == null) {
                playNextTrack();
            }

            if (!this.tracks.isEmpty() && updateMessage)
                this.musicManager.getMusicMessage().updateMessage();
        });
    }

    public void enqueue(List<Track> tracks) {
        enqueue(tracks, true);
    }

    private void startTrack(Track track) {
        this.musicManager.getLink().ifPresentOrElse(
                link -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(100) // default 100
                        .subscribe(
                                s -> this.musicManager.getMusicMessage().sendMessageWhenStarts(),
                                f -> playNextTrack()
                        ),
                this::playNextTrack
        );
    }

    public Track getCurrentTrack() {
        return this.tracks.get(currentTrackIndex);
    }

    public Track getNextTrack() {
        if (this.currentTrackIndex == this.tracks.size() - 1) {
            return null;
        }

        return this.tracks.get(currentTrackIndex + 1);
    }

    public Track getPreviousTrack() {
        if (this.currentTrackIndex < 1) {
            return null;
        }

        return this.tracks.get(currentTrackIndex - 1);
    }

    public List<Track> getNextTracks() {
        List<Track> nextTracks = new ArrayList<>();

        if (this.currentTrackIndex == this.tracks.size() - 1) {
            return nextTracks;
        }

        for (int i = 0; i < this.tracks.size() - 1; i++) {
            if (i > this.currentTrackIndex) {
                nextTracks.add(this.tracks.get(i));
            }
        }

        System.out.println(" --- " + this.tracks.size());
        System.out.println(currentTrackIndex);
        System.out.println(nextTracks);

        return nextTracks;
    }

    public List<Track> getPreviousTracks() {
        List<Track> previousTracks = new ArrayList<>();

        if (this.currentTrackIndex == 0) {
            return previousTracks;
        }

        for (int i = 0; i < this.tracks.size() - 1; i++) {
            if (i < this.currentTrackIndex) {
                previousTracks.add(this.tracks.get(i));
            }
        }

        return previousTracks;
    }

    public List<Track> getTracks() {
        return this.tracks;
    }

    public void playNextTrack() {
        Track nextTrack = getNextTrack();

        if (nextTrack == null) {
            this.musicManager.stop();
            return;
        }

        this.currentTrackIndex++;
        startTrack(nextTrack);
    }

    public void playPreviousTrack() {
        Track previousTrack = getPreviousTrack();
        this.currentTrackIndex--;
        startTrack(previousTrack);
    }

    public void clear() {
        this.tracks.clear();
        this.currentTrackIndex = -1;
        this.repeatType = RepeatType.NONE;
    }

    public void repeatQueue() {
        if (this.repeatType == RepeatType.NONE) {
            this.repeatType = RepeatType.TRACK;
        } else if (repeatType == RepeatType.TRACK) {
            this.repeatType = RepeatType.QUEUE;
        } else {
            this.repeatType = RepeatType.NONE;
        }
    }

    public RepeatType getRepeatType() {
        return this.repeatType;
    }

    // events

    /**
     * @apiNote lastTrack returning object of ENDED track (with ending position)
     */
    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            if (getRepeatType().equals(RepeatType.TRACK)) {
                startTrack(getCurrentTrack());
            } else if (getRepeatType().equals(RepeatType.QUEUE) && getNextTrack() == null) {
                this.currentTrackIndex = 0;
                startTrack(getNextTrack());
            } else {
                playNextTrack();
            }
        }
    }

    public void onTrackStart(Track track) {
    }
}
