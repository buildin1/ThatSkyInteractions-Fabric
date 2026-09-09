package net.quepierts.thatskyinteractions.internal;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;


import net.neoforged.bus.api.Event;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.ServerHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Fabric 移植引导：
 * 1. 扫描本 mod 中带 @EventBusSubscriber 的类并注册到兼容事件总线；
 * 2. 触发平台注册类事件（网络包注册等）；
 * 3. 把 Fabric 生命周期事件映射为 NeoForge 事件发到总线。
 */
public final class CompatBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger("tsi-compat");
    private static final String MODID = "thatskyinteractions";
    private static final String BASE_PACKAGE = "net.quepierts.thatskyinteractions";

    private static boolean bootstrapped = false;
    private static final java.util.concurrent.atomic.AtomicBoolean RELOAD_LISTENERS_REGISTERED = new java.util.concurrent.atomic.AtomicBoolean(false);

    private CompatBootstrap() {}

    public static void bootstrapCommon() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;

        ServerHolder.init();
        registerSubscribers();

        // 网络包注册（RegisterPayloadHandlersEvent 等价）
        NeoForge.EVENTS.post(new net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent());

        wireServerEvents();
    }

    /**
     * 扫描 jar 中带 @EventBusSubscriber(modid=thatskyinteractions) 的类。
     *
     * <p><b>顺序必须确定。</b>监听器的注册顺序决定了同优先级监听器的调用顺序，而本 mod 有多处
     * 依赖它：{@code RegisterSyncManagerEvent} 决定 {@code DataSyncSystem.MANAGERS} 的下标，
     * 该下标又是 {@code SyncDatapackPacket} 的 id——两端顺序不一致就会把数据解到错误的管理器上
     * （与网络包注册顺序必须两端一致是同一类问题）。{@code Files.walk} 与
     * {@code Class#getDeclaredMethods} 都不保证顺序，所以这里按类名排序后再注册。
     */
    public static List<Class<?>> findSubscriberClasses() {
        List<String> classNames = new ArrayList<>();
        try {
            var container = FabricLoader.getInstance().getModContainer(MODID).orElseThrow();
            for (Path root : container.getRootPaths()) {
                Path pkgDir = root.resolve(BASE_PACKAGE.replace('.', '/'));
                if (!Files.exists(pkgDir) || !Files.isDirectory(pkgDir)) continue;
                try (var stream = Files.walk(pkgDir)) {
                    stream.filter(p -> p.toString().endsWith(".class")).forEach(p -> {
                        String relative = root.relativize(p).toString().replace('\\', '/');
                        classNames.add(relative.replace('/', '.').substring(0, relative.length() - ".class".length()));
                    });
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to scan @EventBusSubscriber classes", e);
        }

        java.util.Collections.sort(classNames);

        List<Class<?>> result = new ArrayList<>();
        for (String className : classNames) {
            Class<?> clazz;
            try {
                clazz = Class.forName(className, false, CompatBootstrap.class.getClassLoader());
            } catch (Throwable t) {
                // 客户端专属类在专用服务端上会被 Fabric 剥离，属正常情况；其余记录下来便于排查
                LOGGER.debug("Skipped class {} while scanning subscribers: {}", className, t.toString());
                continue;
            }

            EventBusSubscriber annotation;
            try {
                annotation = clazz.getAnnotation(EventBusSubscriber.class);
            } catch (Throwable t) {
                LOGGER.warn("Failed to read @EventBusSubscriber on {}", className, t);
                continue;
            }

            if (annotation == null) {
                continue;
            }
            // modid 过滤（NeoForge 语义：modid 或 value 二选一）
            boolean modidMatches = MODID.equals(annotation.modid())
                    || (annotation.modid().isEmpty() && annotation.value().length == 0);
            if (!modidMatches) {
                continue;
            }
            // dist 过滤：仅客户端订阅者在服务端不加载
            var dists = annotation.value().length > 0 ? annotation.value() : annotation.dist();
            if (dists.length > 0 && FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.SERVER
                    && java.util.Arrays.stream(dists).noneMatch(d -> d == net.neoforged.api.distmarker.Dist.DEDICATED_SERVER)) {
                continue;
            }
            result.add(clazz);
        }
        return result;
    }

    private static void registerSubscribers() {
        int count = 0;
        for (Class<?> clazz : findSubscriberClasses()) {
            try {
                NeoForge.EVENTS.register(clazz);
                count++;
            } catch (Exception e) {
                LOGGER.error("Failed to register subscriber {}", clazz.getName(), e);
            }
        }
        LOGGER.info("Registered {} @EventBusSubscriber classes (deterministic order)", count);
    }

    private static void wireServerEvents() {
        // 命令注册
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, commandSelection) ->
                NeoForge.EVENTS.post(new RegisterCommandsEvent(dispatcher, buildContext, commandSelection)));

        // 玩家退出
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                NeoForge.EVENTS.post(new PlayerEvent.PlayerLoggedOutEvent(handler.getPlayer())));

        // 实体交互（客户端与服务端各发一次，与 NeoForge 行为一致）
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            var event = new PlayerInteractEvent.EntityInteract(player, level, entity, hand);
            NeoForge.EVENTS.post(event);
            return event.isCanceled()
                    ? net.minecraft.world.InteractionResult.FAIL
                    : net.minecraft.world.InteractionResult.PASS;
        });

        // 数据包重载监听在 mod 初始化阶段注册（见 registerDataReloadListeners），
        // Fabric 在服务器构造时就固定监听器列表，SERVER_STARTING 阶段注册已太晚。

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                NeoForge.EVENTS.post(new OnDatapackSyncEvent(server, handler.getPlayer(), server.reloadableRegistries())));
    }

    /**
     * 注册数据包重载监听器（必须在 mod 初始化阶段调用，即 ResourceManager 构建之前）。
     * 等价 NeoForge 的 AddServerReloadListenersEvent。
     */
    public static void registerDataReloadListeners() {
        if (!RELOAD_LISTENERS_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        var reloadEvent = new AddServerReloadListenersEvent(null);
        NeoForge.EVENTS.post(reloadEvent);

        // NeoForge 按 addListener 的调用顺序执行监听器，而 DataSyncSystem 是按拓扑排序后的
        // MANAGERS 顺序添加的（interaction → expression → animation）。Fabric 不保证注册顺序，
        // 未声明依赖时它按 id 字母序跑，正好把这条链倒过来，导致：
        //   animation/definition 先跑 → RegisterPlayerAnimationEvent 触发时 interaction/expression
        //   还没加载 → 所有交互动画（*.requester / *.receiver）一条都生成不出来。
        // 因此这里把「前一个必须先于后一个」显式声明给 Fabric，还原 NeoForge 的执行顺序。
        var helper      = ResourceManagerHelper.get(PackType.SERVER_DATA);
        var order       = new ArrayList<Identifier>();
        Identifier previous = null;

        for (var entry : reloadEvent.getListeners()) {
            helper.registerReloadListener(new SimpleFabricReloadListener(
                    entry.identifier(),
                    entry.listener(),
                    previous == null ? List.of() : List.of(previous)
            ));
            order.add(entry.identifier());
            previous = entry.identifier();
        }

        // 重载完成后的全体同步（NeoForge 在 datapack sync 时对所有玩家触发）——必须排在最后，
        // 否则同步出去的是尚未加载完的缓存。
        var syncId      = Identifier.fromNamespaceAndPath(MODID, "datapack_sync");
        helper.registerReloadListener(new SimpleFabricReloadListener(
                syncId,
                simpleListener(),
                previous == null ? List.of() : List.of(previous)
        ));
        order.add(syncId);

        LOGGER.info("Registered {} data reload listeners in order: {}", order.size(), order);
    }

    private static PreparableReloadListener simpleListener() {
        return new PreparableReloadListener() {
            @Override
            public CompletableFuture<Void> reload(SharedState currentReload, Executor taskExecutor, PreparationBarrier barrier, Executor reloadExecutor) {
                return barrier.wait(null).thenRun(() -> {
                    MinecraftServer server = ServerHolder.get();
                    if (server != null) {
                        NeoForge.EVENTS.post(new OnDatapackSyncEvent(server, null, server.reloadableRegistries()));
                    }
                });
            }
        };
    }
}
