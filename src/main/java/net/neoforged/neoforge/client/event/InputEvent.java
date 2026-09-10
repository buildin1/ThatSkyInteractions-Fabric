package net.neoforged.neoforge.client.event;

import net.minecraft.client.KeyMapping;
import dev.anvilcraft.lib.v2.input.KeyEvent;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class InputEvent extends Event {

    public static class Key extends InputEvent implements ICancellableEvent {

        private final int key;
        private final int scanCode;
        private final int action;
        private final int modifiers;
        private final KeyEvent keyEvent;

        public Key(KeyEvent keyEvent, int action) {
            this.keyEvent = keyEvent;
            this.key = keyEvent.key();
            this.scanCode = keyEvent.scancode();
            this.modifiers = keyEvent.modifiers();
            this.action = action;
        }

        public int getKey() {
            return this.key;
        }

        public int getScanCode() {
            return this.scanCode;
        }

        public int getAction() {
            return this.action;
        }

        public int getModifiers() {
            return this.modifiers;
        }

        public KeyEvent getKeyEvent() {
            return this.keyEvent;
        }
    }

    public static class MouseScrollingEvent extends InputEvent implements ICancellableEvent {

        private final double scrollDeltaX;
        private final double scrollDeltaY;
        private final double mouseX;
        private final double mouseY;

        public MouseScrollingEvent(double scrollDeltaX, double scrollDeltaY, double mouseX, double mouseY) {
            this.scrollDeltaX = scrollDeltaX;
            this.scrollDeltaY = scrollDeltaY;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        public double getScrollDelta() {
            return this.scrollDeltaY;
        }

        public double getScrollDeltaX() {
            return this.scrollDeltaX;
        }

        public double getScrollDeltaY() {
            return this.scrollDeltaY;
        }

        public double getMouseX() {
            return this.mouseX;
        }

        public double getMouseY() {
            return this.mouseY;
        }
    }

    public static class InteractionKeyMappingTriggered extends InputEvent implements ICancellableEvent {

        private final int keyType; // 0=attack 1=use 2=pick
        private final KeyMapping keyMapping;
        private Boolean swingHand;

        private InteractionKeyMappingTriggered(int keyType, KeyMapping keyMapping) {
            this.keyType = keyType;
            this.keyMapping = keyMapping;
        }

        public static InteractionKeyMappingTriggered attack(KeyMapping mapping) {
            return new InteractionKeyMappingTriggered(0, mapping);
        }

        public static InteractionKeyMappingTriggered use(KeyMapping mapping) {
            return new InteractionKeyMappingTriggered(1, mapping);
        }

        public static InteractionKeyMappingTriggered pick(KeyMapping mapping) {
            return new InteractionKeyMappingTriggered(2, mapping);
        }

        public KeyMapping getKeyMapping() {
            return this.keyMapping;
        }

        public boolean isAttack() {
            return this.keyType == 0;
        }

        public boolean isUseItem() {
            return this.keyType == 1;
        }

        public boolean isPickItem() {
            return this.keyType == 2;
        }

        /** 触发按键对应的手（26.x 无独立副手键，均为 MAIN_HAND） */
        public InteractionHand getHand() {
            return InteractionHand.MAIN_HAND;
        }

        public void setSwingHand(boolean swingHand) {
            this.swingHand = swingHand;
        }

        public boolean getSwingHand() {
            return this.swingHand == null || this.swingHand;
        }
    }
}
