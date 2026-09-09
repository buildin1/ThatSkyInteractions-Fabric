package net.neoforged.fml.loading;

import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.api.distmarker.Dist;

public final class FMLLoader {

    private static final FMLLoader INSTANCE = new FMLLoader();

    private FMLLoader() {}

    public static FMLLoader getCurrent() {
        return INSTANCE;
    }

    public static Dist getDist() {
        return FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT
                ? Dist.CLIENT
                : Dist.DEDICATED_SERVER;
    }

    public Dist getDistOrNull() {
        return getDist();
    }
}
