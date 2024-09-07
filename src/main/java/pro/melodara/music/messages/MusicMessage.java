package pro.melodara.music.messages;

import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import pro.melodara.Melodara;
import pro.melodara.exceptions.CommandExecutionException;
import pro.melodara.music.MusicManager;
import pro.melodara.utils.StringFormat;

import java.util.*;


public class MusicMessage {
    private final MusicManager musicManager;
    private MessageChannelUnion messageChannel = null;
    private Message currentPlayerMessage = null;

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

        Optional<LavalinkPlayer> playerOptional = musicManager.getPlayerFromLink();

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
                        (tracks.size() > 1 ? " (then " + (tracks.size() - 1) + " tracks)" : "") + "\n" +
                        "- Previous: " + (previousTrack == null ? "No previous track" :
                        "[" + StringFormat.limitString(50, previousTrack.getInfo().getTitle()) + "](" +
                                previousTrack.getInfo().getUri() + ")"),
                false
        ));

        return embed.build();
    }

    private Collection<ActionRow> getActionRows() {
        return getActionRows(false);
    }

    private Collection<ActionRow> getActionRows(boolean starts) {
        Optional<LavalinkPlayer> playerOptional = musicManager.getPlayer();
        if (playerOptional.isEmpty()) return List.of();

        LavalinkPlayer player = playerOptional.get();

        return List.of(
            ActionRow.of(
                    Button.secondary(
                            "previous",
                            Emoji.fromFormatted("<:arrowleft:1279770369947074560>")
                    ),
                    Button.secondary(
                            "minus15s",
                            Emoji.fromFormatted("<:minus15s:1279773771783475220>")
                    ),
                    Button.secondary(
                            "pause",
                            player.getPaused() || starts ?
                                    Emoji.fromFormatted("<:pause:1279770388557074494>") :
                                    Emoji.fromFormatted("<:play:1279771723104915537>")
                    ),
                    Button.secondary(
                            "plus15s",
                            Emoji.fromFormatted("<:plus15s:1279774025320497325>")
                    ),
                    Button.secondary(
                            "next",
                            Emoji.fromFormatted("<:arrowright:1279770380462194698>")
                    )
            )
        );
    }

    public void handleButtons(ButtonInteraction interaction) {
        if (!interaction.getMessage().getId().equals(currentPlayerMessage.getId()))
            return;

        interaction.deferReply(true).queue(s -> {}, f -> {});

        Optional<LavalinkPlayer> playerOptional = musicManager.getPlayerFromLink();

        checkIfNoPlayer(playerOptional.isPresent());

        String buttonId = interaction.getComponent().getId();

        switch (Objects.requireNonNull(buttonId)) {
            case "previous" -> playPreviousTrack(interaction);
            case "plus15s" -> seekPlus15s(interaction, playerOptional.get());
            case "minus15s" -> seekMinus15s(interaction, playerOptional.get());
            case "pause" -> pauseTrack(interaction);
            case "next" -> playNextTrack(interaction);
        }
    }

    private void checkIfNoPlayer(boolean isPlayer) {
        if (!isPlayer)
            throw new CommandExecutionException("Nothing is playing now.");
    }

    private void pauseTrack(ButtonInteraction interaction) {
        this.musicManager.getPlayerFromLink().ifPresent(player -> player.setPaused(!player.getPaused()).subscribe());
        updateMessage();

        interaction.getHook().deleteOriginal().queue(s -> {}, f -> {});
    }

    private void playNextTrack(ButtonInteraction interaction) {
        this.musicManager.getScheduler().skipTrack();

        interaction.getHook().deleteOriginal().queue(s -> {}, f -> {});
    }

    private void playPreviousTrack(ButtonInteraction interaction) {
        this.musicManager.getScheduler().playPreviousTrack();

        interaction.getHook().deleteOriginal().queue(s -> {}, f -> {});
    }

    private void checkIsSeekable(LavalinkPlayer player) {
        if (!Objects.requireNonNull(player.getTrack()).getInfo().isSeekable())
            throw new CommandExecutionException("You cannot seek the track!");
    }

    private void seekPlus15s(ButtonInteraction interaction, LavalinkPlayer player) {
        checkIsSeekable(player);

        player.setPosition(player.getPosition() + 15000L).subscribe();

        interaction.getHook().deleteOriginal().queue(s -> {}, f -> {});
    }

    private void seekMinus15s(ButtonInteraction interaction, LavalinkPlayer player) {
        checkIsSeekable(player);

        player.setPosition(player.getPosition() - 15000L).subscribe();

        interaction.getHook().deleteOriginal().queue(s -> {}, f -> {});
    }

    public void sendMessageWhenStarts() {
        if (messageChannel == null) return;

        if (currentPlayerMessage != null)
            currentPlayerMessage.delete().queue(s -> {}, f -> {});

        MessageChannelUnion channel = currentPlayerMessage == null ?
                messageChannel : currentPlayerMessage.getChannel();

        channel.sendMessageEmbeds(getEmbed()).setComponents(getActionRows(true))
                .queue(s -> currentPlayerMessage = s, f -> {});
    }

    public void updateMessage() {
        if (currentPlayerMessage == null) return;

        currentPlayerMessage.editMessageEmbeds(getEmbed()).setComponents(getActionRows())
                .queue(s -> currentPlayerMessage = s, f -> {});
    }

    public void sendMessage(MessageChannelUnion channel) {
        if (currentPlayerMessage != null)
            currentPlayerMessage.delete().queue(s -> {}, f -> {});

        MessageCreateAction createAction = channel.sendMessageEmbeds(getEmbed());

        if (musicManager.getScheduler().getCurrentTrack() != null) {
            createAction.setComponents(getActionRows());
        }

        createAction.queue(s -> currentPlayerMessage = s, f -> {});
    }

    public void setChannelUnion(MessageChannelUnion messageChannel) {
        this.messageChannel = messageChannel;
    }

    public void delete() {
        if (currentPlayerMessage != null) currentPlayerMessage.delete().queue(s -> {}, f -> {});

        currentPlayerMessage = null;
        messageChannel = null;
    }
}
