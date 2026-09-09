package net.neoforged.bus.api;

/**
 * NeoForge API 兼容层：可取消事件。
 * 默认实现委托给 Event 的 final 内部方法，事件类可以安全地重写 setCanceled 并调用
 * {@code ICancellableEvent.super.setCanceled(...)}。
 */
public interface ICancellableEvent {

    default boolean isCanceled() {
        return ((Event) this).isCanceledInternal();
    }

    default void setCanceled(boolean canceled) {
        ((Event) this).setCanceledInternal(canceled);
    }
}
