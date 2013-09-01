package com.bydavy.easy.network;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;

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
