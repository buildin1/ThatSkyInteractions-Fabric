package net.neoforged.neoforge.client.event;

import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class RegisterKeyMappingsEvent extends Event {

    private final List<KeyMapping> keyMappings = new ArrayList<>();

    public void register(KeyMapping key) {
        this.keyMappings.add(key);
    }

    /** 1.20.1 的按键分类就是一个字符串，不需要显式注册；保留空实现以维持调用点不变。 */
    public void registerCategory(String category) {
    }

    public List<KeyMapping> getKeyMappings() {
        return this.keyMappings;
    }
}
