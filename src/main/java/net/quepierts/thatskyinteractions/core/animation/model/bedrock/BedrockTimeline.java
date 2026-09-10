package net.quepierts.thatskyinteractions.core.animation.model.bedrock;

import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Map;

public record BedrockTimeline(
        Map<Float, BedrockKeyframe> keyframes
) {

    public static BedrockTimeline of(float value) {
        var vector      = new Vector3f(value);
        return          BedrockTimeline.of(vector);
    }

    public static BedrockTimeline of(Vector3fc vector) {
        // 1.20.1 打包的 fastutil 版本没有 Float2ObjectMap.entry / ofEntries，直接建 map
        final var keyframe = BedrockKeyframe.of(vector);
        final var map      = new it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap<BedrockKeyframe>();
        map.put(0.0f, keyframe);

        return          new BedrockTimeline(map);
    }

}
