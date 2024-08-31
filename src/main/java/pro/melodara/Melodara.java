package pro.melodara;

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
import pro.melodara.listeners.Listener;
import pro.melodara.utils.commands.CommandHandler;
import pro.melodara.utils.commands.CommandManager;
import pro.melodara.utils.Configuration;
import pro.melodara.utils.music.LavalinkManager;

import java.time.Instant;

public class Melodara {
    private static final Logger LOGGER = LoggerFactory.getLogger("melodara/main");
    private static final Instant STARTUP_TIME = Instant.now();
    private static String PROJECT_NAME = null;
    private static String VERSION = null;
    private static ShardManager shardManager = null;
    private static CommandManager commandManager = null;
    private static LavalinkManager lavaManager = null;


    public static void main(String[] args) throws Exception {
        // loading configuration
        new Configuration().load("./configuration.properties");


        // static variables
        PROJECT_NAME = Configuration.get("melodara.main.name");
        VERSION = Configuration.get("melodara.main.version");
        final String TOKEN = Configuration.get("melodara.bot.discord.authentication");


        // adding commands
        commandManager = CommandManager.create()
                .addCommands(
                        new Play()
                );


        // lavalink-client
        lavaManager = LavalinkManager.create(TOKEN);
        lavaManager.registerHost("China", "localhost:10300", "12345678");

        // TODO: wait until lavalink client connects to all nodes, then start bot

        // building sharded bot
        DefaultShardManagerBuilder shardManagerBuilder = DefaultShardManagerBuilder.createDefault(TOKEN)
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavaManager.getLavalinkClient()))
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
                        new Listener(),
                        new CommandHandler()
                )
                .setShardsTotal(1)
                .setShards(0);


        // welcome message
        LOGGER.info(" ");
        LOGGER.info(
                "Welcome to '{}' version '{}'! Initializing 'JDA {}' right now ...",
                PROJECT_NAME, VERSION, JDAInfo.VERSION
        );
        LOGGER.info(" ");


        // starting
        shardManager = shardManagerBuilder.build();
    }


    public static ShardManager getShardManager() {
        assert shardManager != null : "shardManager is null";
        return shardManager;
    }

    public static CommandManager getCommandManager() {
        assert commandManager != null : "commandManager is null";
        return commandManager;
    }

    public static LavalinkManager getLavaManager() {
        assert lavaManager != null : "lavaManager is null";
        return lavaManager;
    }

    public static Instant getStartupTime() {
        return STARTUP_TIME;
    }

    public static String getProjectName() {
        return PROJECT_NAME;
    }

    public static String getVERSION() {
        return VERSION;
    }
}