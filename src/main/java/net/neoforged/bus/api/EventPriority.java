package net.neoforged.bus.api;

public enum EventPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST;

    private final int index;

    EventPriority() {
        this.index = ordinal();
    }

    public int getIndex() {
        return this.index;
    }
}
