package pro.melodara.music;

import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import pro.melodara.music.entities.TrackRequester;

import java.util.ArrayList;
import java.util.List;

public class TrackLoaderListener extends AbstractAudioLoadResultHandler {
    private final SlashCommandInteractionEvent event;
    private final MusicManager musicManager;

    public TrackLoaderListener(SlashCommandInteractionEvent event, MusicManager musicManager) {
        this.event = event;
        this.musicManager = musicManager;
        musicManager.getMusicMessage().setChannelUnion(event.getChannel());
    }

    @Override
    public void loadFailed(@NotNull LoadFailed loadFailed) {
        event.getHook()
                .editOriginal(":x: " + loadFailed.getException().getMessage())
                .queue(s -> {}, f -> {});
    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded trackLoaded) {
        startTrack(trackLoaded.getTrack());
    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded playlistLoaded) {
        List<Track> tracks = new ArrayList<>();
        TrackRequester trackRequester = new TrackRequester(event.getUser().getIdLong());

        for (Track track : playlistLoaded.getTracks()) {
            track.setUserData(trackRequester);
            tracks.add(track);
        }

        musicManager.getScheduler().enqueue(tracks);

        event.getHook()
                .editOriginal(":notes: Playlist **" + playlistLoaded.getInfo().getName() + "** with **" +
                        playlistLoaded.getTracks().size() + "** tracks has been added by <@" + trackRequester.userId() + ">!")
                .queue(s -> {}, f -> {});
    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult searchResult) {
        startTrack(searchResult.getTracks().get(0));
    }

    @Override
    public void noMatches() {
        event.getHook()
                .editOriginal(":x: No tracks were found!")
                .queue(s -> {}, f -> {});
    }

    private void startTrack(Track track) {
        TrackRequester trackRequester = new TrackRequester(event.getUser().getIdLong());

        track.setUserData(trackRequester);
        musicManager.getScheduler().enqueue(track);

        event.getHook()
                .editOriginal(":notes: Track [**" + track.getInfo().getTitle() + "**](<" +
                        track.getInfo().getUri() + ">) has been added by <@" + trackRequester.userId() + ">!")
                .queue(s -> {}, f -> {});
    }
}
