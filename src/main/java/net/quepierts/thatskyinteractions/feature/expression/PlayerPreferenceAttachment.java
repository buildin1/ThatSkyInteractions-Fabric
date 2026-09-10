package net.quepierts.thatskyinteractions.feature.expression;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import dev.anvilcraft.lib.v2.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.feature.expression.call.PlayerVoiceType;
import net.quepierts.thatskyinteractions.feature.expression.call.PlayerVoiceTypeManager;
import net.quepierts.thatskyinteractions.feature.registry.AttachmentTypes;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Getter
public final class PlayerPreferenceAttachment {

    public static final MapCodec<PlayerPreferenceAttachment> MAP_CODEC
            = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("voice", PlayerVoiceType.DEFAULT_ID).forGetter(PlayerPreferenceAttachment::getVoice)
            ).apply(instance, PlayerPreferenceAttachment::new));

    public static final StreamCodec<ByteBuf, PlayerPreferenceAttachment> STREAM_CODEC
            = StreamCodec.composite(
                    dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.RESOURCE_LOCATION,
                    PlayerPreferenceAttachment::getVoice,
                    PlayerPreferenceAttachment::new
            );

    public static PlayerPreferenceAttachment getAttachment(final @NonNull Player player) {
        return ((net.neoforged.neoforge.attachment.IAttachmentHolder) player).getData(AttachmentTypes.PLAYER_PREFERENCE);
    }

    private @NonNull ResourceLocation voice;

    public PlayerPreferenceAttachment() {
        this.voice = PlayerVoiceType.DEFAULT_ID;
    }

    public void setVoice(
            final @Nullable ResourceLocation voice
    ) {
        this.voice = voice == null || voice.equals(PlayerVoiceType.DEFAULT_ID)
                ? PlayerVoiceType.DEFAULT_ID
                : voice;
    }

    private PlayerPreferenceAttachment(
            final @NonNull ResourceLocation voice
    ) {
        this.voice = voice.equals(PlayerVoiceType.DEFAULT_ID)
                ? PlayerVoiceType.DEFAULT_ID
                : voice;
    }

    public boolean shouldSerialize() {
        return this.voice != PlayerVoiceType.DEFAULT_ID;
    }

    public @NonNull PlayerVoiceType getVoiceType() {
        return PlayerVoiceTypeManager.getInstance().getNonNull(this.voice);
    }

}
