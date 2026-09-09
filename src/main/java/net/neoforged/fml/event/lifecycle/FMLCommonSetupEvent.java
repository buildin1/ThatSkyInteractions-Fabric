package net.neoforged.fml.event.lifecycle;

import net.neoforged.bus.api.Event;

public final class FMLCommonSetupEvent extends Event {

    public void enqueueWork(Runnable runnable) {
        runnable.run();
    }
}
