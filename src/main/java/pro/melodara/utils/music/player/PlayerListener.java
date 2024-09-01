package pro.melodara.utils.music.player;

import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pro.melodara.Melodara;
import pro.melodara.utils.music.MusicManager;

import java.util.List;
import java.util.Objects;

public class PlayerListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Guild guild = event.getGuild();
        List<String> playerButtons = List.of(
                "pause", "next", "prev", "plus15s", "minus15s"
        );

        if (guild == null || event.getButton().getId() == null) return;

        MusicManager mg = Melodara.getLavaManager().getOrCreateMusicManager(guild.getIdLong());
        Message msg = mg.getMessagePlayer().getMessage();

        if (
                !playerButtons.contains(event.getButton().getId()) ||
                        msg == null || !event.getMessage().getId().equals(msg.getId())
        )
            return;

        event.deferReply(true).queue(s -> proccessButtons(event), f -> {});
    }

    private void proccessButtons(@NotNull ButtonInteractionEvent event) {
        Link link = Melodara.getLavaManager().getLavalinkClient()
                .getOrCreateLink(Objects.requireNonNull(event.getGuild()).getIdLong());
        MusicManager mg = Melodara.getLavaManager().getOrCreateMusicManager(event.getGuild().getIdLong());

        switch (Objects.requireNonNull(event.getButton().getId())) {
            case "pause":
                link.getPlayer().flatMap(f -> f.setPaused(!f.getPaused()))
                        .subscribe(p -> {
                            mg.getMessagePlayer().updateEmbed();
                            event.getHook().editOriginal(
                                            ":white_check_mark: Successfully " +
                                                    (p.getPaused() ? "paused" : "resumed")
                                    )
                                    .queue(s -> {}, f -> {});
                        });
                break;

            case "next":
                link.getPlayer().flatMap(f -> f.setTrack(mg.getQueueManager().getQueue().poll()))
                        .subscribe(p -> {
                            event.getHook().editOriginal(":white_check_mark: Successfully skipped!")
                                    .queue(s -> {}, f -> {});
                        });

            case "plus15s":
                link.getPlayer()
                        .flatMap(f -> f.setPosition(mg.getQueueManager().getCurrentTrack().getInfo().getPosition() + 15000L))
                        .subscribe(p -> {
                            mg.getQueueManager().setCurrentTrack(p.getTrack());
                            mg.getMessagePlayer().updateEmbed();
                            event.getHook().editOriginal(":white_check_mark: Successfully!")
                                    .queue(s -> {}, f -> {});
                        });
                break;

            case "minus15s":
                link.getPlayer()
                        .flatMap(f -> f.setPosition(mg.getQueueManager().getCurrentTrack().getInfo().getPosition() - 15000L))
                        .subscribe(p -> {
                            mg.getQueueManager().setCurrentTrack(p.getTrack());
                            mg.getMessagePlayer().updateEmbed();
                            event.getHook().editOriginal(":white_check_mark: Successfully!")
                                    .queue(s -> {}, f -> {});
                        });
                break;

            default:
                break;
        }
    }
}
