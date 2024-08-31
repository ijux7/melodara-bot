package pro.melodara.utils.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Message;

import java.util.Optional;

public class MusicManager {
    private final long guildId;
    private final LavalinkClient lavalink;
    private final MusicQueue queueManager = new MusicQueue(this);
    private Message message = null;
    private Message lastRequestMessage = null;

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

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getLastRequestMessage() {
        return lastRequestMessage;
    }

    public void setLastRequestMessage(Message lastRequestMessage) {
        this.lastRequestMessage = lastRequestMessage;
    }
}
