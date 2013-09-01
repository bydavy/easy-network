package com.bydavy.easy.network.test.utils;


import org.junit.Assert;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

public class ByteBufferUtils {
    public static void assertPadding(byte paddingValue, @Nonnull ByteBuffer buffer, @Nonnegative int offset, @Nonnegative int length) {
        buffer.position(0);
        buffer.limit(buffer.capacity());
        for (int i = 0; i < offset; i++) {
            Assert.assertEquals(paddingValue, buffer.get());
        }

        buffer.position(offset + length);
        while (buffer.hasRemaining()) {
            Assert.assertEquals(paddingValue, buffer.get());
        }

        buffer.position(0);
    }

    public static void initialize(@Nonnull ByteBuffer buffer, byte value) {
        buffer.position(0);
        buffer.limit(buffer.capacity());
        while (buffer.hasRemaining()) {
            buffer.put(value);
        }

        buffer.position(0);
    }

    private ByteBufferUtils() {
        throw new IllegalAccessError();
    }
}
