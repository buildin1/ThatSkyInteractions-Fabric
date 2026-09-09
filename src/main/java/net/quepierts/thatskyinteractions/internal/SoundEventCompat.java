package net.quepierts.thatskyinteractions.internal;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * NeoForge 的 SoundEvent.CODEC 语义等价物：接受 id 字符串或 {sound_id, range} 对象，
 * 返回 Holder&lt;SoundEvent&gt;。原版 26.1 的 SoundEvent.CODEC 是注册表引用编解码器，
 * 需要 RegistryOps，而数据包加载走的是无注册表的 JsonOps，因此无法解析字符串形式。
 */
public final class SoundEventCompat {

    public static final Codec<Holder<SoundEvent>> CODEC = Codec.either(Identifier.CODEC, SoundEvent.DIRECT_CODEC)
            .xmap(
                    either -> either.map(
                            id -> Holder.<SoundEvent>direct(SoundEvent.createVariableRangeEvent(id)),
                            Holder::direct
                    ),
                    holder -> holder.unwrap().map(
                            key -> com.mojang.datafixers.util.Either.<Identifier, SoundEvent>left(key.identifier()),
                            sound -> com.mojang.datafixers.util.Either.<Identifier, SoundEvent>right(sound)
                    )
            );

    private SoundEventCompat() {}
}
