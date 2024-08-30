package pro.melodara.utils.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface CommandSampleImpl {
    default void run(SlashCommandInteractionEvent event) {}
}
