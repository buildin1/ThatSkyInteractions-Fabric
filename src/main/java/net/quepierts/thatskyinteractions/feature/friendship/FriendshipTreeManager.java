package net.quepierts.thatskyinteractions.feature.friendship;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.data.DataSyncManager;
import net.quepierts.thatskyinteractions.feature.data.event.RegisterSyncManagerEvent;
import org.jspecify.annotations.NonNull;

import java.util.Map;

@Slf4j
@EventBusSubscriber(modid = ThatSkyInteractions.MODID)
public final class FriendshipTreeManager extends DataSyncManager<FriendshipTreeFile> {

    public static final String FOLDER = "friendship/tree";

    @Getter
    private static final FriendshipTreeManager instance
            = new FriendshipTreeManager();

    private Map<ResourceLocation, FriendshipTree> trees = Map.of();

    FriendshipTreeManager() {
        super(
                FriendshipTreeFile.CODEC,
                FriendshipTreeFile.STREAM_CODEC,
                FOLDER
        );
    }

    @SubscribeEvent
    public static void onRegisterSyncManager(final RegisterSyncManagerEvent event) {
        event.register(instance);
    }

    @Override
    protected void apply(final @NonNull Map<ResourceLocation, FriendshipTreeFile> preparations) {
        final var builder = new FriendshipTreeBuilder();
        builder.addAll(preparations);
        this.trees = builder.build();

        log.info("Loaded {} friendship trees", this.trees.size());
    }

    public FriendshipTree get(final @NonNull ResourceLocation identifier) {
        return this.trees.get(identifier);
    }

}
