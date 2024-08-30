package pro.melodara;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class Listener extends ListenerAdapter {
    private final Logger LOGGER = LoggerFactory.getLogger("melodara/events");

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Melodara.getShardManager().setActivity(Activity.listening("Linkin Park"));
        Melodara.getShardManager().setStatus(OnlineStatus.ONLINE);

        LOGGER.info(
                "SHARD #{} | Startup time: {} secs; User#tag: {}",
                event.getJDA().getShardInfo().getShardId(),
                Instant.now().getEpochSecond()  - Melodara.getStartupTime().getEpochSecond(),
                event.getJDA().getSelfUser().getAsTag()
        );
    }
}
