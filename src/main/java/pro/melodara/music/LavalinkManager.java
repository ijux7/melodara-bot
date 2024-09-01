package pro.melodara.music;

import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.melodara.Melodara;

import java.util.HashMap;
import java.util.List;

public class LavalinkManager {
    public static LavalinkManager create(Melodara melodara, String token) {
        return new LavalinkManager(melodara, token);
    }

    private final Logger LOGGER = LoggerFactory.getLogger("melodara/lavalink");
    private final Melodara melodara;
    private final LavalinkClient client;
    private final HashMap<Long, MusicManager> musicManagers = new HashMap<>();

    private static int TOTAL_NODES = 0;
    private static int ACTIVATED_NODES = 0;

    private static final int SESSION_INVALID = 4006;
    private static final int DISCONNECTED = 4014;

    public LavalinkManager(Melodara melodara, @NotNull String token) {
        LOGGER.info("Initializing Lavalink module ...");

        this.melodara = melodara;
        this.client = new LavalinkClient(Helpers.getUserIdFromToken(token));

        client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());

        useNodeListeners();
    }

    private void useNodeListeners() {
        // ready
        this.client.on(ReadyEvent.class).subscribe(event -> {
            LavalinkNode node = event.getNode();

            LOGGER.info("Lavalink node '{}' has been connected with session '{}'", node.getName(), node.getSessionId());

            ACTIVATED_NODES++;

            if (ACTIVATED_NODES == TOTAL_NODES && !melodara.BOT_STARTED_UP)
                melodara.startBot();
        });

        // stats
        this.client.on(StatsEvent.class).subscribe((event) -> {
            LavalinkNode node = event.getNode();

            LOGGER.info(
                    "Lavalink node '{}' has sent stats, current players: {}/{} (link count {})",
                    node.getName(), event.getPlayingPlayers(), event.getPlayers(), client.getLinks().size()
            );
        });

        // socket troubles
        client.on(WebSocketClosedEvent.class).subscribe((event) -> {
            if (event.getCode() == SESSION_INVALID) {
                long guildId = event.getGuildId();

                Guild guild = melodara.getShardManager().getGuildById(guildId);
                if (guild == null) return;

                GuildVoiceState guildVoiceState = guild.getSelfMember().getVoiceState();
                if (guildVoiceState == null) return;

                AudioChannelUnion connectedChannel = guildVoiceState.getChannel();
                if (connectedChannel == null) return;

                guild.getAudioManager().openAudioConnection(connectedChannel);
            } else if (event.getCode() == DISCONNECTED) {
                // TODO: stop player
            }
        });

        // IMPORTANT: tracks
        client.on(TrackStartEvent.class).subscribe((event) -> {
            // call start track event
        });
        client.on(TrackEndEvent.class).subscribe((event) -> {
            // call end track event
        });

        // random events
        client.on(EmittedEvent.class).subscribe(ignored -> {});
    }

    public void addNodes(List<NodeOptions> nodes) {
        LOGGER.info("Waiting for nodes to connect ...");

        for (NodeOptions node : nodes) {
            this.client.addNode(node);
            TOTAL_NODES++;
        }
    }

    public LavalinkClient getClient() {
        return client;
    }

    public MusicManager getMusicManager(long guildId) {
        synchronized(this) {
            MusicManager musicManager = musicManagers.get(guildId);

            if (musicManager == null)
                musicManager = this.musicManagers.put(guildId, new MusicManager(guildId, this));

            return musicManager;
        }
    }
}
