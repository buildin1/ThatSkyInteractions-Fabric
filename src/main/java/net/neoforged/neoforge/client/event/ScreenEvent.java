package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.bus.api.Event;

public class ScreenEvent extends Event {

    public static class MouseButtonPressed extends ScreenEvent {

        private final double mouseX;
        private final double mouseY;
        private final int button;

        protected MouseButtonPressed(Screen screen, double mouseX, double mouseY, int button) {
            this.screen = screen;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.button = button;
        }

        private final Screen screen;

        public Screen getScreen() {
            return this.screen;
        }

        public double getMouseX() {
            return this.mouseX;
        }

        public double getMouseY() {
            return this.mouseY;
        }

        public int getButton() {
            return this.button;
        }

        public int getMouseButton() {
            return this.button;
        }

        public static class Post extends MouseButtonPressed {

            private final boolean clickHandled;
            public Post(Screen screen, double mouseX, double mouseY, int button, boolean clickHandled) {
                super(screen, mouseX, mouseY, button);
                this.clickHandled = clickHandled;
            }

            public boolean wasClickHandled() {
                return this.clickHandled;
            }
        }
    }
}
