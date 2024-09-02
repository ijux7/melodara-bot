package pro.melodara.music;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;

import java.util.Optional;

public class MusicManager {
    private final LavalinkManager lavalinkManager;
    private final long guildId;
    private final MusicScheduler scheduler;

    public MusicManager(long guildId, LavalinkManager lavalinkManager) {
        this.lavalinkManager = lavalinkManager;
        this.guildId = guildId;
        this.scheduler = new MusicScheduler(this);
    }

    public void stop() {
        this.scheduler.getQueue().clear();

        getPlayer().ifPresent(
                (player) -> player.setPaused(false)
                        .setTrack(null)
                        .subscribe()
        );
        getLink().ifPresent(link -> link.destroy().subscribe());
    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(this.lavalinkManager.getClient().getLinkIfCached(this.guildId));
    }

    public MusicScheduler getScheduler() {
        return scheduler;
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return getLink().map(Link::getCachedPlayer);
    }
}
