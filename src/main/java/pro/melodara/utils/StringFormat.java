package pro.melodara.utils;

import java.time.Duration;

public class StringFormat {
    public static String limitString(int maxCharsCount, String string) {
        if (string.length() > maxCharsCount) {
            return string.substring(0, maxCharsCount - 3) + "...";
        }
        return string;
    }

    public static String getDurationWithNames(long duration) {
        Duration d = Duration.ofMillis(duration);
        long hours = d.toHours();
        long minutes = d.toMinutes() % 60;
        long seconds = d.getSeconds() % 60;

        return (hours == 0 ? "" : (hours + " hours ")) +
                (minutes == 0 ? "" : (minutes + " minutes ")) +
                seconds + " seconds";
    }

    public static String getDuration(long duration) {
        Duration d = Duration.ofMillis(duration);
        long hours = d.toHours();
        long minutes = d.toMinutes() % 60;
        long seconds = d.getSeconds() % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
