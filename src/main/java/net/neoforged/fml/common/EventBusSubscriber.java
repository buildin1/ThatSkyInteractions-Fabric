package net.neoforged.fml.common;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {
    net.neoforged.api.distmarker.Dist[] value() default {};

    String modid() default "";

    net.neoforged.api.distmarker.Dist[] dist() default {};

    Bus bus() default Bus.GAME;
}
