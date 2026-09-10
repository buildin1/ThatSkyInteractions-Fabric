package net.quepierts.thatskyinteractions.feature.client.gui.component.visual;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.quepierts.thatskyinteractions.feature.client.gui.ColorStack;
import net.quepierts.thatskyinteractions.feature.client.gui.ExtendedGuiGraphics;
import net.quepierts.thatskyinteractions.feature.client.gui.component.control.Control;
import net.quepierts.thatskyinteractions.infra.animation.tween.TweenScope;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.function.Function;

@FunctionalInterface
public interface VisualNode {

    VisualNode EMPTY = (__unused0, __unused1, __unused2, __unused3, __unused4, __unused5, __unused6) -> {};

    static VisualNode combine(@NonNull VisualNode... nodes) {

        return combine(Combine::of, nodes);

    }

    static VisualNode combine(
            final @NonNull Function<VisualNode[], VisualNode>   factory,
            final @NonNull VisualNode...                        nodes
    ) {
        final var length = nodes.length;
        if (length == 0) {
            return EMPTY;
        } else if (length == 1) {
            return nodes[0];
        }

        final var nonempty = new ArrayList<VisualNode>(length);
        for (final var node : nodes) {
            if (node != EMPTY) {
                nonempty.add(node);
            }
        }

        final var size  = nonempty.size();
        if (size == 0) {
            return EMPTY;
        } else if (size == 1) {
            return nonempty.get(0);
        } else {
            return factory.apply(nonempty.toArray(VisualNode[]::new));
        }
    }

    static VisualNode flatten(@NonNull VisualNode node) {
        if (!(node instanceof Combine combine)) {
            return node;
        }

        final var children      = new ArrayList<VisualNode>();
        final var stack         = new ObjectArrayList<Combine>();
        stack.push(combine);

        while (!stack.isEmpty()) {
            final var current   = stack.pop();
            for (final var child : current.nodes) {
                if (!(child instanceof Combine nest)) {
                    if (child != EMPTY) {
                        children.add(child);
                    }
                    continue;
                }

                stack.push(nest);
            }
        }

        return Combine.of(children.toArray(VisualNode[]::new));
    }

    static void array(
            final @NonNull VisualNode @NonNull[]    nodes,
            final @NonNull Control                  control,
            final @NonNull ExtendedGuiGraphics      graphics,
            final @NonNull ColorStack               colors,
            final @NonNull TweenScope               tween,

            final int                               mouseX,
            final int                               mouseY,
            final float                             delta
    ) {
        final var pose = graphics.pose();
        for (final var node : nodes) {
            pose.pushPose();
            node.extractRenderState(
                    control,
                    graphics,
                    colors,
                    tween,

                    mouseX,
                    mouseY,
                    delta
            );
            pose.popPose();
        }
    }

    void extractRenderState(
            final @NonNull Control              control,
            final @NonNull ExtendedGuiGraphics  graphics,
            final @NonNull ColorStack           colors,
            final @NonNull TweenScope           tween,

            final int                           mouseX,
            final int                           mouseY,
            final float                         delta
    );

    @RequiredArgsConstructor(staticName = "of")
    final class Combine implements VisualNode {

        private final @NonNull VisualNode @NonNull[] nodes;

        @Override
        public void extractRenderState(
                final @NonNull Control              control,
                final @NonNull ExtendedGuiGraphics  graphics,
                final @NonNull ColorStack           colors,
                final @NonNull TweenScope           tween,

                final int                           mouseX,
                final int                           mouseY,
                final float                         delta
        ) {

            array(
                    this.nodes,
                    control,
                    graphics,
                    colors,
                    tween,

                    mouseX,
                    mouseY,
                    delta
            );

        }

    }

}
