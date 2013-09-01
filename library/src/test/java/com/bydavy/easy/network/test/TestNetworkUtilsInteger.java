package com.bydavy.easy.network.test;

import com.bydavy.easy.network.utils.EasyNetworkUtils;
import com.bydavy.easy.network.test.utils.ByteBufferUtils;
import junit.framework.TestCase;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TestNetworkUtilsInteger extends TestCase {

    private static final int INTEGER_SIZE = 4;

    public void testReadWriteIntegerBigEndian() {
        final int i = Integer.MAX_VALUE;
        final ByteBuffer buffer = ByteBuffer.allocate(INTEGER_SIZE);

        EasyNetworkUtils.writeInt(i, buffer, ByteOrder.BIG_ENDIAN);
        buffer.flip();
        assertEquals(i, EasyNetworkUtils.readInt(buffer, ByteOrder.BIG_ENDIAN));
        buffer.position(0);
        assertNotSame(i, EasyNetworkUtils.readInt(buffer, ByteOrder.LITTLE_ENDIAN));
    }

    public void testReadWriteIntegerLittleEndian() {
        final int i = Integer.MIN_VALUE;
        final ByteBuffer buffer = ByteBuffer.allocate(INTEGER_SIZE);

        EasyNetworkUtils.writeInt(i, buffer, ByteOrder.LITTLE_ENDIAN);
        buffer.flip();
        assertEquals(i, EasyNetworkUtils.readInt(buffer, ByteOrder.LITTLE_ENDIAN));
        buffer.position(0);
        assertNotSame(i, EasyNetworkUtils.readInt(buffer, ByteOrder.BIG_ENDIAN));
    }

    public void testReadWriteIntegerBigEndianOffset() {
        final int i = 0x01234567;
        final ByteBuffer buffer = ByteBuffer.allocate(9);
        final int offset = 3;
        final byte arrayPaddingValue = 0;


        ByteBufferUtils.initialize(buffer, arrayPaddingValue);

        buffer.position(offset);
        EasyNetworkUtils.writeInt(i, buffer, ByteOrder.BIG_ENDIAN);
        buffer.flip();
        buffer.position(offset);
        assertEquals(i, EasyNetworkUtils.readInt(buffer, ByteOrder.BIG_ENDIAN));
        buffer.position(offset);
        assertNotSame(i, EasyNetworkUtils.readInt(buffer, ByteOrder.LITTLE_ENDIAN));

        ByteBufferUtils.assertPadding(arrayPaddingValue, buffer, offset, INTEGER_SIZE);
    }

    public void testReadWriteIntegerLittleEndianOffset() {
        final int i = Integer.MAX_VALUE / 2;
        final ByteBuffer buffer = ByteBuffer.allocate(9);
        final int offset = 3;
        final byte arrayPaddingValue = -1;

        ByteBufferUtils.initialize(buffer, arrayPaddingValue);

        buffer.position(offset);
        EasyNetworkUtils.writeInt(i, buffer, ByteOrder.LITTLE_ENDIAN);
        buffer.flip();
        buffer.position(offset);
        assertEquals(i, EasyNetworkUtils.readInt(buffer, ByteOrder.LITTLE_ENDIAN));
        buffer.position(offset);
        assertNotSame(i, EasyNetworkUtils.readInt(buffer, ByteOrder.BIG_ENDIAN));

        ByteBufferUtils.assertPadding(arrayPaddingValue, buffer, offset, INTEGER_SIZE);
    }
}
