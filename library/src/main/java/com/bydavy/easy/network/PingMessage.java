package com.bydavy.easy.network;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.ByteBuffer;

@Immutable
public class PingMessage implements EasyMessage {


    public PingMessage() {
    }

    public PingMessage(@Nonnull ByteBuffer buffer) throws IOException {
    }

    @Override
    public void writeTo(@Nonnull ByteBuffer buffer) throws IOException {
    }

    @Override
    public String toString() {
        return "PingMessage";
    }
}
