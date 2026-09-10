package net.quepierts.thatskyinteractions.feature.client.gui.component.control;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.quepierts.thatskyinteractions.core.property.FloatProperty;
import net.quepierts.thatskyinteractions.core.transition.BooleanTransition;
import net.quepierts.thatskyinteractions.feature.client.gui.ColorStack;
import net.quepierts.thatskyinteractions.feature.client.gui.ExtendedGuiGraphics;
import net.quepierts.thatskyinteractions.feature.client.gui.component.attribute.AttributeHolder;
import net.quepierts.thatskyinteractions.feature.client.gui.component.attribute.IAttributeHolder;
import net.quepierts.thatskyinteractions.feature.client.gui.component.attribute.AttributeKey;
import net.quepierts.thatskyinteractions.feature.client.gui.component.visual.VisualNode;
import net.quepierts.thatskyinteractions.core.model.ui.Insets;
import net.quepierts.thatskyinteractions.feature.mixin.vanilla.client.accessor.AbstractWidgetAccessor;
import net.quepierts.thatskyinteractions.infra.animation.tween.TweenScope;
import net.quepierts.thatskyinteractions.infra.animation.tween.ease.Eases;
import org.joml.Vector2fc;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class Control extends AbstractWidget implements IAttributeHolder {

    public static final AttributeKey<BooleanTransition> ATTRIBUTE_HOVER_TRANSITION
            = new AttributeKey<>("hover_transition");
    public static final AttributeKey<FloatProperty> ATTRIBUTE_HOVER_PROGRESS
            = new AttributeKey<>("hover_progress");

    private static boolean              debug       = false;

    private final AttributeHolder       attributes  = new AttributeHolder();
    private final TweenScope            tween;

    @Getter
    private final FloatProperty         hoverDuration   = new FloatProperty(0.25f);

    @Getter
    private final BooleanTransition     hoverTransition = new BooleanTransition(Eases.CUBIC_OUT, this.hoverDuration);

    @Getter
    private final Consumer<Vector2fc>   position;

    @Getter
    private final Consumer<Vector2fc>   size;

    @Getter
    private final Insets                padding;

    @Getter
    private final Insets                margin;

    @Setter
    @Getter(AccessLevel.PROTECTED)
    private VisualNode                  visualNode;

    @Setter
    private Runnable                    onMouseEntered;

    @Setter
    private Runnable                    onMouseExited;

    public Control(
            final TweenScope    tween,
            final int           x,
            final int           y,
            final int           width,
            final int           height,
            final Component     message
    ) {
        super(x, y, width, height, message);

        this.tween      = tween;

        this.position   = vec2 -> this.setPosition((int) vec2.x(), (int) vec2.y());
        this.size       = vec2 -> { this.setWidth((int) vec2.x()); this.height = (int) vec2.y(); };

        this.padding    = new Insets(0);
        this.margin     = new Insets(0);

        this.setAttribute(ATTRIBUTE_HOVER_PROGRESS, this.hoverDuration);
        this.setAttribute(ATTRIBUTE_HOVER_TRANSITION, this.hoverTransition);
    }

    @Override
    protected final void renderWidget(
            final @NonNull GuiGraphics graphics,
            final int                           mouseX,
            final int                           mouseY,
            final float                         delta
    ) { }

    public final void extractRenderState(
            final @NonNull ExtendedGuiGraphics  graphics,
            final @NonNull ColorStack           colors,
            final int                           mouseX,
            final int                           mouseY,
            final float                         delta
    ) {
        if (this.visible) {
            final var hovering = this.isMouseOver(mouseX, mouseY);

            if (hovering && !this.isHovered) {
                this.onMouseEntered();
                this.hoverTransition.update(this.tween(), true);
            } else if (!hovering && this.isHovered) {
                this.onMouseExited();
                this.hoverTransition.update(this.tween(), false);
            }

            this.isHovered = hovering;
            this.extractControlRenderState(
                    graphics,
                    colors,
                    mouseX,
                    mouseY,
                    delta
            );
            // 1.20.1 的 tooltip 由 AbstractWidget 内部在 render 时刷新，无需手动调用
        }
    }

    @Override
    public void onClick(final double x, final double y) {
        this.onClick(new dev.anvilcraft.lib.v2.input.MouseButtonEvent(x, y, 0), false);
    }

    public void onClick(final dev.anvilcraft.lib.v2.input.MouseButtonEvent event, final boolean doubleClick) {
    }

    @Override
    public void onRelease(final double x, final double y) {
        this.onRelease(new dev.anvilcraft.lib.v2.input.MouseButtonEvent(x, y, 0));
    }

    public void onRelease(final dev.anvilcraft.lib.v2.input.MouseButtonEvent event) {
    }

    // ---- 1.20.1 原版鼠标签名 → mod 内部 MouseButtonEvent 签名的适配层 ----
    // 26.x 的 Screen/AbstractWidget 已经把点击参数打包成 MouseButtonEvent，1.20.1 还是散参数。
    // 在基类转换一次，整棵组件树的重写方法就不用动。

    @Override
    public boolean mouseClicked(final double x, final double y, final int button) {
        return this.mouseClicked(new dev.anvilcraft.lib.v2.input.MouseButtonEvent(x, y, button), false);
    }

    /**
     * 26.x 的 {@code AbstractWidget#mouseClicked(MouseButtonEvent, boolean)} 负责命中测试并派发
     * {@code onClick}。1.20.1 原版只有散参数版本，本类重写散参数版本转发到这里——若这里是空实现，
     * 命中测试与派发就被整个绕过：点击恒返回 false，按钮的按压动画和动作都触发不了。
     * 这里照 26.x 的实现补回。
     */
    public boolean mouseClicked(final dev.anvilcraft.lib.v2.input.MouseButtonEvent event, final boolean doubleClick) {
        if (!this.isActive()) {
            return false;
        }

        if (this.isValidClickButton(event.button()) && this.isMouseOver(event.x(), event.y())) {
            this.playDownSound(net.minecraft.client.Minecraft.getInstance().getSoundManager());
            this.onClick(event, doubleClick);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(final double x, final double y, final int button) {
        return this.mouseReleased(new dev.anvilcraft.lib.v2.input.MouseButtonEvent(x, y, button));
    }

    public boolean mouseReleased(final dev.anvilcraft.lib.v2.input.MouseButtonEvent event) {
        if (this.isValidClickButton(event.button())) {
            this.onRelease(event);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(final double x, final double y, final int button, final double dx, final double dy) {
        return this.mouseDragged(new dev.anvilcraft.lib.v2.input.MouseButtonEvent(x, y, button), dx, dy);
    }

    public boolean mouseDragged(final dev.anvilcraft.lib.v2.input.MouseButtonEvent event, final double dx, final double dy) {
        return this.isValidClickButton(event.button());
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double delta) {
        return this.mouseScrolled(x, y, 0.0, delta);
    }

    public boolean mouseScrolled(final double x, final double y, final double scrollX, final double scrollY) {
        return false;
    }

    /** 1.20.1 的 AbstractWidget 没有 setSize(int,int)，这里补一个等价入口。 */
    public void setControlSize(final int width, final int height) {
        this.setWidth(width);
        this.height = height;
    }

    public void onTick(final float delta) {

    }

    protected void extractControlRenderState(
            final @NonNull ExtendedGuiGraphics  graphics,
            final @NonNull ColorStack           colors,
            final int                           mouseX,
            final int                           mouseY,
            final float                         delta
    ) {

        this.renderDebug(graphics);

        if (this.visualNode != null) {
            final var pose = graphics.pose();
            pose.pushPose();
            this.visualNode.extractRenderState(
                    this,
                    graphics,
                    colors,
                    this.tween,
                    mouseX,
                    mouseY,
                    delta
            );
            pose.popPose();
        }
    }

    protected void onMouseEntered() {
        if (this.onMouseEntered != null) {
            this.onMouseEntered.run();
        }
    }

    protected void onMouseExited() {
        if (this.onMouseExited != null) {
            this.onMouseExited.run();
        }
    }

    public float x() {
        return this.getX();
    }

    public float y() {
        return this.getY();
    }

    @Override
    protected void updateWidgetNarration(
            final @NonNull NarrationElementOutput output
    ) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

    protected TweenScope tween() {
        return this.tween;
    }

    @Override
    public <T> void setAttribute(
            final @NonNull AttributeKey<T>  key,
            final @NonNull T                value
    ) {
        this.attributes.setAttribute(key, value);
    }

    @Override
    public <T> T getAttribute(
            final @NonNull AttributeKey<T>  key
    ) {
        return this.attributes.getAttribute(key);
    }

    @Override
    public <T> boolean hasAttribute(
            final @NonNull AttributeKey<T>  key
    ) {
        return this.attributes.hasAttribute(key);
    }

    protected void renderDebug(
            final @NonNull ExtendedGuiGraphics graphics
    ) {
        if (Control.debug) {
            // if hover, render margin and padding
            if (true) {

                final var margin    = this.getMargin();
                final var original  = graphics.original();
                if (!margin.equals(Insets.NONE)) {
                    original.fill(
                            (int) margin.left,
                            (int) margin.top,
                            (int) ((this.getX() + this.getWidth()) - margin.right),
                            (int) ((this.getY() + this.getHeight()) - margin.bottom),
                            0xFFFF0000
                    );
                }

                final var padding = this.getPadding();
                if (!padding.equals(Insets.NONE)) {
                    original.fill(
                            (int) (this.getX() + padding.left),
                            (int) (this.getY() + padding.top),
                            (int) ((this.getX() + this.getWidth()) - padding.right),
                            (int) ((this.getY() + this.getHeight()) - padding.bottom),
                            0xFF00FF00
                    );
                }

            }
        }
    }

    public static void debug(
            final boolean debug
    ) {
        Control.debug = debug;
    }
}
