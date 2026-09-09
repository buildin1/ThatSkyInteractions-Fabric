package net.quepierts.thatskyinteractions.feature.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import net.quepierts.thatskyinteractions.feature.client.gui.component.floating.FloatingButton;
import net.quepierts.thatskyinteractions.feature.client.gui.component.floating.FloatingTarget;
import net.quepierts.thatskyinteractions.feature.client.gui.component.floating.WorldPositionSupplier;
import net.quepierts.thatskyinteractions.feature.client.gui.component.visual.floating.FloatingButtonNode;
import net.quepierts.thatskyinteractions.feature.client.gui.layer.FloatingControlLayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 靠近其他玩家时在其头顶显示交互提示图标（点击直接打开好友界面）。
 * 纯客户端表现，不参与任何游戏逻辑。
 */
public final class ClientNearbyInteractionPrompt {

    /** 提示触发距离（方块） */
    private static final double RANGE = 8.0;
    private static final double RANGE_SQ = RANGE * RANGE;

    private static final Set<UUID> SHOWN = new HashSet<>();

    private ClientNearbyInteractionPrompt() {}

    public static void tick(final Minecraft minecraft) {

        final var level = minecraft.level;
        final var self = minecraft.player;

        if (level == null || self == null) {
            clear();
            return;
        }

        // 本地玩家正在互动（牵手/背起）时不显示任何提示
        final var selfBond = net.quepierts.thatskyinteractions.feature.bond.PlayerBondAttachment.getAttachment(self);
        if (selfBond.getHandhold().isHolding()
                || selfBond.getCarry().isCarrying()
                || selfBond.getCarry().isBeingCarried()) {
            clear();
            return;
        }

        final Set<UUID> visible = new HashSet<>();

        for (final Player other : level.players()) {
            if (other == self || other.isRemoved()) {
                continue;
            }
            if (self.distanceToSqr(other) > RANGE_SQ) {
                continue;
            }
            // 已存在邀请按钮时不叠加提示
            if (FloatingControlLayer.INSTANCE.hasEntityControl(other.getUUID())) {
                continue;
            }
            // 对方正在互动中也不提示
            final var otherBond = net.quepierts.thatskyinteractions.feature.bond.PlayerBondAttachment.getAttachment(other);
            if (otherBond.getHandhold().isHolding()
                    || otherBond.getCarry().isCarrying()
                    || otherBond.getCarry().isBeingCarried()) {
                continue;
            }

            visible.add(other.getUUID());
            if (SHOWN.add(other.getUUID())) {
                show(other);
            }
        }

        SHOWN.removeIf(uuid -> {
            if (visible.contains(uuid)) {
                return false;
            }
            FloatingControlLayer.INSTANCE.remove(new FloatingTarget.Prompt(uuid));
            return true;
        });
    }

    private static void show(final Player target) {

        FloatingControlLayer.INSTANCE.add(
                new FloatingTarget.Prompt(target.getUUID()),
                FloatingButton.dynamic(
                        Component.empty(),
                        WorldPositionSupplier.entity(target, 2.4f),
                        (_, _) -> ClientPlayerFriendshipSystem.openFriendshipScreen(target)
                ).withVisualNode(FloatingButtonNode.icon(
                        ThatSkyInteractions.location("interaction/high_five")
                ))
        );
    }

    public static void clear() {
        for (final UUID uuid : SHOWN) {
            FloatingControlLayer.INSTANCE.remove(new FloatingTarget.Prompt(uuid));
        }
        SHOWN.clear();
    }
}
