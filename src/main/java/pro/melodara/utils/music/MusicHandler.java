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

    }

    @Override
    public void ontrackLoaded(@NotNull TrackLoaded trackLoaded) {
        Track track = trackLoaded.getTrack();

        track.setUserData(new MusicRequesterData(event.getUser().getIdLong()));

        mg.getQueueManager().enqueue(track);


    }

    @Override
    public void onPlaylistLoaded(@NotNull PlaylistLoaded playlistLoaded) {

    }

    @Override
    public void onSearchResultLoaded(@NotNull SearchResult searchResult) {
        Track track = searchResult.getTracks().get(0);

        track.setUserData(new MusicRequesterData(event.getUser().getIdLong()));

        mg.getQueueManager().enqueue(track);


    }

    @Override
    public void noMatches() {

    }
}
