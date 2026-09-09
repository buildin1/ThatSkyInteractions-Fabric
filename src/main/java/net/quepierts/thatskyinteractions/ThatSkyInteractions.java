package net.quepierts.thatskyinteractions;

import dev.anvilcraft.lib.v2.config.ConfigManager;
import dev.anvilcraft.lib.v2.registrum.Registrum;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.quepierts.thatskyinteractions.datagen.TsiEntityTagsProvider;
import net.quepierts.thatskyinteractions.feature.animation.PlayerAnimationFactory;
import net.quepierts.thatskyinteractions.feature.config.TsiServerConfig;
import net.quepierts.thatskyinteractions.feature.data.DataSyncSystem;
import net.quepierts.thatskyinteractions.feature.registry.*;
import net.quepierts.thatskyinteractions.internal.CompatBootstrap;

@Slf4j
@Getter
public class ThatSkyInteractions implements ModInitializer {
    public static final String          MODID           = "thatskyinteractions";
    public static final Registrum       REGISTRUM       = Registrum.create(MODID);

    public static final TsiServerConfig SERVER_CONFIG
            = ConfigManager.register(ThatSkyInteractions.MODID, TsiServerConfig::new);

    @Override
    public void onInitialize() {
        CompatBootstrap.bootstrapCommon();

        // 提前初始化自定义注册表（NeoForge 由 NewRegistryEvent 触发，早于内容注册）
        TsiRegistries.register();

        AnimationLayerTypes.register();
        InteractionTypes.register();
        ExpressionTypes.register();
        FriendshipBehaviours.register();
        TsiSoundEvents.register();

        TsiParticleTypes.REGISTER.register(null);

        AttachmentTypes.register();

        // FMLCommonSetupEvent.enqueueWork 的等价物
        PlayerAnimationFactory.register();
        DataSyncSystem.register();

        this.setupDataGeneration();

        // 数据包重载监听必须在 ResourceManager 构建前注册
        CompatBootstrap.registerDataReloadListeners();
    }

    public static Identifier location(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    private void setupDataGeneration() {
        REGISTRUM.addDataGenerator(ProviderType.ENTITY_TAGS, TsiEntityTagsProvider::provide);
    }
}
