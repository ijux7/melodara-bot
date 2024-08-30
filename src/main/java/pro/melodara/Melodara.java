package pro.melodara;

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

import java.time.Instant;

public class Melodara {
    private static final Logger LOGGER = LoggerFactory.getLogger("melodara/main");
    private static final Instant STARTUP_TIME = Instant.now();
    private static ShardManager shardManager = null;


    public static void main(String[] args) throws Exception {
        // loading configuration
        new Configuration().load("./configuration.properties");


        // static variables
        final String PROJECT_NAME = Configuration.get("melodara.main.name");
        final String VERSION = Configuration.get("melodara.main.version");


        // building sharded bot
        DefaultShardManagerBuilder shardManagerBuilder = DefaultShardManagerBuilder.createDefault(
                Configuration.get("melodara.bot.discord.authentication")
        )
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
                .addEventListeners(new Listener())
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

    public static Instant getStartupTime() {
        return STARTUP_TIME;
    }
}