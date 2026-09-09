package net.neoforged.bus.api;

/**
 * NeoForge API 兼容层：事件基类（取消状态由基类持有，与 NeoForge 行为一致）。
 * 注意：取消状态的读写走 final 内部方法，避免与事件类的 setCanceled 重写形成递归。
 */
public abstract class Event {

    private boolean canceled = false;

    public boolean isCancelable() {
        return this instanceof ICancellableEvent;
    }

    public void setCanceled(boolean canceled) {
        if (!(this instanceof ICancellableEvent)) {
            throw new UnsupportedOperationException("Attempted to cancel a non-cancelable event: " + this.getClass().getName());
        }
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    /** 供 {@link ICancellableEvent} 默认方法使用，不可被事件类重写（避免递归） */
    public final void setCanceledInternal(boolean canceled) {
        this.canceled = canceled;
    }

    /** 供 {@link ICancellableEvent} 默认方法使用，不可被事件类重写（避免递归） */
    public final boolean isCanceledInternal() {
        return this.canceled;
    }
}
