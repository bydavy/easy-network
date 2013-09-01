package com.bydavy.easy.network.protocols;

import com.bydavy.easy.network.EasyMessage;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public interface PrefixedBySizeReader {
    @Nonnegative
    int getMessageContentLengthSizeInBytes();

    @Nonnegative
    int getMessageContentSizeMaxInBytes();

    @Nonnegative
    int readMessageContentLength(@Nonnull ByteBuffer buffer);

    @Nullable
    EasyMessage readMessageContent(@Nonnull ByteBuffer buffer);

    @Nullable
    EasyMessage readMessage(@Nonnull ByteBuffer buffer);
}
