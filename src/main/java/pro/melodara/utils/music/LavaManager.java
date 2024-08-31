package pro.melodara.utils.music;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.melodara.Melodara;

import java.util.HashMap;

public class LavaManager {
    private final Logger LAVA_LOGGER = LoggerFactory.getLogger("melodara/lavalink");
    private final Logger NODE_LOGGER = LoggerFactory.getLogger("melodara/nodes");
    private final HashMap<Long, MusicManager> voiceManagers = new HashMap<>();
    private static LavalinkClient client = null;

    public LavaManager(@NotNull String token) {
        client = new LavalinkClient(Helpers.getUserIdFromToken(token));
        LAVA_LOGGER.info("Lavalink client has been initialized!");


        // main ready event
        client.on(ReadyEvent.class).subscribe(event -> {
            LavalinkNode node = event.getNode();

            NODE_LOGGER.info("Node '{}' is ready with session '{}'", node.getName(), node.getSessionId());
        });

        // stats
        client.on(StatsEvent.class).subscribe(event -> {
            LavalinkNode node = event.getNode();

            NODE_LOGGER.info(
                    "Node '{}' has stats, current players: {}/{} (link count: {})",
                    node.getName(), event.getPlayingPlayers(), event.getPlayers(), client.getLinks().size()
            );
        });

        // lags & reconnecting
        client.on(WebSocketClosedEvent.class).subscribe((event) -> {
            if (event.getCode() == 4006) { // INVALID SESSION
                LAVA_LOGGER.warn("WebSocket closed event (code 4006 - INVALID_SESSION), trying to reconnect ...");

                long guildId = event.getGuildId();
                Guild guild = Melodara.getShardManager().getGuildById(guildId);
                if (guild == null) return;

                GuildVoiceState vs = guild.getSelfMember().getVoiceState();
                if (vs == null) return;

                AudioChannelUnion ch = vs.getChannel();
                if (ch == null) return;

                try {
                    guild.getAudioManager().closeAudioConnection();
                    guild.getAudioManager().openAudioConnection(ch);

                    LAVA_LOGGER.info("Successfully reconnected to guild '{}'", guild.getId());
                }
                catch (IllegalArgumentException ignored) {}
                catch (Exception error) {
                    LAVA_LOGGER.error("Error while reconnecting to guild '{}'", guildId, error);
                }


            }
        });

        // tracks QUEUE
        client.on(TrackStartEvent.class).subscribe((event) ->
            getOrCreateMusicManager(event.getGuildId()).getQueueManager().onTrackStart(event.getTrack())
        );
        client.on(TrackEndEvent.class).subscribe((event) ->
                getOrCreateMusicManager(event.getGuildId()).getQueueManager().onTrackEnd(
                        event.getTrack(), event.getEndReason()
                )
        );

        // random events
        client.on(EmittedEvent.class).subscribe((event) -> {
            LavalinkNode node = event.getNode();

            NODE_LOGGER.info("Node '{}' emitted event: {}", node.getName(), event);
        });
    }

    /**
     * Initializing class
     */
    public static LavaManager create(@NotNull String token) {
        return new LavaManager(token);
    }

    public void registerHost(
            @NotNull String nodeName, @NotNull String host, @NotNull String password
    ) {
        client.addNode(
                new NodeOptions.Builder()
                        .setName(nodeName)
                        .setServerUri("ws://" + host)
                        .setPassword(password)
                        .setRegionFilter(RegionGroup.EUROPE)
                        .setHttpTimeout(5000L)
                        .build()
        );
        NODE_LOGGER.info("Added node '{}' with host '{}'", nodeName, host);
    }

    public LavalinkClient getLavalinkClient() {
        return client;
    }

    public MusicManager getOrCreateMusicManager(long guildId) {
        MusicManager mg = voiceManagers.get(guildId);

        if (mg == null) {
            mg = new MusicManager(guildId, getLavalinkClient());
            voiceManagers.put(guildId, mg);
        }

        return mg;
    }
}
