package net.jackofalltrades.taterbot.event;

import com.linecorp.bot.model.event.JoinEvent;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Invokes a method annotated with @JoinEventTask on the bean configured within Spring.
 *
 * @author bhandy
 */
// @todo refactor this handle any event type.
class JoinEventTaskHandler {

    private final Object bean;
    private final Method method;
    private final ReflectionArgumentsSupplierFactory<?> reflectionArgumentsSupplierFactory;

    private boolean valid;

    public JoinEventTaskHandler(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
        this.reflectionArgumentsSupplierFactory = new JoinEventTaskReflectionArgumentsSupplierFactory<>(method);
    }

    public void handleEvent(JoinEvent joinEvent) {
        ReflectionArgumentsSupplier<?> reflectionArgumentsSupplier =
                reflectionArgumentsSupplierFactory.createReflectionArgumentsSupplier(joinEvent);
        try {
            method.invoke(bean, reflectionArgumentsSupplier.get());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unexpected exception while processing join event.", e);
        }
    }

    private static class JoinEventTaskReflectionArgumentsSupplierFactory<T> implements ReflectionArgumentsSupplierFactory<T> {

        private final ReflectionArgumentsSupplierType reflectionArgumentsSupplierType;
        private final Constructor<? extends ReflectionArgumentsSupplier<?>> reflectionArgumentsSupplierTypeConstructor;

        private JoinEventTaskReflectionArgumentsSupplierFactory(Method method) {
            reflectionArgumentsSupplierType = findMatchingReflectionArgumentsSupplierType(method);
            try {
                reflectionArgumentsSupplierTypeConstructor =
                        reflectionArgumentsSupplierType.getReflectionArgumentsSupplierClass().getDeclaredConstructor(JoinEvent.class);
                ReflectionUtils.makeAccessible(reflectionArgumentsSupplierTypeConstructor);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(
                        "ReflectionArgumentsSupplier implement must take a JoinEvent in the constructor.", e);
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
        public ReflectionArgumentsSupplier<T> createReflectionArgumentsSupplier(JoinEvent joinEvent) {
            try {
                return (ReflectionArgumentsSupplier<T>) reflectionArgumentsSupplierTypeConstructor.newInstance(joinEvent);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new IllegalStateException("Unexpected reflective operation exception.", e);
            }
        }

        private enum ReflectionArgumentsSupplierType {
            CHANNEL_ID(ChannelIdReflectionArgumentsSupplier.class, String.class),
            CHANNEL_ID_AND_USER_ID(ChannelIdUserIdReflectionArgumentsSupplier.class, String.class, String.class),
            JOIN_EVENT(JoinEventReflectionArgumentsSupplier.class, JoinEvent.class);

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

        private final JoinEvent joinEvent;

        private ChannelIdReflectionArgumentsSupplier(JoinEvent joinEvent) {
            this.joinEvent = joinEvent;
        }

        @Override
        public String[] get() {
            return new String[]{ joinEvent.getSource().getSenderId() };
        }

    }

    private static class ChannelIdUserIdReflectionArgumentsSupplier implements ReflectionArgumentsSupplier<String> {

        private final JoinEvent joinEvent;

        private ChannelIdUserIdReflectionArgumentsSupplier(JoinEvent joinEvent) {
            this.joinEvent = joinEvent;
        }

        @Override
        public String[] get() {
            return new String[]{ joinEvent.getSource().getSenderId(), joinEvent.getSource().getUserId() };
        }

    }

    private static class JoinEventReflectionArgumentsSupplier implements ReflectionArgumentsSupplier<JoinEvent> {

        private final JoinEvent joinEvent;

        private JoinEventReflectionArgumentsSupplier(JoinEvent joinEvent) {
            this.joinEvent = joinEvent;
        }

        @Override
        public JoinEvent[] get() {
            return new JoinEvent[]{ joinEvent };
        }

    }

}
