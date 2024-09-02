package pro.melodara.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import pro.melodara.Melodara;
import pro.melodara.music.MusicManager;
import pro.melodara.utils.commands.CommandSample;

import java.util.Objects;

public class NowPlaying extends CommandSample {
    private final Melodara melodara;

    public NowPlaying(Melodara melodara) {
        this.melodara = melodara;

        this.name = "now-playing";
        this.description = "Get information about current track";
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        MusicManager musicManager = melodara.getLavalinkManager()
                .getMusicManager(Objects.requireNonNull(event.getGuild()).getIdLong());

        musicManager.getMusicMessage().sendMessage();

        event.getHook().deleteOriginal().queue(s -> {}, f -> {});
    }
}
