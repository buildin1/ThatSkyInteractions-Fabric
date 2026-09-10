package net.quepierts.thatskyinteractions.internal;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * NeoForge 的 SoundEvent.CODEC 语义等价物：接受 id 字符串或 {sound_id, range} 对象，
 * 返回 Holder&lt;SoundEvent&gt;。原版 26.1 的 SoundEvent.CODEC 是注册表引用编解码器，
 * 需要 RegistryOps，而数据包加载走的是无注册表的 JsonOps，因此无法解析字符串形式。
 */
public final class SoundEventCompat {

    public static final Codec<Holder<SoundEvent>> CODEC = Codec.either(ResourceLocation.CODEC, SoundEvent.DIRECT_CODEC)
            .xmap(
                    either -> either.map(
                            id -> Holder.<SoundEvent>direct(SoundEvent.createVariableRangeEvent(id)),
                            Holder::direct
                    ),
                    holder -> holder.unwrap().map(
                            key -> com.mojang.datafixers.util.Either.<ResourceLocation, SoundEvent>left(key.location()),
                            sound -> com.mojang.datafixers.util.Either.<ResourceLocation, SoundEvent>right(sound)
                    )
            );


    /**
     * 对应 26.x 的 {@code SoundEvent.STREAM_CODEC}：网络上只传 id，收端按可变距离事件重建。
     */
    public static final dev.anvilcraft.lib.v2.network.codec.StreamCodec<io.netty.buffer.ByteBuf, Holder<SoundEvent>> STREAM_CODEC
            = dev.anvilcraft.lib.v2.network.codec.ByteBufCodecs.RESOURCE_LOCATION.map(
                    id -> Holder.<SoundEvent>direct(SoundEvent.createVariableRangeEvent(id)),
                    holder -> holder.value().getLocation()
            );

    private SoundEventCompat() {}
}
