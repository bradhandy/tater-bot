package net.jackofalltrades.taterbot.event;

import com.linecorp.bot.model.event.Event;

@FunctionalInterface
interface ReflectionArgumentsSupplierFactory<T> {

    ReflectionArgumentsSupplier<T> createReflectionArgumentsSupplier(Event joinEvent);

}
