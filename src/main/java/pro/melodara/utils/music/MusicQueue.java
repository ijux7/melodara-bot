package pro.melodara.utils.music;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MusicQueue {
    private final MusicManager mq;
    private final Queue<Track> queue = new LinkedList<>();

    public MusicQueue(MusicManager guildMusicManager) {
        mq = guildMusicManager;
    }

    public void enqueue(Track track) {
        mq.getPlayer().ifPresentOrElse(
                player -> {
                    if (player.getTrack() == null) startTrack(track);
                    else queue.offer(track);
                },
                () -> startTrack(track)
        );
    }

    public void enqueuePlaylist(List<Track> tracks) {
        queue.addAll(tracks);

        mq.getPlayer().ifPresentOrElse(
                player -> {
                    if (player.getTrack() == null) startTrack(queue.poll());
                },
                () -> startTrack(queue.poll())
        );
    }

    public void onTrackStart(Track track) {
        
    }

    public void onTrackEnd(Track lastTrack, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            Track nextTrack = queue.poll();

            if (nextTrack != null) {
                startTrack(nextTrack);
            }
        }
    }

    public Queue<Track> getQueue() {
        return queue;
    }

    private void startTrack(Track track) {
        mq.getLink().ifPresent(
                (link) -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(75)
                        .subscribe()
        );
    }
}
