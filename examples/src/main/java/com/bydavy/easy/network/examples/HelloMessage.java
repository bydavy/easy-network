package com.bydavy.easy.network.examples;

import com.bydavy.easy.network.EasyMessage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HelloMessage implements EasyMessage {

    public HelloMessage() {
    }

    public HelloMessage(@Nonnull ByteBuffer buffer) throws IOException {
        buffer.getInt();
    }

    @Override
    public void writeTo(@Nonnull ByteBuffer buffer) throws IOException {
        buffer.putInt(42);
    }

    @Override
    public String toString() {
        return "HelloMessage";
    }
}
