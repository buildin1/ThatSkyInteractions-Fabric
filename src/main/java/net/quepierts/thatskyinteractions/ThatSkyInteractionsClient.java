package net.quepierts.thatskyinteractions;

import net.fabricmc.api.ClientModInitializer;
import net.quepierts.thatskyinteractions.internal.ClientCompatBootstrap;

public class ThatSkyInteractionsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCompatBootstrap.bootstrapClient();
    }
}
