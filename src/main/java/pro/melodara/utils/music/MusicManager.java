package pro.melodara.utils.music;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import pro.melodara.Melodara;
import pro.melodara.utils.music.player.MessagePlayer;

import java.util.Optional;

public class MusicManager {
    private final long guildId;
    private final LavalinkClient lavalink;
    private final MusicQueue queueManager = new MusicQueue(this);
    private final MessagePlayer messagePlayer;

    public MusicManager(long guildId, LavalinkClient lavalink) {
        this.lavalink = lavalink;
        this.guildId = guildId;
        this.messagePlayer = new MessagePlayer(this);
    }

    public void stop() {
        this.queueManager.getQueue().clear();

        this.getPlayer().ifPresent(
                (player) -> player.setPaused(false)
                        .setTrack(null)
                        .subscribe()
        );

        Guild guild = Melodara.getShardManager().getGuildById(guildId);

        if (guild == null)
            return;

        AudioManager audioManager = guild.getAudioManager();

        messagePlayer.finish();
        getLink().ifPresent(Link::destroy);
        audioManager.closeAudioConnection();
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

    public MessagePlayer getMessagePlayer() {
        return messagePlayer;
    }

    public long getGuildId() {
        return guildId;
    }
}
