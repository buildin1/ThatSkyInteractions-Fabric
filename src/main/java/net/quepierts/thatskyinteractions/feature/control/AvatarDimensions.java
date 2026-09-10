package net.quepierts.thatskyinteractions.feature.control;

import lombok.experimental.UtilityClass;
import net.minecraft.world.entity.EntityDimensions;

@UtilityClass
public class AvatarDimensions {

    // 1.20.1 的 EntityDimensions 既没有 withEyeHeight 也没有 attachments：
    // 乘客位置由 Entity#getPassengerRidingPosition 决定，眼高由 Entity#getEyeHeight 给出。
    public static final EntityDimensions    RIDING_DIMENSIONS
            = EntityDimensions.scalable(0.6F, 1.5F);

}
