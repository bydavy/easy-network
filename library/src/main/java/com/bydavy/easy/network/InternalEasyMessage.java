package com.bydavy.easy.network;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class InternalEasyMessage implements EasyMessage {

    @Override
    public void writeTo(@Nonnull ByteBuffer buffer) throws IOException {
        throw new IllegalAccessError("Internal messages cannot be send to the wire");
    }
}
