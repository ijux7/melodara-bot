package pro.melodara.utils.music.player;

import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.Nullable;
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

    private MessageEmbed getEmbed(boolean isNew, @Nullable LavalinkPlayer player) {
        Track track = mg.getQueueManager().getCurrentTrack();
        EmbedBuilder embed = new EmbedBuilder();
        List<Track> nextTracks = mg.getQueueManager().getQueue().stream().toList();

        long trackPosition = (isNew || player == null) ? 0L : player.getPosition();

        embed.setTitle(StringFormat.limitString(50, track.getInfo().getTitle()));
        embed.setUrl(track.getInfo().getUri());
        embed.setThumbnail(track.getInfo().getArtworkUrl());
        embed.addField(new MessageEmbed.Field(
                "Duration",
                getProgressBar(
                        isNew ? 0L : trackPosition, track.getInfo().getLength()
                ) + "\n" +
                        StringFormat.getDuration(trackPosition) + " — " +
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
                        " | Source: " + track.getInfo().getSourceName() + " | Embed updates every 20 seconds",
                nextTracks.isEmpty() ? null : nextTracks.get(0).getInfo().getArtworkUrl()
        );

        return embed.build();
    }

    private Collection<ActionRow> getActionRows(@Nullable LavalinkPlayer player) {
        Track track = mg.getQueueManager().getCurrentTrack();

        return List.of(
                ActionRow.of(
                        Button.secondary("prev", Emoji.fromFormatted("<:arrowleft:1279770369947074560>"))
                                .withDisabled(true),
                        Button.secondary("minus15s", Emoji.fromFormatted("<:minus15s:1279773771783475220>"))
                                .withDisabled(player == null || player.getPosition() < 20000L),
                        Button.secondary("pause",
                                (player != null && player.getPaused()) ?
                                        Emoji.fromFormatted("<:pause:1279770388557074494>") :
                                        Emoji.fromFormatted("<:play:1279771723104915537>")
                        ),
                        Button.secondary("plus15s", Emoji.fromFormatted("<:plus15s:1279774025320497325>"))
                                .withDisabled(!(track.getInfo().getLength() > 20000L)),
                        Button.secondary("next", Emoji.fromFormatted("<:arrowright:1279770380462194698>"))
                                .withDisabled(mg.getQueueManager().getQueue().isEmpty())
                )
        );
    }

    private String getProgressBar(long pos, long max) {
        StringBuilder builder = new StringBuilder();//

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

        mg.getPlayer().ifPresentOrElse(
                player -> lastPlayCommandText.sendMessageEmbeds(getEmbed(isNew, player))
                        .setComponents(getActionRows(player))
                        .queue(s -> {
                            setMessage(s);
                            startUpdatingTask();
                        }, f -> {}),
                () -> lastPlayCommandText.sendMessageEmbeds(getEmbed(isNew, null))
                        .setComponents(getActionRows(null))
                        .queue(s -> {
                            setMessage(s);
                            startUpdatingTask();
                        }, f -> {})
        );
    }

    public void updateEmbed() {
        updateEmbed(false);
    }

    public void updateEmbed(boolean shouldUpdate) {
        if (message == null)
            return;

        Instant now = Instant.now();

        if (!shouldUpdate && lastTimeMessageUpdated != null &&
                (int) now.getEpochSecond() - lastTimeMessageUpdated.getEpochSecond() < MESSAGE_UPDATE_INTERVAL)
            return;

        mg.getPlayer().ifPresent(player ->
                message.editMessageEmbeds(getEmbed(false, player))
                .setComponents(getActionRows(player))
                .queue(s -> {
                    setMessage(s);
                    lastTimeMessageUpdated = now;
                }, f -> {
                    setMessage(null);
                    send(false);
                })
        );
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

    public Message getMessage() {
        return message;
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
