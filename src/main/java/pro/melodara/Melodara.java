package pro.melodara;

import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.loadbalancing.RegionGroup;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.melodara.commands.Play;
import pro.melodara.music.LavalinkManager;
import pro.melodara.utils.commands.CommandHandler;
import pro.melodara.utils.commands.CommandManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Melodara {
    private static final Logger LOGGER = LoggerFactory.getLogger("melodara/main");
    public static final Instant STARTUP_TIME = Instant.now();
    public static String PROJECT_NAME = null;
    public static String VERSION = null;
    private ShardManager shardManager = null;
    private final CommandManager commandManager;
    private final LavalinkManager lavalinkManager;

    public Melodara() {
        // command manager
        this.commandManager = CommandManager.create()
                .addCommands(
                        new Play()
                );

        // lavalink manager
        this.lavalinkManager = LavalinkManager.create(
                this,
                Configuration.get("melodara.bot.discord.authentication")
        );
        loadNodes(Configuration.get("melodara.lavalink.nodes"));


        // TODO: move nodes data to .properties
        // TODO: wait until lavalink client connects to all nodes, then start bot

        startBot();
    }

    public static void main(String[] args) throws Exception {
        // loading configuration
        new Configuration().load("./configuration.properties");

        // static variables
        PROJECT_NAME = Configuration.get("melodara.main.name");
        VERSION = Configuration.get("melodara.main.version");

        // welcome message
        LOGGER.info(" ");
        LOGGER.info(
                "Welcome to '{}' version '{}'! Initializing '{}.class' right now ...",
                PROJECT_NAME, VERSION, Melodara.class.getSimpleName()
        );
        LOGGER.info("JDA version: JDA-{}", JDAInfo.VERSION);
        LOGGER.info(" ");

        // initializing class
        new Melodara();
    }

    private void loadNodes(String config) {
        String[] nodesRaw = config.split(";");
        List<NodeOptions> nodes = new ArrayList<>();

        for (String node : nodesRaw) {
            String[] nodeData = node.split(","); // ip:port,password,name;...

            nodes.add(
                    new NodeOptions.Builder()
                            .setName(nodeData[2])
                            .setServerUri("ws://" + nodeData[0])
                            .setPassword(nodeData[1])
                            .setRegionFilter(RegionGroup.EUROPE)
                            .build()
            );
        }

        this.lavalinkManager.addNodes(nodes);
    }

    private DefaultShardManagerBuilder getShardManagerBuilder() {
        return DefaultShardManagerBuilder.createDefault(
                        Configuration.get("melodara.bot.discord.authentication")
                )
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkManager.getClient()))
                .disableIntents(
                        GatewayIntent.AUTO_MODERATION_CONFIGURATION,
                        GatewayIntent.DIRECT_MESSAGE_POLLS,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                        GatewayIntent.GUILD_INVITES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_MODERATION,
                        GatewayIntent.GUILD_WEBHOOKS,
                        GatewayIntent.GUILD_MESSAGE_TYPING,
                        GatewayIntent.GUILD_MESSAGE_POLLS,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.SCHEDULED_EVENTS,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_PRESENCES
                )
                .enableIntents(
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .disableCache(
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.EMOJI,
                        CacheFlag.FORUM_TAGS,
                        CacheFlag.ROLE_TAGS,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ONLINE_STATUS,
                        CacheFlag.SCHEDULED_EVENTS,
                        CacheFlag.ACTIVITY,
                        CacheFlag.STICKER
                )
                .enableCache(
                        CacheFlag.VOICE_STATE
                )
                .setMemberCachePolicy(
                        MemberCachePolicy.VOICE
                )
                .setActivity(Activity.watching("the bot is starting..."))
                .setStatus(OnlineStatus.IDLE)
                .addEventListeners(
                        new BotListener(this),
                        new CommandHandler(this)
                )
                .setShardsTotal(1)
                .setShards(0);
    }

    public void startBot() {
        this.shardManager = getShardManagerBuilder().build();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public LavalinkManager getLavalinkManager() {
        return lavalinkManager;
    }
}