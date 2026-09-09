package net.quepierts.thatskyinteractions.feature.client.reference;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.experimental.UtilityClass;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;
import org.lwjgl.glfw.GLFW;

@UtilityClass
@EventBusSubscriber(value = Dist.CLIENT, modid = ThatSkyInteractions.MODID)
public class TsiKeys {

    public static final KeyMapping.Category CATEGORY
            = new KeyMapping.Category(ThatSkyInteractions.location("interactions"));

    public static final KeyMapping KEY_INTERACT = new KeyMapping(
            "key.thatskyinteractions.interact",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            CATEGORY
    );

    public static final KeyMapping KEY_UNLOCK_CAMERA = new KeyMapping(
            "key.thatskyinteractions.unlock_camera",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_EQUAL,
            CATEGORY
    );

    public static final KeyMapping KEY_OPEN_EXPRESSION = new KeyMapping(
            "key.thatskyinteractions.open_expression",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            CATEGORY
    );


    @SubscribeEvent
    public static void onRegisterKeyMapping(final RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(KEY_INTERACT);
        event.register(KEY_UNLOCK_CAMERA);
        event.register(KEY_OPEN_EXPRESSION);
    }

}
