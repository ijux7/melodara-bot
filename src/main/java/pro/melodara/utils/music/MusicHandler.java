package pro.melodara.utils.music;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MusicHandler extends AbstractAudioLoadResultHandler {
    private final Logger LOGGER = LoggerFactory.getLogger("melodara/handlers");
    private final SlashCommandInteractionEvent event;
    private final MusicManager mg;

    public MusicHandler(SlashCommandInteractionEvent event, MusicManager mg) {
        this.event = event;
        this.mg = mg;
    }


    @Override
    public void loadFailed(@NotNull LoadFailed loadFailed) {
        event.getHook().editOriginal(":x: Failed to load your track: " + loadFailed.getException().getMessage())
                .queue();
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded trackLoaded) {
        Track track = trackLoaded.getTrack();
        addTrackToQueue(track);
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded playlist) {
        for (Track track : playlist.getTracks()) {
            track.setUserData(new MusicRequesterData(event.getUser().getIdLong()));
        }

        event.getHook().editOriginal(
                ":notes: Adding **" + playlist.getTracks().size() + "tracks** to queue..."
                )
                .queue(s -> {
                    mg.setLastRequestMessage(s);
                    mg.getQueueManager().enqueuePlaylist(playlist.getTracks());
                }, f -> {});;
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult searchResult) {
        Track track = searchResult.getTracks().get(0);
        addTrackToQueue(track);
    }

    @Override
    public void noMatches() {
        event.getHook().editOriginal(":x: No track was found!").queue();
    }

    private void addTrackToQueue(Track track) {
        track.setUserData(new MusicRequesterData(event.getUser().getIdLong()));

        event.getHook().editOriginal(":notes: Adding **" + track.getInfo().getTitle() + "** to queue...")
                .queue(s -> {
                    mg.setLastRequestMessage(s);
                    mg.getQueueManager().enqueue(track);
                }, f -> {});
    }
}
