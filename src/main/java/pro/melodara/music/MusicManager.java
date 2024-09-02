package pro.melodara.music;

import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.managers.AudioManager;
import pro.melodara.Melodara;
import pro.melodara.music.messages.MusicMessage;

import java.util.Optional;

public class MusicManager {
    private final Melodara melodara;
    private final LavalinkManager lavalinkManager;
    private final long guildId;
    private final MusicScheduler scheduler;
    private final MusicMessage musicMessage;

    public MusicManager(long guildId, LavalinkManager lavalinkManager, Melodara melodara) {
        this.melodara = melodara;
        this.lavalinkManager = lavalinkManager;
        this.guildId = guildId;
        this.scheduler = new MusicScheduler(this);
        this.musicMessage = new MusicMessage(this);
    }

    public void stop() {
        Guild guild = melodara.getShardManager().getGuildById(guildId);

        if (guild != null) {
            AudioManager audioManager = guild.getAudioManager();
            audioManager.closeAudioConnection();
        }

        scheduler.clear();
        getPlayer().ifPresent((player) -> player.setPaused(false).setTrack(null).subscribe());
        musicMessage.delete();
    }

    public Optional<Link> getLink() {
        return Optional.ofNullable(this.lavalinkManager.getClient().getLinkIfCached(this.guildId));
    }

    public Optional<LavalinkPlayer> getPlayer() {
        return getLink().map(Link::getCachedPlayer);
    }

    public MusicScheduler getScheduler() {
        return scheduler;
    }

    public MusicMessage getMusicMessage() {
        return musicMessage;
    }
}
