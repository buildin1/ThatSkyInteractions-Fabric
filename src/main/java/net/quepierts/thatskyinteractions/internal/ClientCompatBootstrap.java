package net.quepierts.thatskyinteractions.internal;

import dev.anvilcraft.lib.v2.rendering.ALRPipelines;
import dev.anvilcraft.lib.v2.rendering.sdf.SdfGraphics;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.AtlasRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Fabric 客户端引导：触发客户端注册类事件并把 Fabric 客户端事件映射到兼容总线。
 */
public final class ClientCompatBootstrap {

    private ClientCompatBootstrap() {}

    public static void bootstrapClient() {
        CompatBootstrap.bootstrapCommon();

        // ---- 注册类事件（与 NeoForge mod-bus 事件等价） ----
        var keyEvent = new RegisterKeyMappingsEvent();
        NeoForge.EVENTS.post(keyEvent);
        for (var key : keyEvent.getKeyMappings()) {
            KeyMappingHelper.registerKeyMapping(key);
        }

        var atlasEvent = new RegisterTextureAtlasesEvent();
        NeoForge.EVENTS.post(atlasEvent);
        for (var config : atlasEvent.getAtlases()) {
            AtlasRegistry.register(config);
        }

        var particleEvent = new RegisterParticleProvidersEvent();
        NeoForge.EVENTS.post(particleEvent);
        for (var entry : particleEvent.getProviders()) {
            ParticleProviderRegistry.getInstance().register(entry.type(), (net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.PendingParticleProvider) entry.provider()::apply);
        }

        var guiLayerEvent = new RegisterGuiLayersEvent();
        NeoForge.EVENTS.post(guiLayerEvent);
        for (var entry : guiLayerEvent.getEntries()) {
            // registerAboveAll 等价：挂在最顶层 vanilla HUD 元素之后
            HudElementRegistry.attachElementAfter(
                    VanillaHudElements.MISC_OVERLAYS,
                    entry.id(),
                    (graphics, deltaTracker) -> entry.layer().render(graphics, deltaTracker)
            );
        }

        // ---- SDF 渲染管线注册 ----
        ALRPipelines.on(RenderPipelines::register);

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            SdfGraphics.init();
            verifyKeyMappings(client);
        });

        // ---- 客户端事件映射 ----
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            NeoForge.EVENTS.post(new ClientTickEvent.Pre());
            net.quepierts.thatskyinteractions.feature.client.ClientNearbyInteractionPrompt.tick(client);
            net.quepierts.thatskyinteractions.feature.client.ClientInteractionFeedback.tick(client);
            net.quepierts.thatskyinteractions.feature.client.ClientCameraEffects.tick(client);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            net.quepierts.thatskyinteractions.feature.client.ClientNearbyInteractionPrompt.clear();
            NeoForge.EVENTS.post(new ClientPlayerNetworkEvent.LoggingOut());
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            var event = new ClientChatEvent(message);
            NeoForge.EVENTS.post(event);
            return !event.isCanceled();
        });
    }

    /** 启动自检：确认本 mod 的按键已进入游戏选项（便于排查按键不显示问题） */
    private static void verifyKeyMappings(net.minecraft.client.Minecraft client) {
        var ours = new String[]{
                "key.thatskyinteractions.interact",
                "key.thatskyinteractions.unlock_camera",
                "key.thatskyinteractions.open_expression"
        };
        java.util.List<String> found = new java.util.ArrayList<>();
        for (var mapping : client.options.keyMappings) {
            for (String name : ours) {
                if (name.equals(mapping.getName())) {
                    found.add(name);
                }
            }
        }
        var logger = org.slf4j.LoggerFactory.getLogger("tsi-compat");
        logger.info("Key mappings present: {}/{} {}", found.size(), ours.length, found);
    }
}
