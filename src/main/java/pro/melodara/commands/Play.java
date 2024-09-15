package pro.melodara.commands;

import dev.arbjerg.lavalink.client.Link;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.managers.AudioManager;
import pro.melodara.Melodara;
import pro.melodara.exceptions.CommandExecutionException;
import pro.melodara.music.MusicManager;
import pro.melodara.music.TrackLoaderListener;
import pro.melodara.utils.commands.CommandSample;

import java.util.Objects;

public class Play extends CommandSample {
    private final Melodara melodara;

    public Play(Melodara melodara) {
        this.melodara = melodara;

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

        String trackRaw = Objects.requireNonNull(event.getOption("track")).getAsString();
        long guildId = Objects.requireNonNull(event.getGuild()).getIdLong();

        Link link = melodara.getLavalinkManager().getClient().getOrCreateLink(guildId);
        MusicManager musicManager = melodara.getLavalinkManager().getMusicManager(guildId);

        String track = trackRaw.toLowerCase().startsWith("https://") ? trackRaw : "ytsearch:" + trackRaw;
        link.loadItem(track).subscribe(new TrackLoaderListener(event, musicManager));
    }

    private void joinToVoiceChannel(Guild guild, Member member) {
        GuildVoiceState state = member.getVoiceState();

        if (state == null)
            throw new CommandExecutionException("Failed to get your voice information!");

        AudioChannelUnion channel = state.getChannel();
        if (channel == null)
            throw new CommandExecutionException("You must be in the voice channel to execute this command");

        if (!guild.getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT))
            throw new CommandExecutionException("I don't have permission to join your channel!");
        else if (
                channel.getMembers().size() == channel.asVoiceChannel().getUserLimit() &&
                        !guild.getSelfMember().hasPermission(channel, Permission.VOICE_MOVE_OTHERS)
        )
            throw new CommandExecutionException("Voice channel is full! Give permission \"Move Members\"!");

        AudioManager manager = guild.getAudioManager();

        if (!manager.isConnected()) {
            manager.openAudioConnection(channel);
            manager.setSelfDeafened(true);
        }
    }
}
