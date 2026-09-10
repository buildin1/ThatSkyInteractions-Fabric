package net.quepierts.thatskyinteractions.feature.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.quepierts.thatskyinteractions.feature.client.reference.TsiAtlases;
import org.joml.Vector4f;
import org.jspecify.annotations.NonNull;

/**
 * 1.20.1 差异：
 * <ul>
 *   <li>没有 {@code Minecraft#getAtlasManager()}，图集通过 {@code TextureManager} 按纹理 id 取；</li>
 *   <li>{@code GuiGraphics} 没有 {@code blitSprite(sprite, ...)}，改用带 uv 的 {@code blit}；</li>
 *   <li>{@code pose()} 返回 {@code PoseStack} 而非 {@code Matrix3x2fStack}。</li>
 * </ul>
 */
public final class ExtendedGuiGraphics {

    private final GuiGraphics   graphics;
    private final TextureAtlas  iconAtlas;

    public ExtendedGuiGraphics(final @NonNull GuiGraphics graphics) {
        this.graphics = graphics;

        final var minecraft = Minecraft.getInstance();
        this.iconAtlas = (TextureAtlas) minecraft.getTextureManager()
                .getTexture(TsiAtlases.Sheets.ICONS);
    }

    public void blitIcon(
            final ResourceLocation identifier,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        final var sprite = this.iconAtlas.getSprite(identifier);
        this.blitSprite(sprite, x, y, width, height);
    }

    public void blitIcon(
            final ResourceLocation identifier,
            final int x,
            final int y,
            final int width,
            final int height,
            final int color
    ) {
        this.graphics.setColor(
                net.minecraft.util.FastColor.ARGB32.red(color)   / 255.0f,
                net.minecraft.util.FastColor.ARGB32.green(color) / 255.0f,
                net.minecraft.util.FastColor.ARGB32.blue(color)  / 255.0f,
                net.minecraft.util.FastColor.ARGB32.alpha(color) / 255.0f
        );
        final var sprite = this.iconAtlas.getSprite(identifier);
        this.blitSprite(sprite, x, y, width, height);
        this.graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * 1.20.1 的 {@code GuiGraphics#blit} 不碰混合状态，而 SdfGraphics 每画完一个形状都会
     * {@code disableBlend}。图标是带 alpha 的，这里必须自己把混合打开，否则整张图会被画丢。
     */
    private void blitSprite(
            final TextureAtlasSprite sprite,
            final int x,
            final int y,
            final int width,
            final int height
    ) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        this.graphics.blit(
                x, y,
                0,
                width, height,
                sprite
        );
    }

    /**
     * 1.20.1 的 blit 没有精灵尺寸与颜色参数：颜色用 setColor 包一层，精灵尺寸丢弃。
     */
    public static void blitColored(
            final GuiGraphics       graphics,
            final ResourceLocation  texture,
            final int x, final int y,
            final float u, final float v,
            final int width, final int height,
            final int textureWidth, final int textureHeight,
            final int color
    ) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        graphics.setColor(
                net.minecraft.util.FastColor.ARGB32.red(color)   / 255.0f,
                net.minecraft.util.FastColor.ARGB32.green(color) / 255.0f,
                net.minecraft.util.FastColor.ARGB32.blue(color)  / 255.0f,
                net.minecraft.util.FastColor.ARGB32.alpha(color) / 255.0f
        );
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * 子区域版本：对应 26.x 的
     * {@code blit(pipeline, texture, x, y, u, v, width, height, uWidth, vHeight, texW, texH, color)}。
     */
    public static void blitColored(
            final GuiGraphics       graphics,
            final ResourceLocation  texture,
            final int x, final int y,
            final int width, final int height,
            final float u, final float v,
            final int uWidth, final int vHeight,
            final int textureWidth, final int textureHeight,
            final int color
    ) {
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        graphics.setColor(
                net.minecraft.util.FastColor.ARGB32.red(color)   / 255.0f,
                net.minecraft.util.FastColor.ARGB32.green(color) / 255.0f,
                net.minecraft.util.FastColor.ARGB32.blue(color)  / 255.0f,
                net.minecraft.util.FastColor.ARGB32.alpha(color) / 255.0f
        );
        graphics.blit(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * 26.x 的 {@code GuiGraphicsExtractor#enableScissor} 会先用当前 pose 变换裁剪框
     * （{@code ScreenRectangle#transformAxisAligned}），所以调用方传的是**局部坐标**
     * （滚动面板自身在 0,0，靠外层 pose 平移到屏幕右侧）。
     * 1.20.1 的 {@code GuiGraphics#enableScissor} 直接拿坐标去设 GL scissor，
     * 不做任何变换——照搬局部坐标会把裁剪框留在屏幕左上角，面板里的内容全部被裁掉。
     * 这里补上同样的 pose 变换。
     */
    public void enableScissor(
            final int x0, final int y0,
            final int x1, final int y1
    ) {
        final var matrix = this.graphics.pose().last().pose();
        final var p0     = matrix.transform(new Vector4f(x0, y0, 0.0f, 1.0f));
        final var p1     = matrix.transform(new Vector4f(x1, y1, 0.0f, 1.0f));

        this.graphics.enableScissor(
                Mth.floor(Math.min(p0.x(), p1.x())),
                Mth.floor(Math.min(p0.y(), p1.y())),
                Mth.ceil (Math.max(p0.x(), p1.x())),
                Mth.ceil (Math.max(p0.y(), p1.y()))
        );
    }

    public void disableScissor() {
        this.graphics.disableScissor();
    }

    public PoseStack pose() {
        return this.graphics.pose();
    }

    public GuiGraphics original() {
        return this.graphics;
    }

}
