package com.bydavy.easy.network;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface EasyMessage {
    void writeTo(@Nonnull ByteBuffer buffer) throws IOException;
}
