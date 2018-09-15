package net.jackofalltrades.taterbot.event;

import com.linecorp.bot.model.event.JoinEvent;

@FunctionalInterface
interface ReflectionArgumentsSupplierFactory<T> {

    ReflectionArgumentsSupplier<T> createReflectionArgumentsSupplier(JoinEvent joinEvent);

}
