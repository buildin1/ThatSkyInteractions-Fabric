package dev.anvilcraft.lib.v2.rendering.sdf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.anvilcraft.lib.v2.rendering.ALRPipelines;
import dev.anvilcraft.lib.v2.rendering.AnvilLibRendering;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.math.Axis;
import net.minecraft.util.FastColor;
import dev.anvilcraft.lib.v2.rendering.MthF;
import net.minecraft.util.Mth;




import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;



@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SdfGraphics {
    private static final int            MAX_SDF_AMOUNT          = 256;
    private static final int            MAX_SHARED_SDF_AMOUNT   = 64;
    @Getter
    public static final SdfGraphics     instance                = new SdfGraphics(new SdfParameters());

    private static boolean              debug                   = false;

    private final SdfParameters[]       shared                  = new SdfParameters[MAX_SHARED_SDF_AMOUNT];

    private final SdfParameters         parameters;

    private int                         shareCursor             = 0;

    private float                       x, y, rotation;
    private int                         color                   = -1;
    private boolean                     centred                 = false;

    public SdfGraphics box(float x, float y, float width, float height) {
        this.parameters .getRect()
                        .set(0, 0, width, height);
        this.parameters .box(width, height);
        this.x          = x;
        this.y          = y;

        return          this;
    }

    public SdfGraphics circle(float x, float y, float radius) {
        this.parameters .getRect()
                        .set(0, 0, radius * 2, radius * 2);
        this.parameters .circle(radius);
        this.x          = x;
        this.y          = y;

        return          this;
    }

    public SdfGraphics arc(float x, float y, float sweep, float radius, float width) {
        var scale       = radius * 2 + width;

        this.parameters .getRect()
                        .set(0, 0, scale, scale);
        this.parameters .arc(sweep, radius, width);
        this.x          = x;
        this.y          = y;

        return          this;
    }

    public SdfGraphics sector(float x, float y, float sweep, float radius, float width) {
        this.parameters .getRect()
                        .set(0, 0, radius * 2, radius * 2);
        this.parameters .sector(sweep, radius, width);
        this.x          = x;
        this.y          = y;

        return          this;
    }

    public SdfGraphics pie(float x, float y, float sweep, float radius) {
        this.parameters .getRect()
                        .set(0, 0, radius * 2, radius * 2);
        this.parameters .pie(sweep, radius);
        this.x          = x;
        this.y          = y;

        return          this;
    }

    public SdfGraphics capsule(
            float x, float y,
            float topRadius, float bottomRadius,
            float height
    ) {
        var width       = Math.max(topRadius, bottomRadius) * 2;

        this.parameters .getRect()
                        .set(0, 0, width, height + (topRadius + bottomRadius) * 3);
        this.parameters .capsule(topRadius, bottomRadius, height);
        this.x          = x;
        this.y          = y;

        return          this;
    }

    public SdfGraphics egg(
            float x, float y,
            float topRadius, float bottomRadius,
            float height
    ) {
        var width       = Math.max(topRadius, bottomRadius) * 2;

        this.parameters .getRect()
                        .set(0, 0, width, height + (topRadius + bottomRadius) * 2);
        this.parameters .egg(topRadius, bottomRadius, height);
        this.x          = x;
        this.y          = y;

        return          this;
    }

    public SdfGraphics segment(
            float x0, float y0,
            float x1, float y1
    ) {
        var left        = Math.min(x0, x1);
        var top         = Math.min(y0, y1);
        var width       = Math.abs(x1 - x0);
        var height      = Math.abs(y1 - y0);

        var halfWidth   = (x1 - x0) * 0.5f;
        var halfHeight  = (y1 - y0) * 0.5f;

        this.parameters .getRect()
                        .set(left + width / 2, top + height / 2, width, height);
        this.parameters .segment( -halfWidth, -halfHeight, +halfWidth, +halfHeight);
        this.x          = left + width / 2;
        this.y          = top + height / 2;

        return          this;
    }

    public SdfGraphics triangleEquilateral(float x, float y, float radius) {
        var actual      = radius * (1.0f / 1.2f);

        this.parameters .getRect()
                        .set(0, 0, radius * 2, radius * 2);
        this.parameters .triangleEquilateral(actual);
        this.x          = x;
        this.y          = y;

        return          this;
    }

    public SdfGraphics triangleIsosceles(float x, float y, float width, float height) {
        final var factor = 1.0f / 1.2f;
        this.parameters .getRect()
                        .set(0, 0, width * 2.0f, height);
        this.parameters .triangleIsosceles(width * factor, height * factor);
        this.x          = x;
        this.y          = y;

        return          this;
    }

    public SdfGraphics color(int color) {
        this.color      = color;
        return          this;
    }

    public SdfGraphics color(float red, float green, float blue, float alpha) {
        this.color      = FastColor.ARGB32.color((int)(alpha * 255.0f), (int)(red * 255.0f), (int)(green * 255.0f), (int)(blue * 255.0f));
        return          this;
    }

    public SdfGraphics color(int red, int green, int blue, int alpha) {
        this.color      = FastColor.ARGB32.color(alpha, red, green, blue);
        return          this;
    }

    public SdfGraphics smooth(float radius) {
        var v           = Math.max(0.0f, radius);
        this.parameters .smooth(v);
        return          this;
    }

    public SdfGraphics round(float radius) {
        var v           = Math.max(0.0f, radius);
        this.parameters .round(v);
        return          this;
    }

    public SdfGraphics stroke(float width) {
        var v           = Math.max(0.0f, width * 0.5f);
        this.parameters .stroke(v);
        this.parameters .onion(width > 0.0f);
        return          this;
    }

    public SdfGraphics rotate(float degrees) {
        this.rotation   = degrees;
        return          this;
    }

    public SdfGraphics center(boolean center) {
        this.centred    = center;
        return          this;
    }

    public SdfGraphics onion(boolean onion) {
        this.parameters .onion(onion);
        return          this;
    }

    public SdfGraphics fill() {
        this.parameters .fill();
        return          this;
    }

    public SdfGraphics light(float radius) {
        this.parameters .light(radius);
        return          this;
    }

    public SdfGraphics draw(
            @NotNull GuiGraphics   graphics
    ) {
        _draw(
                graphics, 
                this.parameters, 
                this.x, 
                this.y,
                this.rotation,
                this.color,
                this.centred
        );
        return          this;
    }

    public SdfGraphics draw(
            @NotNull GuiGraphics   graphics,
            @NotNull SdfParameters          parameters,
                     float                  x,
                     float                  y
    ) {

        if (parameters.isShared()) {
            _draw(
                    graphics, 
                    parameters, 
                    x, 
                    y,
                    this.rotation,
                    this.color,
                    this.centred
            );
        }

        return this;
    }

    public SdfGraphics reset() {
        this.parameters .reset();
        this.x          = 0;
        this.y          = 0;
        this.rotation   = 0;
        this.color      = -1;
        this.centred    = false;
        return          this;
    }

    public boolean collide(float x, float y, float threshold) {
        return this.collide(
                this.parameters,
                x,
                y,
                this.x,
                this.y,
                threshold
        );
    }

    public boolean collide(
            @NotNull SdfParameters          parameters,
                     float                  pointX,
                     float                  pointY,
                     float                  x,
                     float                  y,
                     float                  threshold
    ) {
        return Sdf2d    .sd(
                            parameters,
                            pointX,
                            pointY,
                            x,
                            y,
                            this.rotation,
                            this.centred
                        ) < threshold;
    }

    public @NonNull SdfParameters cache() {
        return this.parameters.duplicate();
    }

    public @NonNull SdfParameters share() {
        final var cache = this.cache();
        this.share(cache);
        return cache;
    }

    public void share(@NotNull SdfParameters cache) {
        final var cursor = this.shareCursor;
        if (cursor == MAX_SHARED_SDF_AMOUNT) {
            return;
        }

        this.shared[cursor]     = cache;
        cache.uboIndex          = MAX_SDF_AMOUNT - cursor - 1;
        cache.uploaded          = false;

        do {
            this.shareCursor++;
        } while (this.shareCursor < MAX_SHARED_SDF_AMOUNT && this.shared[this.shareCursor] != null);
    }

    public void unshare(@NotNull SdfParameters cache) {
        var cursor = 0;
        // find index
        for (; cursor < MAX_SHARED_SDF_AMOUNT; cursor++) {
            if (this.shared[cursor] == cache) {
                cache.uploaded = false;
                cache.uboIndex = -1;
                break;
            }
        }

        if (cursor == MAX_SHARED_SDF_AMOUNT) {
            return;
        }

        this.shared[cursor] = null;
        if (cursor < this.shareCursor) {
            this.shareCursor = cursor;
        }
    }

    public static void flush() {
        instance.parameters .reset();
        SdfGraphics.index   = 0;
    }

    private static  int             index;

    /** 1.20.1 没有 GPU 缓冲抽象，无需初始化；保留空实现以维持调用点不变。 */
    public static void init() {
    }

    public static void debug(boolean enable) {
        SdfGraphics.debug = enable;
    }

    /**
     * 1.20.1 后端：一个形状一次 draw call，形状参数走逐形状 uniform。
     *
     * <p>26.x 分支是「256 个形状写进 std140 UBO，顶点用 UV1.x 索引，GUI pass 统一绘制」。
     * 1.20.1 的 ShaderInstance 既不支持 UBO 也不支持 uniform 数组，所以改为立即模式绘制。
     * GUI 一帧通常只有几十个形状，多出来的 draw call 无关紧要。
     */
    private static void _draw(
            @NotNull GuiGraphics            graphics,
            @NotNull SdfParameters          parameters,
                     float                  x,
                     float                  y,
                     float                  rotation,
                     int                    color,
                     boolean                centred
    ) {
        final var shader    = ALRPipelines.sdfGraphics();

        if (shader == null) {
            return;
        }

        final var round     = parameters.getRound();
        final var smooth    = parameters.getSmooth();
        final var stroke    = parameters.getStroke();

        final var rect      = parameters.getRect();
        final var z         = rect.z;
        final var w         = rect.w;

        final var ex        = (round + smooth + stroke) * 2.0f;
        final var width     = rect.z + ex;
        final var height    = rect.w + ex;
        final var hw        = width * 0.5f;
        final var hh        = height * 0.5f;

        // 着色器按扩张后的尺寸计算距离场，与 26.x 一致
        rect.z              = width;
        rect.w              = height;

        final var shared    = parameters.getSharedParams();
        final var shape     = parameters.getShapeParams();
        final var types     = parameters.getTypeParams();

        setUniform(shader, "SdfShared", shared.x, shared.y, shared.z, shared.w);
        setUniform(shader, "SdfShape",  shape.x,  shape.y,  shape.z,  shape.w);
        setUniform(shader, "SdfRect",   rect.x,   rect.y,   rect.z,   rect.w);
        setUniformInt(shader, "SdfTypes", types.x, types.y, types.z, types.w);

        rect.z              = z;
        rect.w              = w;

        final PoseStack pose = graphics.pose();
        pose                .pushPose();

        if (centred) {
            pose            .translate(x, y, 0.0f);
        } else {
            pose            .translate(x + hw, y + hh, 0.0f);
        }

        if (rotation != 0.0f) {
            pose            .mulPose(Axis.ZP.rotationDegrees(rotation));
        }

        pose                .scale(width, height, 1.0f);

        final Matrix4f mat  = pose.last().pose();

        final int a         = FastColor.ARGB32.alpha(color);
        final int r         = FastColor.ARGB32.red(color);
        final int g         = FastColor.ARGB32.green(color);
        final int b         = FastColor.ARGB32.blue(color);

        RenderSystem        .enableBlend();
        RenderSystem        .defaultBlendFunc();
        RenderSystem        .setShader(ALRPipelines::sdfGraphics);

        final BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer              .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        buffer              .vertex(mat, -0.5f, -0.5f, 0.0f).color(r, g, b, a).uv(0.0f, 0.0f).endVertex();
        buffer              .vertex(mat, -0.5f, +0.5f, 0.0f).color(r, g, b, a).uv(0.0f, 1.0f).endVertex();
        buffer              .vertex(mat, +0.5f, +0.5f, 0.0f).color(r, g, b, a).uv(1.0f, 1.0f).endVertex();
        buffer              .vertex(mat, +0.5f, -0.5f, 0.0f).color(r, g, b, a).uv(1.0f, 0.0f).endVertex();
        BufferUploader      .drawWithShader(buffer.end());

        RenderSystem        .disableBlend();

        pose                .popPose();

        if (debug) {
            final var extX  = hw + 1.0f;
            final var extY  = hh + 1.0f;
            graphics        .renderOutline(
                                    (int) (x - extX), (int) (y - extY),
                                    (int) (extX * 2.0f), (int) (extY * 2.0f),
                                    0xFF0000FF
                            );
        }

        if (!parameters.isShared()) {
            SdfGraphics     .index++;
        }
    }

    private static void setUniform(
            @NotNull net.minecraft.client.renderer.ShaderInstance shader,
            @NotNull String name,
            float a, float b, float c, float d
    ) {
        final var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(a, b, c, d);
        }
    }

    private static void setUniformInt(
            @NotNull net.minecraft.client.renderer.ShaderInstance shader,
            @NotNull String name,
            int a, int b, int c, int d
    ) {
        final var uniform = shader.getUniform(name);
        if (uniform != null) {
            uniform.set(a, b, c, d);
        }
    }
}
