package net.jackofalltrades.taterbot.event;

import com.linecorp.bot.model.event.Event;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a method which should be invoked upon joining a channel within Line.
 *
 * Methods annotated with @EventTask may throw an Exception, and shall have one of the following signatures:
 *   - void methodName(String channelId)
 *   - void methodName(String channelId, String userId)
 *   - void methodName(? extends com.linecorp.bot.model.event.Event event)
 *
 * @author bhandy
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EventTask {

    Class<? extends Event> eventType();

}
