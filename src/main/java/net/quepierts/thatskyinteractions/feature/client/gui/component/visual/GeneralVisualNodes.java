package net.quepierts.thatskyinteractions.feature.client.gui.component.visual;

import dev.anvilcraft.lib.v2.rendering.sdf.SdfGraphics;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.thatskyinteractions.feature.client.gui.component.visual.button.ButtonSdfParameters;
import org.jspecify.annotations.NonNull;

@UtilityClass
public class GeneralVisualNodes {

    public static final VisualNode BASE = (control, graphics, colors, __unused0, __unused1, __unused2, __unused3) -> GeneralRenderOps.BASE.render(
            graphics,
            colors,
            control.getX(),
            control.getY(),
            control.getWidth(),
            control.getHeight()
    );

    public static final VisualNode ROUNDED_BASE = (control, graphics, colors, __unused0, __unused1, __unused2, __unused3) -> GeneralRenderOps.ROUND_BASE.render(
            graphics,
            colors,
            control.getX(),
            control.getY(),
            control.getWidth(),
            control.getHeight()
    );

    public static VisualNode fill(
            final int   color
    ) {
        return (control, graphics, colors, __unused0, __unused1, __unused2, __unused3) -> graphics.original().fill(
                control.getX(),
                control.getY(),
                control.getX() + control.getWidth(),
                control.getY() + control.getHeight(),
                colors.argb(color)
        );
    }

    public static VisualNode base(
            final int   color,
            final float round
    ) {
        return (control, graphics, colors, __unused0, __unused1, __unused2, __unused3) -> {

            final var hw = control.getWidth() / 2;
            final var hh = control.getHeight() / 2;
            final var x  = control.getX() + hw;
            final var y  = control.getY() + hh;

            SdfGraphics.getInstance()
                    .reset()
                    .center(true)
                    .color(colors.argb(color))
                    .round(round)
                    .box(
                            x, y,
                            control.getWidth(),
                            control.getHeight()
                    )
                    .draw(graphics.original());
        };
    }

    public static VisualNode lBase(
            final int   color
    ) {
        return (__unused0, graphics, colors, __unused1, __unused2, __unused3, __unused4) -> {

            SdfGraphics.getInstance()
                    .reset()
                    .center(true)
                    .color(colors.argb(color))
                    .draw(graphics.original(), ButtonSdfParameters.PARAM_BASE32, 0, 0);
        };
    }

    public static VisualNode texture(
            final @NonNull ResourceLocation               texture,
            final          int                      textureWidth,
            final          int                      textureHeight
    ) {
        return (control, graphics, colors, __unused0, __unused1, __unused2, __unused3) ->
                net.quepierts.thatskyinteractions.feature.client.gui.ExtendedGuiGraphics.blitColored(
                        graphics.original(),
                        texture,
                        control.getX(),
                        control.getY(),
                        0, 0,
                        textureWidth,
                        textureHeight,
                        textureWidth,
                        textureHeight,
                        colors.argb()
                );
    }

    public static VisualNode message(
            final @NonNull Component message
    ) {
        return (control, graphics, colors, __unused0, __unused1, __unused2, __unused3) -> graphics.original().drawCenteredString(
                Minecraft.getInstance().font,
                message,
                control.getX() + control.getWidth() / 2,
                control.getY() + control.getHeight() / 2,
                colors.argb()
        );
    }

}
