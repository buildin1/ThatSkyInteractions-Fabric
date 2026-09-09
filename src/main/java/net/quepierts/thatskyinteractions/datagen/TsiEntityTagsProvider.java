package net.quepierts.thatskyinteractions.datagen;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.quepierts.thatskyinteractions.feature.registry.TsiEntityTags;

public final class TsiEntityTagsProvider {

    public static void provide(RegistrumTagsProvider.IntrinsicImpl<EntityType<?>> intrinsic) {
        intrinsic.tag(TsiEntityTags.ANIMATABLE_HUMANOID)
                .add(EntityType.PLAYER, EntityType.MANNEQUIN);
        intrinsic.addOptionalElement(TsiEntityTags.ANIMATABLE_HUMANOID, Identifier.fromNamespaceAndPath("easy_npc", "humanoid"));
        intrinsic.addOptionalElement(TsiEntityTags.ANIMATABLE_HUMANOID, Identifier.fromNamespaceAndPath("easy_npc", "humanoid_slim"));
    }

}
