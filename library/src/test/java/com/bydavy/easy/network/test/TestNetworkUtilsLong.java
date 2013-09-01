package com.bydavy.easy.network.test;

import com.bydavy.easy.network.utils.EasyNetworkUtils;
import com.bydavy.easy.network.test.utils.ByteBufferUtils;
import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestNetworkUtilsLong extends TestCase {

    private static final int LONG_SIZE = 8;

    public void testReadWriteLongBigEndian() {
        final long l = Long.MAX_VALUE;
        final ByteBuffer buffer = ByteBuffer.allocate(LONG_SIZE);

        EasyNetworkUtils.writeLong(l, buffer, ByteOrder.BIG_ENDIAN);
        buffer.flip();
        assertEquals(l, EasyNetworkUtils.readLong(buffer, ByteOrder.BIG_ENDIAN));
        buffer.position(0);
        assertNotSame(l, EasyNetworkUtils.readLong(buffer, ByteOrder.LITTLE_ENDIAN));
    }

    public void testReadWriteLongLittleEndian() {
        final long l = Long.MIN_VALUE;
        final ByteBuffer buffer = ByteBuffer.allocate(LONG_SIZE);

        EasyNetworkUtils.writeLong(l, buffer, ByteOrder.LITTLE_ENDIAN);
        buffer.flip();
        assertEquals(l, EasyNetworkUtils.readLong(buffer, ByteOrder.LITTLE_ENDIAN));
        buffer.position(0);
        assertNotSame(l, EasyNetworkUtils.readLong(buffer, ByteOrder.BIG_ENDIAN));
    }

    public void testReadWriteLongBigEndianOffset() {
        final long l = 0x01234567901234567L;
        final ByteBuffer buffer = ByteBuffer.allocate(LONG_SIZE * 3);
        final int offset = 3;
        final byte arrayPaddingValue = 0;

        ByteBufferUtils.initialize(buffer, arrayPaddingValue);

        buffer.position(offset);
        EasyNetworkUtils.writeLong(l, buffer, ByteOrder.BIG_ENDIAN);
        buffer.flip();
        buffer.position(offset);
        assertEquals(l, EasyNetworkUtils.readLong(buffer, ByteOrder.BIG_ENDIAN));
        buffer.position(offset);
        assertNotSame(l, EasyNetworkUtils.readLong(buffer, ByteOrder.LITTLE_ENDIAN));

        ByteBufferUtils.assertPadding(arrayPaddingValue, buffer, offset, LONG_SIZE);
    }

    public void testReadWriteIntegerLittleEndianOffset() {
        final long l = Long.MAX_VALUE / 2;
        final ByteBuffer buffer = ByteBuffer.allocate(LONG_SIZE * 4);
        final int offset = 3;
        final byte arrayPaddingValue = -1;

        ByteBufferUtils.initialize(buffer, arrayPaddingValue);

        buffer.position(offset);
        EasyNetworkUtils.writeLong(l, buffer, ByteOrder.LITTLE_ENDIAN);
        buffer.flip();
        buffer.position(offset);
        assertEquals(l, EasyNetworkUtils.readLong(buffer, ByteOrder.LITTLE_ENDIAN));
        buffer.position(offset);
        assertNotSame(l, EasyNetworkUtils.readLong(buffer, ByteOrder.BIG_ENDIAN));

        ByteBufferUtils.assertPadding(arrayPaddingValue, buffer, offset, LONG_SIZE);
    }
}
