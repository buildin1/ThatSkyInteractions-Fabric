package net.neoforged.neoforge.client.event;

import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;

public class RegisterTextureAtlasesEvent extends Event {

    private final List<AtlasManager.AtlasConfig> atlases = new ArrayList<>();

    public void register(AtlasManager.AtlasConfig config) {
        this.atlases.add(config);
    }

    public List<AtlasManager.AtlasConfig> getAtlases() {
        return this.atlases;
    }
}
