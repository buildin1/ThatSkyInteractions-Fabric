package net.quepierts.thatskyinteractions.feature.client.gui.component.visual.floating;

import dev.anvilcraft.lib.v2.rendering.sdf.SdfGraphics;
import dev.anvilcraft.lib.v2.rendering.sdf.SdfParameters;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.client.gui.ColorStack;
import net.quepierts.thatskyinteractions.feature.client.gui.ExtendedGuiGraphics;
import net.quepierts.thatskyinteractions.feature.client.gui.component.control.Control;
import net.quepierts.thatskyinteractions.feature.client.gui.component.floating.FloatingButton;
import net.quepierts.thatskyinteractions.feature.client.gui.component.visual.RenderOp;
import net.quepierts.thatskyinteractions.feature.client.gui.component.visual.VisualNode;
import net.quepierts.thatskyinteractions.infra.animation.tween.TweenScope;
import org.jspecify.annotations.NonNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FloatingButtonNode implements VisualNode {

    public static final ResourceLocation ICON                 = ThatSkyInteractions.location("textures/gui/floating.png");
    public static final SdfParameters PARAMS_BASE
            = SdfGraphics.getInstance()
            .circle(0, 0, 16)
            .fill()
            .share();

    public static final SdfParameters PARAMS_FRAME
            = SdfGraphics.getInstance()
            .circle(0, 0, 15)
            .stroke(1.0f)
            .share();

    private final RenderOp renderOp;

    public static FloatingButtonNode texture(
            final @NonNull ResourceLocation identifier
    ) {
        return new FloatingButtonNode(
                (graphics, colors, __unused0, __unused1, __unused2, __unused3) -> {
                    net.quepierts.thatskyinteractions.feature.client.gui.ExtendedGuiGraphics.blitColored(
                            graphics.original(),
                            identifier,
                            -14, -14,
                            0, 0,
                            28, 28,
                            32, 32,
                            colors.argb()
                    );
                }
        );
    }

    public static FloatingButtonNode icon(
            final @NonNull ResourceLocation identifier
    ) {
        return new FloatingButtonNode(
                (graphics, colors, __unused0, __unused1, __unused2, __unused3) -> {
                    graphics.blitIcon(
                            identifier,
                            -14, -14,
                            28, 28,
                            colors.argb()
                    );
                }
        );
    }

    public static FloatingButtonNode sprite(
            final @NonNull ResourceLocation identifier
    ) {
        return new FloatingButtonNode(
                (graphics, colors, __unused0, __unused1, __unused2, __unused3) -> {
                    net.quepierts.thatskyinteractions.feature.client.gui.ExtendedGuiGraphics.blitColored(
                            graphics.original(),
                            identifier,
                            -14, -28,
                            0, 0,
                            32, 32,
                            32, 32,
                            colors.argb()
                    );
                }
        );
    }

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
        
        final var activeTransition  = control.getAttribute(FloatingButton.ATTRIBUTE_ACTIVE_TRANSITION);
        final var active            = activeTransition.getValue();

        if (active == 0.0f) {
            return;
        }

        final var transiting        = active < 1.0f;

        final var pose              = graphics.pose();
        pose                        .translate(control.x(), control.y(), 0.0f);

        if (transiting) {
            pose                    .scale(0.8f + active * 0.2f, 0.8f + active * 0.2f, 1.0f);
        }

        colors                      .push();
        colors                      .mul(active, 1.0f, 1.0f, 1.0f);

        final var focusTransition   = control.getAttribute(FloatingButton.ATTRIBUTE_FOCUS_TRANSITION);
        final var ft                = focusTransition.getValue();
        final var original          = graphics.original();
        if (ft > 0.0f) {
            pose                    .pushPose();
            pose                    .translate(0, -14, 0.0f);

            final var click         = control.getAttribute(FloatingButton.ATTRIBUTE_CLICK_TRANSITION);
            final var t             = Mth.abs(Mth.cos(2 * click.getValue() * Mth.PI));
            pose                    .scale(t, 1.0f, 1.0f);

            colors                  .push();
            colors                  .mul(ft, 1.0f, 1.0f, 1.0f);

            SdfGraphics             .getInstance()
                                    .reset()
                                    .center(true)

                                    .color(colors.argb(0x80, 0x00, 0x00, 0x00))
                                    .draw(original, PARAMS_BASE, 0, 0)

                                    .color(colors.argb(0xff, 0xa0, 0xa0, 0x80))
                                    .draw(original, PARAMS_FRAME, 0, 0);

            this.renderOp           .render(
                                            graphics,
                                            colors, 0, 0,
                                            0, 0
                                    );

            colors                  .pop();
            pose                    .popPose();

        }

        pose                        .mulPose(com.mojang.math.Axis.ZP.rotation(Mth.HALF_PI * 0.5f));

        net.quepierts.thatskyinteractions.feature.client.gui.ExtendedGuiGraphics.blitColored(
                                            original,
                                            ICON,
                                            -4, -4,
                                            0, 0,
                                            8, 8,
                                            8, 8,
                                            colors.argb()
                                    );

        colors                      .pop();
    }

}
