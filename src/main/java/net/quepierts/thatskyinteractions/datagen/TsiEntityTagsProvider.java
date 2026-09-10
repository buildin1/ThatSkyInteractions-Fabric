package net.quepierts.thatskyinteractions.datagen;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.quepierts.thatskyinteractions.feature.registry.TsiEntityTags;

public final class TsiEntityTagsProvider {

    public static void provide(RegistrumTagsProvider.IntrinsicImpl<EntityType<?>> intrinsic) {
        // 1.20.1 没有 MANNEQUIN 实体；tag() 在父类是 protected，走 addOptionalElement 统一处理
        intrinsic.addOptionalElement(TsiEntityTags.ANIMATABLE_HUMANOID, new ResourceLocation("minecraft", "player"));
        intrinsic.addOptionalElement(TsiEntityTags.ANIMATABLE_HUMANOID, new ResourceLocation("easy_npc", "humanoid"));
        intrinsic.addOptionalElement(TsiEntityTags.ANIMATABLE_HUMANOID, new ResourceLocation("easy_npc", "humanoid_slim"));
    }

}
