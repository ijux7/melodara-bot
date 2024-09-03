package pro.melodara.utils.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.melodara.exceptions.CommandExecutionException;

import java.util.ArrayList;
import java.util.List;

public class CommandSample implements CommandSampleImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger("melodara/commands");
    public String name;
    public String description;
    public final List<CommandSample> children = new ArrayList<>();
    public final List<OptionData> options = new ArrayList<>();
    public boolean guildOnly = true;

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.reply("hi").queue(s -> {}, f -> {});
    }

    public void execute(SlashCommandInteractionEvent event) {
        try {
            run(event);
        } catch (CommandExecutionException exception) {
            event.getHook().editOriginal(":x: " + exception.getErrorCode()).queue();
        } catch (Exception e) {
            LOGGER.error(
                    "Non Based Exception, command: {}, guildId {}",
                    event.getFullCommandName(),
                    event.getGuild() == null ? "N/A" : event.getGuild().getIdLong(),
                    e
            );
        }
    }
}
