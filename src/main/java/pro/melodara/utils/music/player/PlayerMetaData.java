package pro.melodara.utils.music.player;

import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import pro.melodara.utils.music.MusicManager;

import java.util.Optional;

public class PlayerMetaData {
    private long volume = 0L;
    private long position = 0L;

    public PlayerMetaData(long volume, long position) {
        this.volume = volume;
        this.position = position;
    }

    public PlayerMetaData() {}

    public static PlayerMetaData getMetaData(MusicManager manager) {
        Optional<LavalinkPlayer> playerOptional = manager.getPlayer();

        if (playerOptional.isPresent()) {
            LavalinkPlayer player = playerOptional.get();

            return new PlayerMetaData(
                    player.getVolume(),
                    player.getPosition()
            );
        }

        return new PlayerMetaData();
    }

    public long getVolume() {
        return volume;
    }

    public long getPosition() {
        return position;
    }
}
