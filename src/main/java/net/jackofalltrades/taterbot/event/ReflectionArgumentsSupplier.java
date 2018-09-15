package net.jackofalltrades.taterbot.event;

import com.google.common.base.Supplier;

/**
 * Convenience interface to show implementors are returning all of the expected arguments for a method call via
 * reflection.
 *
 * @param <T> The object type for the array of arguments to return.
 */
interface ReflectionArgumentsSupplier<T> extends Supplier<T[]> {

}
