package com.bydavy.easy.network.examples;

import java.nio.ByteOrder;

public class ExampleConstants {

    public static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    public static final int MESSAGE_SIZE_LENGTH = 2;
    public static final int MESSAGE_TYPE_LENGTH = 2;
    public static final int MESSAGE_SIZE_MAX = 2048;

    private ExampleConstants() {
        throw new IllegalAccessError();
    }
}
