package net.neoforged.neoforge.client.event;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 1.20.1 兼容层：图集注册事件。
 *
 * <p>26.x 通过 {@code AtlasManager.AtlasConfig} 在代码里注册图集。1.20.1 的图集集合是
 * {@code ModelManager.VANILLA_ATLASES} 里写死的 {@code Map.of}，光有
 * {@code assets/&lt;ns&gt;/atlases/*.json} 不会被拼图——必须把条目塞进那张表。
 * 所以这里把注册结果收集下来，由 {@code ModelManagerMixin} 在构造 {@code AtlasSet} 时合并进去。
 *
 * <p>键是图集纹理 id（{@code <ns>:textures/atlas/<name>.png}），
 * 值是图集定义 id（{@code <ns>:<name>} → {@code assets/<ns>/atlases/<name>.json}）。
 */
public class RegisterTextureAtlasesEvent extends Event {

    /** 最近一次事件收集到的图集；ModelManager 构造时读取。 */
    private static final Map<ResourceLocation, ResourceLocation> REGISTERED = new LinkedHashMap<>();

    private final Map<ResourceLocation, ResourceLocation> atlases = new LinkedHashMap<>();

    public void register(ResourceLocation atlasTextureId, ResourceLocation atlasInfoId, boolean mipmap) {
        this.atlases.put(atlasTextureId, atlasInfoId);
    }

    public Map<ResourceLocation, ResourceLocation> getAtlases() {
        return this.atlases;
    }

    /** 由 ClientCompatBootstrap 在事件派发后调用。 */
    public void publish() {
        REGISTERED.putAll(this.atlases);
    }

    public static Map<ResourceLocation, ResourceLocation> registered() {
        return REGISTERED;
    }
}
