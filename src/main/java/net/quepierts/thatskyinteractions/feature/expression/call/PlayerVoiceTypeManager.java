package net.quepierts.thatskyinteractions.feature.expression.call;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.data.DataSyncManager;
import net.quepierts.thatskyinteractions.feature.data.event.RegisterSyncManagerEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@EventBusSubscriber(modid = ThatSkyInteractions.MODID)
public final class PlayerVoiceTypeManager extends DataSyncManager<PlayerVoiceType> {

    public static final String FOLDER = "preference/voice";

    @Getter
    private static final PlayerVoiceTypeManager instance
            = new PlayerVoiceTypeManager();

    private @NonNull List<ResourceLocation>                   ordinal = List.of();
    private @NonNull Map<ResourceLocation, PlayerVoiceType>   voices = Map.of();

    PlayerVoiceTypeManager() {
        super(
                PlayerVoiceType.CODEC,
                PlayerVoiceType.STREAM_CODEC,
                FOLDER
        );
    }

    @SubscribeEvent
    public static void onRegisterSyncManager(final RegisterSyncManagerEvent event) {
        event.register(instance);
    }

    @Override
    protected void apply(@NonNull final Map<ResourceLocation, PlayerVoiceType> preparations) {
        final var builder   = ImmutableMap.<ResourceLocation, PlayerVoiceType>builderWithExpectedSize(preparations.size() + 1);
        final var builder1  = ImmutableList.<ResourceLocation>builderWithExpectedSize(preparations.size() + 1);

        builder.put(PlayerVoiceType.DEFAULT_ID, PlayerVoiceType.DEFAULT);
        builder1.add(PlayerVoiceType.DEFAULT_ID);

        preparations
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(
                        entry -> {
                            builder.put(entry);
                            builder1.add(entry.getKey());
                        });

        this.voices     = builder.build();
        this.ordinal    = builder1.build();

        log.info("Loaded {} player voice types", preparations.size());
    }

    public @Nullable PlayerVoiceType get(final @NonNull ResourceLocation identifier) {
        return this.voices.get(identifier);
    }

    public @NonNull PlayerVoiceType getNonNull(final @NonNull ResourceLocation identifier) {
        return this.voices.getOrDefault(identifier, PlayerVoiceType.DEFAULT);
    }

    public @NonNull Collection<ResourceLocation> identifiers() {
        return this.voices.keySet();
    }

    public @NonNull List<ResourceLocation> ordinal() {
        return this.ordinal;
    }

    public boolean has(final @NonNull ResourceLocation identifier) {
        return this.voices.containsKey(identifier);
    }
}
