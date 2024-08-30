package pro.melodara.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import pro.melodara.utils.commands.CommandSample;

import java.util.Objects;

public class Leave extends CommandSample {
    public Leave() {
        this.name = "leave";
        this.description = "Leave to VC";
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        AudioManager audioManager = Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getAudioManager());

        if (!audioManager.isConnected()) {
            event.reply("I'm not in VC").queue();
            return;
        }

        audioManager.closeAudioConnection();

        event.reply("OK").queue();
    }
}
