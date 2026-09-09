package net.quepierts.thatskyinteractions.infra.animation.tween.backend.task;

import net.quepierts.thatskyinteractions.infra.animation.tween.ease.Ease;
import net.quepierts.thatskyinteractions.infra.animation.tween.interpolate.Interpolator;

import java.util.function.Consumer;

public class TweenTask<T> extends DefaultTimingTask {

    private final T from;
    private final T to;

    private final Ease ease;

    private final Interpolator<T> interpolator;

    private final Consumer<T> consumer;

    public TweenTask(
            final float             duration,
            final T                 from,
            final T                 to,
            final Ease              ease,
            final Interpolator<T>   interpolator,
            final Consumer<T>       consumer
    ) {
        super(duration);
        this.from                   = from;
        this.to                     = to;
        this.ease                   = ease;
        this.interpolator           = interpolator;
        this.consumer               = consumer;
    }

    @Override
    protected void _update(final float delta) {
        final var t                 = Math.min(this.elapsed / this.duration, 1.0f);
        final var eased             = this.ease.map(t);

        final var v                 = this.interpolator.interpolate(this.from, this.to, eased);
        this.consumer               .accept(v);
    }
}
