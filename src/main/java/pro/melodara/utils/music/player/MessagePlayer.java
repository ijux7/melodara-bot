package pro.melodara.utils.music.player;

import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import pro.melodara.Melodara;
import pro.melodara.utils.StringFormat;
import pro.melodara.utils.music.MusicManager;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MessagePlayer {
    private final MusicManager mg;
    private MessageChannelUnion lastPlayCommandText = null;
    private Message message = null;
    private Instant lastTimeMessageUpdated = null;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> future = null;
    private final int MESSAGE_UPDATE_INTERVAL = 20;

    public MessagePlayer(MusicManager mg) {
        this.mg = mg;
        this.scheduler = Executors.newScheduledThreadPool(1, s ->
                new Thread(s, "Lavalink-G-" + mg.getGuildId()));
    }

    private MessageEmbed getEmbed(boolean isNew) {
        Track track = mg.getQueueManager().getCurrentTrack();
        EmbedBuilder embed = new EmbedBuilder();
        List<Track> nextTracks = mg.getQueueManager().getQueue().stream().toList();

        PlayerMetaData playerMetaData = PlayerMetaData.getMetaData(mg);

        embed.setTitle(StringFormat.limitString(50, track.getInfo().getTitle()));
        embed.setUrl(track.getInfo().getUri());
        embed.setThumbnail(track.getInfo().getArtworkUrl());
        embed.addField(new MessageEmbed.Field(
                "Duration",
                getProgressBar(
                        isNew ? 0L : playerMetaData.getPosition(), track.getInfo().getLength()
                ) + "\n" +
                        StringFormat.getDuration(playerMetaData.getPosition()) + " — " +
                        StringFormat.getDuration(track.getInfo().getLength()),
                false
        ));

        embed.setFooter(
                (
                        nextTracks.isEmpty() ? "No next tracks" :
                        String.format(
                                "Next track: %s",
                                StringFormat.limitString(50, nextTracks.get(0).getInfo().getTitle())
                        ) + (
                                nextTracks.size() > 1 ?
                                String.format(" | Then %s tracks more...", nextTracks.size() - 1) : ""
                        )
                ) +
                        "\n" +
                        Melodara.getProjectName() + " " + Melodara.getVERSION() +
                        " | Source: " + track.getInfo().getSourceName(),
                nextTracks.isEmpty() ? null : nextTracks.get(0).getInfo().getArtworkUrl()
        );

        return embed.build();
    }

    private Collection<ActionRow> getActionRows() {
        return List.of(
                ActionRow.of(
                        Button.primary("prev", Emoji.fromUnicode("⏮")),
                        Button.primary("pause", Emoji.fromUnicode("⏸")),
                        Button.primary("next", Emoji.fromUnicode("⏭"))
                ),
                ActionRow.of(
                        Button.primary("3", Emoji.fromUnicode("⏮")),
                        Button.primary("2", Emoji.fromUnicode("⏸")),
                        Button.primary("4", Emoji.fromUnicode("⏭"))
                )
        );
    }

    private String getProgressBar(long pos, long max) {
        StringBuilder builder = new StringBuilder();

        int progress = (int) ((double) pos / max * 10);

        for (int i = 0; i < 10; i++) {
            if (i < progress) {
                builder.append("<:linepurple:1279555712074125393>");
            } else {
                builder.append("<:linegray:1279554982630129674>");
            }
        }

        return builder.toString();
    }

    public void send(boolean isNew) {
        if (lastPlayCommandText == null)
            return;

        if (message != null)
            message.delete().queue(s -> {}, f -> {});

        lastPlayCommandText.sendMessageEmbeds(getEmbed(isNew))
                .setComponents(getActionRows())
                .queue(s -> {
                    setMessage(s);
                    startUpdatingTask();
                }, f -> {});
    }

    public void updateEmbed() {
        if (message == null)
            return;

        Instant now = Instant.now();

        if (lastTimeMessageUpdated != null &&
                (int) now.getEpochSecond() - lastTimeMessageUpdated.getEpochSecond() < MESSAGE_UPDATE_INTERVAL)
            return;

        message.editMessageEmbeds(getEmbed(false))
                .setComponents(getActionRows())
                .queue(s -> {
                    setMessage(s);
                    lastTimeMessageUpdated = now;
                }, f -> {
                    setMessage(null);
                    send(false);
                });
    }

    private void startUpdatingTask() {
        if (this.scheduler.isShutdown()) {
            this.scheduler = Executors.newScheduledThreadPool(1, s ->
                    new Thread(s, "Lavalink-G-" + mg.getGuildId()));
        }

        future = scheduler.scheduleAtFixedRate(
                this::updateEmbed, MESSAGE_UPDATE_INTERVAL, MESSAGE_UPDATE_INTERVAL, TimeUnit.SECONDS
        );
    }

    public void stopUpdatingPlayerMessage() {
        if (this.future != null && !this.future.isCancelled()) {
            this.future.cancel(true);
            this.future = null;
        }
    }

    private void setMessage(Message message) {
        this.message = message;
    }

    public void setLastPlayCommandText(MessageChannelUnion channel) {
        this.lastPlayCommandText = channel;
    }

    public void finish() {
        stopUpdatingPlayerMessage();

        if (message != null)
            message.delete().queue(s -> setMessage(null), f -> {});

        this.scheduler.shutdown();
    }
}
