package dev.anvilcraft.lib.v2.network.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PACKAGE)
public @interface Network {
    PacketProtocol protocol() default PacketProtocol.PLAY;
}
