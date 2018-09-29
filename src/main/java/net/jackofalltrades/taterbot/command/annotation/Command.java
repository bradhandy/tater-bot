package net.jackofalltrades.taterbot.command.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
@Component
public @interface Command {

    /**
     * The name of the command within the Spring container.  This attribute is an alias for the the Component's
     * value annotation so we can still define the bean name.
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

}
