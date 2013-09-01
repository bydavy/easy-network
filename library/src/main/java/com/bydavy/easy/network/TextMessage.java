package com.bydavy.easy.network;

import com.bydavy.easy.network.utils.Checker;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TextMessage implements EasyMessage {
    private final String mText;

    public TextMessage(@Nonnull String text) {
        Checker.nonNull(text);

        mText = text;
    }

    public TextMessage(@Nonnull ByteBuffer buffer) throws IOException {
        final int size = buffer.getInt();
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        mText = new String(bytes);
    }

    @Override
    public void writeTo(@Nonnull ByteBuffer buffer) throws IOException {
        buffer.putInt(mText.length());
        buffer.put(mText.getBytes());
    }

    @Override
    public String toString() {
        return "TextMessage: " + mText;
    }
}
