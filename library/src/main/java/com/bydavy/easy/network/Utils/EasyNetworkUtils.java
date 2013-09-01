package com.bydavy.easy.network.utils;

import javax.annotation.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public final class EasyNetworkUtils {

    private static short byteSwapShort(short value) {
        short b1 = (short) ((value >> 0) & 0xFF);
        short b2 = (short) ((value >> 8) & 0xFF);

        return (short) (b1 << 8 | b2 << 0);
    }

    public static void writeShort(short s, @Nonnull ByteBuffer buffer, @Nonnull ByteOrder byteOrder) {
        // FIXME : Should we swap byte for an short ?!
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            s = byteSwapShort(s);
        }

        buffer.putShort(s);
    }

    public static short readShort(@Nonnull ByteBuffer buffer, @Nonnull ByteOrder byteOrder) {
        short result = buffer.getShort();

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            result = byteSwapShort(result);
        }

        return result;
    }

    private static int byteSwapInt(int value) {
        int b1 = (value >> 0) & 0xFF;
        int b2 = (value >> 8) & 0xFF;
        int b3 = (value >> 16) & 0xFF;
        int b4 = (value >> 24) & 0xFF;

        return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
    }

    public static void writeInt(int i, @Nonnull ByteBuffer buffer, @Nonnull ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            i = byteSwapInt(i);
        }

        buffer.putInt(i);
    }

    public static int readInt(@Nonnull ByteBuffer buffer, @Nonnull ByteOrder byteOrder) {
        int result = buffer.getInt();

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            result = byteSwapInt(result);
        }

        return result;
    }

    private static long byteSwapLong(long value) {
        long b1 = (value >> 0) & 0xFF;
        long b2 = (value >> 8) & 0xFF;
        long b3 = (value >> 16) & 0xFF;
        long b4 = (value >> 24) & 0xFF;
        long b5 = (value >> 32) & 0xFF;
        long b6 = (value >> 40) & 0xFF;
        long b7 = (value >> 48) & 0xFF;
        long b8 = (value >> 56) & 0xFF;

        return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
                b5 << 24 | b6 << 16 | b7 << 8 | b8 << 0;
    }

    public static void writeLong(long l, @Nonnull ByteBuffer buffer, @Nonnull ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            l = byteSwapLong(l);
        }

        buffer.putLong(l);
    }

    public static long readLong(@Nonnull ByteBuffer buffer, @Nonnull ByteOrder byteOrder) {
        long result = buffer.getLong();

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            result = byteSwapLong(result);
        }

        return result;
    }

    public static boolean write(@Nonnull @WillNotClose SocketChannel channel, @Nonnull ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int write = channel.write(buffer);
            if (write == -1) {
                return false;
            }
        }

        return true;
    }

    public static boolean read(@Nonnull @WillNotClose SocketChannel channel, @Nonnull ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read == -1) {
                return false;
            }
        }

        return true;
    }

    public static void close(@Nullable @WillClose java.io.Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Nothing
            }
        }
    }

    private EasyNetworkUtils() {
        throw new IllegalAccessError();
    }


}
