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
import pro.melodara.utils.commands.CommandSample;
import pro.melodara.utils.music.MusicHandler;
import pro.melodara.utils.music.MusicManager;

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

        String trackRaw = Objects.requireNonNull(event.getOption("track")).getAsString();
        long guildId = Objects.requireNonNull(event.getGuild()).getIdLong();
        Link link = Melodara.getLavaManager().getLavalinkClient().getOrCreateLink(guildId);
        MusicManager musicManager = Melodara.getLavaManager().getOrCreateMusicManager(guildId);

        link.loadItem(
                trackRaw.toLowerCase().startsWith("https://") ? trackRaw : "ytsearch:" + trackRaw
                )
                .subscribe(new MusicHandler(event, musicManager));

        event.getHook().editOriginal("OK").queue();
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

        AudioManager manager = guild.getAudioManager();

        if (!manager.isConnected())
            manager.openAudioConnection(channel);
    }
}
