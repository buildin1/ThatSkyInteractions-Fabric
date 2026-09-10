package net.quepierts.thatskyinteractions.feature.client.gui.component.visual;

import dev.anvilcraft.lib.v2.rendering.sdf.SdfGraphics;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;

@UtilityClass
public class GeneralRenderOps {

    public static final RenderOp BASE = (graphics, colors, x, y, width, height) -> {
        graphics.original().fill(
                (int) x,
                (int) y,
                (int) (x + width),
                (int) (y + height),
                colors.argb()
        );
    };

    public static final RenderOp ROUND_BASE = (graphics, colors, x, y, width, height) -> {
        SdfGraphics.getInstance()
                .reset()
                .round(6.0f)
                .color(colors.argb())
                .box(
                        x, y,
                        width,
                        height
                )
                .draw(graphics.original());
    };

    public static RenderOp texture(
            final @NonNull ResourceLocation               texture,
            final          int                      textureWidth,
            final          int                      textureHeight
    ) {
        return (graphics, colors, x, y, width, height) -> net.quepierts.thatskyinteractions.feature.client.gui.ExtendedGuiGraphics.blitColored(
                graphics.original(),
                texture,
                (int) (x - width / 2), (int) (y - height / 2),
                0f, 0f,
                (int) width, (int) height,
                textureWidth, textureHeight,
                colors.argb()
        );
    }

}
