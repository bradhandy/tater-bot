package net.jackofalltrades.taterbot.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class WaitCapableSupplier<T> implements Supplier<T> {

    private final T product;
    private final int incrementQuantity;
    private final TimeUnit incrementType;

    public WaitCapableSupplier(T product, int incrementQuantity, TimeUnit incrementType) {
        this.product = product;
        this.incrementQuantity = incrementQuantity;
        this.incrementType = incrementType;
    }

    public WaitCapableSupplier(T product) {
        this(product, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public T get() {
        try {
            if (incrementQuantity > 0) {
                Thread.sleep(incrementType.toMillis(incrementQuantity));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return product;
    }

}
