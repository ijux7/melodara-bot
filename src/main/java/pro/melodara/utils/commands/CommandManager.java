package pro.melodara.utils.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private final Logger LOGGER = LoggerFactory.getLogger("melodara/commands");
    private final List<CommandSample> commands = new ArrayList<>();

    public CommandManager() {}

    public static CommandManager create() {
        return new CommandManager();
    }

    public CommandManager addCommands(CommandSample... command) {
        this.commands.addAll(List.of(command));
        return this;
    }

    private List<CommandData> getCommandData() {
        List<CommandData> data = new ArrayList<>();

        for (CommandSample command : commands) {
            SlashCommandData commandData = Commands.slash(command.name,command.description);

            if (command.children.isEmpty()) {
                data.add(commandData.addOptions(command.options));
            } else {
                List<SubcommandData> subcommands = new ArrayList<>();
                List<SubcommandGroupData> subcommandGroups = new ArrayList<>();

                for (CommandSample child : command.children) {
                    if (child.children.isEmpty()) {
                        subcommands.add(new SubcommandData(child.name, child.description));
                    } else {
                        SubcommandGroupData subcommandGroup = new SubcommandGroupData(child.name, child.description);

                        for (CommandSample child2 : child.children) {
                            subcommandGroup.addSubcommands(new SubcommandData(child2.name, child2.description));
                        }

                        subcommandGroups.add(subcommandGroup);
                    }
                }

                commandData.addSubcommands(subcommands);
                commandData.addSubcommandGroups(subcommandGroups);

                data.add(commandData);
            }
        }

        return data;
    }

    public void updateCommandsGlobally(JDA jda)
    {
        LOGGER.info("shard {} | Fetching and trying to update slash (/) commands ...", jda.getShardInfo().getShardId());

        List<CommandData> data = getCommandData();

        jda.updateCommands().addCommands(data).queue(
                s -> LOGGER.info(
                        "shard {} | Successfully updated {} slash (/) commands [GLOBAL]",
                        jda.getShardInfo().getShardId(), data.size()
                ),
                f -> LOGGER.error(
                        "shard {} | Failed to update {} slash (/) commands globally",
                        jda.getShardInfo().getShardId(), data.size(), f
                )
        );
    }

    public CommandSample getCommand(
            @NotNull String commandName,
            @Nullable String subcommand,
            @Nullable String subcommandGroup
    ) {
        for (CommandSample command : commands) {
            if (commandName.equals(command.name)) {
                if (subcommand == null && subcommandGroup == null) {
                    return command;
                }

                for (CommandSample child : command.children) {
                    if (subcommandGroup == null && subcommand.equals(child.name)) {
                        return child;
                    } else if (subcommandGroup != null && subcommand != null && subcommandGroup.equals(child.name)) {
                        for (CommandSample child2 : child.children) {
                            if (subcommand.equals(child2.name)) {
                                return child2;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public List<CommandSample> getCommands() {
        return commands;
    }
}
