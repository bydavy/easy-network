package com.bydavy.easy.network;

import com.bydavy.easy.network.utils.Checker;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.nio.ByteBuffer;


@Immutable
public class TextMessage implements EasyMessage {
    private static final String ENCODING = "UTF-8";

    @Nonnull
    private final String mText;

    public TextMessage(@Nonnull String text) {
        Checker.nonNull(text);

        mText = text;
    }

    public TextMessage(@Nonnull ByteBuffer buffer) throws IOException {
        final int size = buffer.getInt();
        byte[] bytes = new byte[size];
        buffer.get(bytes);
        mText = new String(bytes, ENCODING);
    }

    @Override
    public void writeTo(@Nonnull ByteBuffer buffer) throws IOException {
        buffer.putInt(mText.length());
        buffer.put(mText.getBytes(ENCODING));
    }

    @Override
    public String toString() {
        return "TextMessage: " + mText;
    }
}
