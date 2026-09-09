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

    /** NeoForge API：注册按键分类（原版 26.1 需显式 register 才会进入分类排序表） */
    public void registerCategory(KeyMapping.Category category) {
        try {
            KeyMapping.Category.register(category.id());
        } catch (IllegalArgumentException ignored) {
            // 已注册（例如与其他 mod 共用分类）
        }
    }

    public List<KeyMapping> getKeyMappings() {
        return this.keyMappings;
    }
}
