package dev.anvilcraft.lib.v2.registrum;

/**
 * AnvilLib API 兼容层：注册门面。
 * Fabric 上无 mod 事件总线，注册即时完成。
 */
public class Registrum extends AbstractRegistrum<Registrum> {

    protected Registrum(String modid) {
        super(modid);
    }

    public static Registrum create(String modid) {
        return new Registrum(modid);
    }
}
