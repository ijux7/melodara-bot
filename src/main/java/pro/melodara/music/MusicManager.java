package pro.melodara.music;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;

import java.util.Optional;

public class MusicManager {
    private final LavalinkManager lavalinkManager;
    private final long guildId;

    public MusicManager(long guildId, LavalinkManager lavalinkManager) {
        this.lavalinkManager = lavalinkManager;
        this.guildId = guildId;
    }

//    public void stop() {
//        this.scheduler.queue.clear();
//
//        this.getPlayer().ifPresent(
//                (player) -> player.setPaused(false)
//                        .setTrack(null)
//                        .subscribe()
//        );
//    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(this.lavalinkManager.getClient().getLinkIfCached(this.guildId));
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return getLink().map(Link::getCachedPlayer);
    }
}
