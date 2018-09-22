package net.jackofalltrades.taterbot.event;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.LeaveEvent;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Invokes a method annotated with @EventTask on the bean configured within Spring.
 *
 * @author bhandy
 */
// @todo refactor this handle any event type.
class EventTaskHandler {

    private final Class<? extends Event> eventType;
    private final Object bean;
    private final Method method;
    private final ReflectionArgumentsSupplierFactory<?> reflectionArgumentsSupplierFactory;

    EventTaskHandler(Class<? extends Event> eventType, Object bean, Method method) {
        this.eventType = eventType;
        this.bean = bean;
        this.method = method;
        this.reflectionArgumentsSupplierFactory = new EventTaskReflectionArgumentsSupplierFactory<>(method);
    }

    void handleEvent(Event event) {
        ReflectionArgumentsSupplier<?> reflectionArgumentsSupplier =
                reflectionArgumentsSupplierFactory.createReflectionArgumentsSupplier(event);
        try {
            method.invoke(bean, reflectionArgumentsSupplier.get());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unexpected exception while processing join event.", e);
        }
    }

    boolean supports(Event event) {
        return eventType.isInstance(event);
    }

    private static class EventTaskReflectionArgumentsSupplierFactory<T> implements ReflectionArgumentsSupplierFactory<T> {

        private final ReflectionArgumentsSupplierType reflectionArgumentsSupplierType;
        private final Constructor<? extends ReflectionArgumentsSupplier<?>> reflectionArgumentsSupplierTypeConstructor;

        private EventTaskReflectionArgumentsSupplierFactory(Method method) {
            reflectionArgumentsSupplierType = findMatchingReflectionArgumentsSupplierType(method);
            try {
                reflectionArgumentsSupplierTypeConstructor =
                        reflectionArgumentsSupplierType.getReflectionArgumentsSupplierClass().getDeclaredConstructor(Event.class);
                ReflectionUtils.makeAccessible(reflectionArgumentsSupplierTypeConstructor);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        "ReflectionArgumentsSupplier implement must take an Event subtype in the constructor.",
                        e);
            }
        }

        private ReflectionArgumentsSupplierType findMatchingReflectionArgumentsSupplierType(Method method) {
            for (ReflectionArgumentsSupplierType reflectionArgumentsSupplierType :
                    ReflectionArgumentsSupplierType.values()) {
                if (reflectionArgumentsSupplierType.matchesMethod(method)) {
                    return reflectionArgumentsSupplierType;
                }
            }

            throw new IllegalStateException("Invalid method signature:  " + method.getParameterTypes());
        }

        @Override
        public ReflectionArgumentsSupplier<T> createReflectionArgumentsSupplier(Event event) {
            try {
                return (ReflectionArgumentsSupplier<T>) reflectionArgumentsSupplierTypeConstructor.newInstance(event);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException("Unexpected reflective operation exception.", e);
            }
        }

        private enum ReflectionArgumentsSupplierType {
            CHANNEL_ID(ChannelIdReflectionArgumentsSupplier.class, String.class),
            CHANNEL_ID_AND_USER_ID(ChannelIdUserIdReflectionArgumentsSupplier.class, String.class, String.class),
            JOIN_EVENT(EventReflectionArgumentsSupplier.class, JoinEvent.class),
            LEAVE_EVENT(EventReflectionArgumentsSupplier.class, LeaveEvent.class);

            private final Class<? extends ReflectionArgumentsSupplier<?>> reflectionArgumentsSupplierClass;
            private final Class<?>[] argumentTypes;

            ReflectionArgumentsSupplierType(
                    Class<? extends ReflectionArgumentsSupplier<?>> reflectionArgumentsSupplierClass,
                    Class<?>... argumentTypes) {
                this.reflectionArgumentsSupplierClass = reflectionArgumentsSupplierClass;
                this.argumentTypes = argumentTypes;
            }

            public boolean matchesMethod(Method method) {
                return Arrays.equals(method.getParameterTypes(), argumentTypes);
            }

            public Class<? extends ReflectionArgumentsSupplier<?>> getReflectionArgumentsSupplierClass() {
                return reflectionArgumentsSupplierClass;
            }

        }

    }

    private static class ChannelIdReflectionArgumentsSupplier implements ReflectionArgumentsSupplier<String> {

        private final Event event;

        private ChannelIdReflectionArgumentsSupplier(Event event) {
            this.event = event;
        }

        @Override
        public String[] get() {
            return new String[]{ event.getSource().getSenderId() };
        }

    }

    private static class ChannelIdUserIdReflectionArgumentsSupplier implements ReflectionArgumentsSupplier<String> {

        private final Event event;

        private ChannelIdUserIdReflectionArgumentsSupplier(Event event) {
            this.event = event;
        }

        @Override
        public String[] get() {
            return new String[]{ event.getSource().getSenderId(), event.getSource().getUserId() };
        }

    }

    private static class EventReflectionArgumentsSupplier implements ReflectionArgumentsSupplier<Event> {

        private final Event event;

        private EventReflectionArgumentsSupplier(Event event) {
            this.event = event;
        }

        @Override
        public Event[] get() {
            return new Event[]{ event };
        }

    }

}
