package net.neoforged.neoforge.client.event;

import net.minecraft.client.Camera;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class ViewportEvent extends Event {

    public static class ComputeCameraAngles extends ViewportEvent implements ICancellableEvent {

        private boolean canceled = false;
        private final Camera camera;
        private float yaw;
        private float pitch;
        private float roll;

        public ComputeCameraAngles(Camera camera, float yaw, float pitch, float roll) {
            this.camera = camera;
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        @Override
        public boolean isCanceled() {
            return this.canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }

        public Camera getCamera() {
            return this.camera;
        }

        public float getYaw() {
            return this.yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public float getPitch() {
            return this.pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public float getRoll() {
            return this.roll;
        }

        public void setRoll(float roll) {
            this.roll = roll;
        }
    }
}
