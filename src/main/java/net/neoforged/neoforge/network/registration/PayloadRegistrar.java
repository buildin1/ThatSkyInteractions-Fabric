package net.neoforged.neoforge.network.registration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * NeoForge API 兼容层：payload 注册器。
 * 收集注册回调，由兼容层统一在 Fabric PayloadTypeRegistry 上执行。
 */
public final class PayloadRegistrar {

    private final String version;
    final List<BiConsumer<net.minecraft.network.RegistryFriendlyByteBuf, ?>> entries = new ArrayList<>();

    public PayloadRegistrar(String version) {
        this.version = version;
    }

    public String version() {
        return this.version;
    }
}
