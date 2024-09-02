package pro.melodara.music.messages;

import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import pro.melodara.Melodara;
import pro.melodara.music.MusicManager;
import pro.melodara.utils.StringFormat;

import java.util.Optional;
import java.util.Queue;


public class MusicMessage {
    private final MusicManager musicManager;
    private MessageChannelUnion messageChannel = null;

    public MusicMessage(MusicManager musicManager) {
        this.musicManager = musicManager;
    }

    public MessageEmbed getEmbed() {
        Track currentTrack = musicManager.getScheduler().getCurrentTrack();
        Track nextTrack = musicManager.getScheduler().getNextTrack();
        Track previousTrack = musicManager.getScheduler().getPreviousTrack();

        EmbedBuilder embed = new EmbedBuilder();
        StringBuilder footerText = new StringBuilder(
                Melodara.PROJECT_NAME + " version " + Melodara.VERSION
        );

        Optional<LavalinkPlayer> playerOptional = musicManager.getPlayer();

        if (currentTrack == null || playerOptional.isEmpty()) {
            embed.setDescription(":x: Currently no tracks are currently playing.");
            embed.setFooter(footerText.toString());

            return embed.build();
        }

        LavalinkPlayer player = playerOptional.get();
        Queue<Track> tracks = musicManager.getScheduler().getQueue();

        embed.setTitle(StringFormat.limitString(50, currentTrack.getInfo().getTitle()));
        embed.setUrl(currentTrack.getInfo().getUri());
        embed.setThumbnail(currentTrack.getInfo().getArtworkUrl());

        embed.addField(new MessageEmbed.Field(
                "Duration",
                StringFormat.getDurationWithNames(currentTrack.getInfo().getLength()),
                true
        ));
        embed.addField(new MessageEmbed.Field(
                "Volume",
                player.getVolume() + " / 150%",
                true
        ));
        embed.addField(new MessageEmbed.Field(
                "Duration",
                StringFormat.getDurationWithNames(currentTrack.getInfo().getLength()),
                true
        ));

        embed.addField(new MessageEmbed.Field(
                "Next/Previous Tracks",
                "- Next: " + (nextTrack == null ? "No next track" :
                        "[" + StringFormat.limitString(50, nextTrack.getInfo().getTitle()) + "](" +
                                nextTrack.getInfo().getUri() + ")") +
                        (tracks.size() > 1 ? " (then " + tracks.size() + " tracks)" : "") + "\n" +
                        "- Previous: " + (previousTrack == null ? "No previous track" :
                        "[" + StringFormat.limitString(50, previousTrack.getInfo().getTitle()) + "](" +
                                previousTrack.getInfo().getUri() + ")"),
                false
        ));

        return embed.build();
    }

    public void sendMessage() {
        if (messageChannel == null) return;

        messageChannel.sendMessageEmbeds(getEmbed()).queue(s -> {}, f -> {});
    }

    public void setChannelUnion(MessageChannelUnion messageChannel) {
        this.messageChannel = messageChannel;
    }
}
