package pro.melodara.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import pro.melodara.utils.commands.CommandSample;

import java.util.Objects;

public class Play extends CommandSample {
    public Play() {
        this.name = "play";
        this.description = "Plays the music";
        this.options.add(
                new OptionData(OptionType.STRING, "track", "URL & youtube name")
                        .setRequired(true)
        );
    }

    @Override
    public void run(SlashCommandInteractionEvent event) {
        joinToVoiceChannel(event.getGuild(), Objects.requireNonNull(event.getMember()));
    }

    private void joinToVoiceChannel(Guild guild, Member member) {
        GuildVoiceState state = member.getVoiceState();

        if (state == null)
            throw new NullPointerException("1"); // TODO: set exceptions

        AudioChannelUnion channel = state.getChannel();
        if (channel == null)
            throw new NullPointerException("2");

        if (!guild.getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT))
            throw new IllegalArgumentException("3");
        else if (
                channel.getMembers().size() == channel.asVoiceChannel().getUserLimit() &&
                        !guild.getSelfMember().hasPermission(channel, Permission.VOICE_MOVE_OTHERS)
        )
            throw new IllegalArgumentException("4");

        AudioManager manager = guild.getAudioManager();

        if (!manager.isConnected()) {
            manager.openAudioConnection(channel);
            manager.setSelfDeafened(true);
        }
    }
}
