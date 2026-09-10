package net.quepierts.thatskyinteractions.feature.mixin.vanilla.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 1.20.1 的图集清单是 {@code ModelManager.VANILLA_ATLASES} 这张写死的 {@code Map.of}，
 * 只放 {@code assets/&lt;ns&gt;/atlases/*.json} 不会被拼图（26.x 的 AtlasManager 才允许代码注册）。
 * 这里把 {@link RegisterTextureAtlasesEvent} 收集到的条目并进去，
 * 于是 {@code thatskyinteractions:textures/atlas/icons.png} 才会真正生成。
 */
@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {

    @ModifyExpressionValue(
            method = "<init>",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/resources/model/ModelManager;VANILLA_ATLASES:Ljava/util/Map;",
                    opcode = org.objectweb.asm.Opcodes.GETSTATIC
            )
    )
    private Map<ResourceLocation, ResourceLocation> tsi$addAtlases(
            final Map<ResourceLocation, ResourceLocation> original
    ) {
        final var extra = RegisterTextureAtlasesEvent.registered();
        if (extra.isEmpty()) {
            return original;
        }
        final var merged = new LinkedHashMap<>(original);
        merged.putAll(extra);
        return merged;
    }
}
