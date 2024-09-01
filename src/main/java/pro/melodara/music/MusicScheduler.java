package pro.melodara.music;

import javax.sound.midi.Track;
import java.util.LinkedList;
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
                    // player found
                },
                () -> {
                    // not found
                }
        );
    }
}
