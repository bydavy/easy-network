package com.bydavy.easy.network.protocols;

import com.bydavy.easy.network.EasyMessage;

import javax.annotation.Nonnull;

import javax.annotation.Nonnegative;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface PrefixedBySizeWriter {

    void writeMessage(@Nonnull EasyMessage message, @Nonnull ByteBuffer buffer) throws IOException;

    @Nonnegative
    int getEasyMessageSizeMaxInBytes();
}
