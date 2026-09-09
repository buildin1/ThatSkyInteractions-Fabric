package net.neoforged.neoforge.client.settings;

import net.minecraft.client.KeyMapping;

public enum KeyConflictContext {
    UNIVERSAL {
        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean conflictsDefault(KeyMapping other) {
            return true;
        }
    },
    GUI {
        @Override
        public boolean isActive() {
            return net.minecraft.client.Minecraft.getInstance().screen != null;
        }

        @Override
        public boolean conflictsDefault(KeyMapping other) {
            return false;
        }
    },
    IN_GAME {
        @Override
        public boolean isActive() {
            return net.minecraft.client.Minecraft.getInstance().screen == null;
        }

        @Override
        public boolean conflictsDefault(KeyMapping other) {
            return true;
        }
    };

    public abstract boolean isActive();

    public abstract boolean conflictsDefault(KeyMapping other);
}
