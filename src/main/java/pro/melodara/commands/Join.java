package pro.melodara.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import pro.melodara.utils.commands.CommandSample;

import java.util.Objects;

public class Join extends CommandSample {
    public Join() {
        this.name = "join";
        this.description = "Joins to VC";
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inAudioChannel()) {
            event.reply("You're not in VC").queue();
            return;
        }

        AudioManager audioManager = Objects.requireNonNull(Objects.requireNonNull(event.getGuild()).getAudioManager());
        audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());

        event.reply("OK").queue();
    }
}
