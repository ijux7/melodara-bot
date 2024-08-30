package pro.melodara.utils.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class CommandSample implements CommandSampleImpl {
    public String name;
    public String description;
    public List<CommandSample> children = new ArrayList<>();
    public List<OptionData> options = new ArrayList<>();
    public boolean guildOnly = true;

    @Override
    public void run(SlashCommandInteractionEvent event) {
        event.reply("hi").queue();
    }
}
