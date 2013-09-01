package com.bydavy.easy.network;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PongMessage implements EasyMessage {


    public PongMessage() {
    }

    public PongMessage(@Nonnull ByteBuffer buffer) throws IOException {
    }

    @Override
    public void writeTo(@Nonnull ByteBuffer buffer) throws IOException {
    }

    @Override
    public String toString() {
        return "PongMessage";
    }
}
