package pro.melodara.music;

public enum RepeatType {
    NONE ("NONE"),
    TRACK ("TRACK"),
    QUEUE ("QUEUE");

    private final String name;

    RepeatType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
