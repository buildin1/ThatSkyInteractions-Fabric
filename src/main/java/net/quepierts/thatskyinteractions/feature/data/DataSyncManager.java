package net.quepierts.thatskyinteractions.feature.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import dev.anvilcraft.lib.v2.network.codec.RegistryFriendlyByteBuf;
import dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.data.packet.PacketCache;
import net.quepierts.thatskyinteractions.feature.data.packet.SyncDatapackPacket;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.function.IntFunction;

public abstract class DataSyncManager<T> extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    /** 1.20.1 的 SimpleJsonResourceReloadListener 不是泛型、也不做解码，解码由本类自行完成。 */
    private final Codec<T> elementCodec;

    @Getter
    private final ResourceLocation identifier;

    @Getter
    private final PacketCache cache;

    @Getter
    private final StreamCodec<? super RegistryFriendlyByteBuf, Map<ResourceLocation, T>> streamCodec;

    protected DataSyncManager(
            final Codec<T>                                          codec,
            final StreamCodec<? super RegistryFriendlyByteBuf, T>   streamCodec,
            final String                                            folder
    ) {
        super(GSON, folder);
        this.elementCodec   = codec;
        this.identifier     = ThatSkyInteractions.location(folder);
        this.cache          = new PacketCache();

        this.streamCodec    = createStreamCodec(streamCodec);
    }

    @Override
    protected final void apply(
            final Map<ResourceLocation, JsonElement>    jsons,
            final @NonNull ResourceManager              manager,
            final @NonNull ProfilerFiller               filler
    ) {
        final Map<ResourceLocation, T> preparations = new Object2ObjectOpenHashMap<>();

        jsons.forEach((id, json) -> this.elementCodec
                .parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> org.slf4j.LoggerFactory.getLogger("tsi-data")
                        .error("Failed to parse {}: {}", id, error))
                .ifPresent(value -> preparations.put(id, value)));

        final Map<ResourceLocation, T> payload = this.onHostLoaded(preparations);
        this.apply(payload);

        this.cache.cache(
                this.getStreamCodec(),
                payload
        );
    }

    void handle(@NonNull SyncDatapackPacket packet) {
        final var decode = packet.cache().decode(this.getStreamCodec());
        this.apply(decode);
        this.onSynced();
    }

    protected abstract void apply(@NonNull Map<ResourceLocation, T> preparations);

    protected @NonNull Map<ResourceLocation, T> onHostLoaded(@NonNull Map<ResourceLocation, T> preparations) {
        return preparations;
    }

    protected void onSynced() { }

    protected static <T> StreamCodec<? super RegistryFriendlyByteBuf, Map<ResourceLocation, T>> createStreamCodec(
            final @NonNull StreamCodec<? super RegistryFriendlyByteBuf, T> element
    ) {
        return ByteBufCodecs.map(
                (IntFunction<Map<ResourceLocation, T>>) Object2ObjectOpenHashMap::new,
                ByteBufCodecs.STRING_UTF8.map(
                        s -> new ResourceLocation(s),
                        ResourceLocation::toString
                ),
                element
        );
    }
}
