package by.nalivajr.yana.database.tools

import java.math.BigInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Created by Sergey Nalivko
 * Skype: nalivko_sergey
 */
class AtomicBigInteger (value: BigInteger) {

    private val valueHolder = AtomicReference<BigInteger>(value);

    fun incrementAndGet() : BigInteger {
        while (true) {
            val current = valueHolder.get();
            val next = current.add(BigInteger.ONE);
            if (valueHolder.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}