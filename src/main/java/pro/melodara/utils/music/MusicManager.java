package pro.melodara.utils.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;

import java.util.Optional;

public class MusicManager {
    private final long guildId;
    private final LavalinkClient lavalink;
    private final MusicQueue queueManager = new MusicQueue(this);

    public MusicManager(long guildId, LavalinkClient lavalink) {
        this.lavalink = lavalink;
        this.guildId = guildId;
    }

    public void stop() {
        this.queueManager.getQueue().clear();

        this.getPlayer().ifPresent(
                (player) -> player.setPaused(false)
                        .setTrack(null)
                        .subscribe()
        );
    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(lavalink.getLinkIfCached(guildId));
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return getLink().map(Link::getCachedPlayer);
    }

    public MusicQueue getQueueManager() {
        return queueManager;
    }
}
