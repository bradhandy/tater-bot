package net.jackofalltrades.taterbot.event;

@FunctionalInterface
interface ReflectionArgumentsSupplierFactory<T> {

    ReflectionArgumentsSupplier<T> createReflectionArgumentsSupplier();

}
