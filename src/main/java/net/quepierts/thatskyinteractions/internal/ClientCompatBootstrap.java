package net.quepierts.thatskyinteractions.internal;

import dev.anvilcraft.lib.v2.rendering.ALRPipelines;
import dev.anvilcraft.lib.v2.rendering.sdf.SdfGraphics;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
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
            KeyBindingHelper.registerKeyBinding(key);
        }

        var atlasEvent = new RegisterTextureAtlasesEvent();
        NeoForge.EVENTS.post(atlasEvent);
        atlasEvent.publish();

        var particleEvent = new RegisterParticleProvidersEvent();
        NeoForge.EVENTS.post(particleEvent);
        for (var entry : particleEvent.getProviders()) {
            ParticleFactoryRegistry.getInstance().register(entry.type(), (net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory) entry.provider()::apply);
        }

        var guiLayerEvent = new RegisterGuiLayersEvent();
        NeoForge.EVENTS.post(guiLayerEvent);
        for (var entry : guiLayerEvent.getEntries()) {
            // registerAboveAll 等价：挂在最顶层 vanilla HUD 元素之后
            // 1.20.1 没有 HUD 元素注册表，用 HudRenderCallback（在原版 HUD 之后绘制）
            final var layer = entry.layer();
            HudRenderCallback.EVENT.register((graphics, tickDelta) -> layer.render(graphics, tickDelta));
        }

        // 顶层绘制（26.x 挂在 GameRenderer#extractGui 的最后一步，1.20.1 用 HUD 回调）
        HudRenderCallback.EVENT.register(net.quepierts.thatskyinteractions.feature.client.gui.layer.GameLayerHook::onRenderLayer);

        // ---- SDF 着色器注册 ----
        ALRPipelines.register();

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
