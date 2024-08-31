package pro.melodara.utils.music;

import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MusicQueue {
    private final MusicManager mg;
    private final Queue<Track> queue = new LinkedList<>();

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
        net.dv8tion.jda.api.entities.Message message = mg.getMessage();

        if (message != null) message.delete().queue(s -> {}, f -> {});

        mg.getLastRequestMessage().getChannel().sendMessageEmbeds(
                new EmbedBuilder()
                        .setTitle(track.getInfo().getTitle())
                        .setUrl(track.getInfo().getUri())
                        .setDescription("Now playing music from " + track.getInfo().getSourceName() + "!")
                        .setThumbnail(track.getInfo().getArtworkUrl())
                        .build()
        ).queue(mg::setMessage, f -> mg.setMessage(null));
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
        mg.getLink().ifPresent(
                (link) -> link.createOrUpdatePlayer()
                        .setTrack(track)
                        .setVolume(75)
                        .subscribe()
        );
    }
}
