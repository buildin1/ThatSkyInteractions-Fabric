package net.quepierts.thatskyinteractions.feature.client.reference;

import lombok.experimental.UtilityClass;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import net.quepierts.thatskyinteractions.ThatSkyInteractions;

@UtilityClass
@EventBusSubscriber(value = Dist.CLIENT, modid = ThatSkyInteractions.MODID)
public class TsiAtlases {

    public static final ResourceLocation ICONS
            = ThatSkyInteractions.location("icons");

    @SubscribeEvent
    public static void onRegisterTextureAtlases(final RegisterTextureAtlasesEvent event) {

        // 1.20.1 的图集由 assets/thatskyinteractions/atlases/icons.json 数据驱动，无需代码注册
        event.register(Sheets.ICONS, ICONS, false);

    }

    public static final class Sheets {
        public static final ResourceLocation ICONS
                = ThatSkyInteractions.location("textures/atlas/icons.png");
    }

}
