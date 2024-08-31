package pro.melodara.utils.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.melodara.Melodara;

public class CommandHandler extends ListenerAdapter {
    private final Logger LOGGER = LoggerFactory.getLogger("melodara/handlers");

    public CommandHandler() {
        LOGGER.info("Loading slash (/) commands ...");

        if (Melodara.getCommandManager().getCommands().isEmpty()) {
            LOGGER.warn("No slash commands has been loaded!");
        } else {
            LOGGER.info(
                    "Loaded {} slash (/) commands! Ready to handle slash command events ...",
                    Melodara.getCommandManager().getCommands().size()
            );
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) return;

        String commandName = event.getName();
        String subcommandName = event.getSubcommandName();
        String subcommandGroup = event.getSubcommandGroup();

        CommandSample command = Melodara.getCommandManager().getCommand(commandName, subcommandName, subcommandGroup);

        if (command == null) {
            LOGGER.error("No command with name '{} {} {}' was found!", commandName, subcommandName, subcommandGroup);
            return;
        } else if (command.guildOnly && event.getGuild() == null) {
            return;
        }

        event.deferReply().queue(
                s -> command.run(event),
                f -> LOGGER.error("Failed to defer reply", f)
        );
    }
}
