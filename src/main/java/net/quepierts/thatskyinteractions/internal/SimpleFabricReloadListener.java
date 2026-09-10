package net.quepierts.thatskyinteractions.internal;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 把任意 PreparableReloadListener 适配为带 ResourceLocation 的 Fabric 重载监听器。
 *
 * <p>{@code dependencies} 是本监听器之前必须先跑完的监听器 id。NeoForge 的
 * {@code AddServerReloadListenersEvent} 按添加顺序执行监听器，而 Fabric 只在声明了
 * 依赖时才保证顺序（未声明时按 id 排序）。本 mod 的管理器之间有强制顺序：
 * interaction → expression → animation（后者在 apply 时会向前者收集运行时生成的
 * 表达式/动画），顺序错了会导致交互动画根本不被生成。因此这里必须把顺序显式声明出来。
 */
public record SimpleFabricReloadListener(
        ResourceLocation            identifier,
        PreparableReloadListener    delegate,
        List<ResourceLocation>      dependencies
) implements IdentifiableResourceReloadListener {

    public SimpleFabricReloadListener(ResourceLocation identifier, PreparableReloadListener delegate) {
        this(identifier, delegate, List.of());
    }

    @Override
    public ResourceLocation getFabricId() {
        return this.identifier;
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return this.dependencies;
    }

    @Override
    public CompletableFuture<Void> reload(
            PreparationBarrier  preparationBarrier,
            ResourceManager     resourceManager,
            ProfilerFiller      preparationsProfiler,
            ProfilerFiller      reloadProfiler,
            Executor            backgroundExecutor,
            Executor            gameExecutor
    ) {
        return this.delegate.reload(
                preparationBarrier,
                resourceManager,
                preparationsProfiler,
                reloadProfiler,
                backgroundExecutor,
                gameExecutor
        );
    }
}
