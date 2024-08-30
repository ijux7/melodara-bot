package pro.melodara.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import pro.melodara.utils.commands.CommandSample;

public class TestBro extends CommandSample {
    public TestBro() {
        this.name = "testbro";
        this.description = "sassasdas";
        this.children.add(new Konch());
    }

    public static class Konch extends CommandSample {
        public Konch() {
            this.name = "konch";
            this.description = "aaaaa";
            this.children.add(new HUI());
            this.children.add(new pizda());
        }

        public static class HUI extends CommandSample {
            public HUI() {
                this.name = "hui";
                this.description = "bbbb";
            }

            @Override
            public void run(SlashCommandInteractionEvent event) {
                event.reply("hui").queue();
            }
        }

        public static class pizda extends CommandSample {
            public pizda() {
                this.name = "pizda";
                this.description = "pizdaa";
            }

            @Override
            public void run(SlashCommandInteractionEvent event) {
                event.reply("pizdaaa").queue();
            }
        }
    }
}
