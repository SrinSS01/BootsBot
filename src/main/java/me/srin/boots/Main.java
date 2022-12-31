package me.srin.boots;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.*;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.SCHEDULED_EVENTS;

public final class Main extends ListenerAdapter {
    private static final Main INSTANCE = new Main();
    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws InterruptedException {
        String token = "OTcxMzkzMTEwNjkxMTc2NDU4.GnK6Ga.54MmlJ6HQ4mLJGjsFIsXCHIH3g2MnjfZUECH58";
        var jda = JDABuilder.createDefault(
                token
        ).setEnabledIntents(
                GUILD_MESSAGES,
                GUILD_MESSAGE_REACTIONS,
                GUILD_VOICE_STATES
        ).addEventListeners(INSTANCE)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("meow meow"))
                .disableCache(VOICE_STATE, EMOJI, STICKER, SCHEDULED_EVENTS)
                .build();
        for (Guild guild : jda.awaitReady().getGuilds()) {
            guild.upsertCommand("boots", "shows random boots pics")
                    .addOption(OptionType.INTEGER, "number", "any integer number between 1 to 45",false)
                    .queue();
        }
        var consoleThread = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while(true) try {
                if (sc.nextLine().equals("stop")) {
                    jda.shutdown();
                    break;
                }
            } catch(NoSuchElementException ignored) {}
        }, "consoleThread");
        consoleThread.start();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LOGGER.info("%s is ready".formatted(event.getJDA().getSelfUser().getName()));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String bootsUrl = "https://cdn.fundy.tv/Boots/Boots%d.jpg";
        if (!Objects.requireNonNull(event.getGuild())
                .getSelfMember()
                .hasPermission(event.getGuildChannel(), Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY)) {
            event.reply("sowwy I don't have permissions to send message here ;w;").setEphemeral(true).queue();
            return;
        }
        if (event.getName().equals("boots")) {
            var numberOption = event.getOption("number");
            int number = RANDOM.nextInt(1, 46);
            if (numberOption != null) {
                String numberString = numberOption.getAsString();
                if (!numberString.matches("[1-9]|([1-3]\\d)|4[0-5]")) {
                    event.reply("enter a valid range \uD83D\uDE41").setEphemeral(true).queue();
                    return;
                }
                number = Integer.parseInt(numberString);
            }
            event.reply(bootsUrl.formatted(number)).queue(it -> it.retrieveOriginal().queue(message -> {
                message.addReaction(Emoji.fromFormatted("<bootsHi:971428204181069834>")).queue();
                message.addReaction(Emoji.fromFormatted("<fundyLove:971428204130729984>")).queue();
                message.addReaction(Emoji.fromFormatted("<fundyFeelsGood:760830070842458123>")).queue();
            }));
        }
    }
}
