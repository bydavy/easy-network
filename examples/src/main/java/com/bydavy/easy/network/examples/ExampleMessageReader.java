package com.bydavy.easy.network.examples;

import com.bydavy.easy.network.*;
import com.bydavy.easy.network.utils.EasyNetworkUtils;
import com.bydavy.easy.network.protocols.PrefixedBySizeReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ExampleMessageReader implements PrefixedBySizeReader {

    @Override
    public int getMessageContentLengthSizeInBytes() {
        return ExampleConstants.MESSAGE_SIZE_LENGTH;
    }

    @Override
    public int getMessageContentSizeMaxInBytes() {
        return ExampleConstants.MESSAGE_SIZE_MAX;
    }

    @Override
    public int readMessageContentLength(@Nonnull ByteBuffer buffer) {
        return EasyNetworkUtils.readShort(buffer, ByteOrder.BIG_ENDIAN);
    }

    @Nullable
    @Override
    public EasyMessage readMessageContent(@Nonnull ByteBuffer buffer) {
        EasyMessage result = null;

        try {
            int messageType = buffer.getInt();
            switch (messageType) {
                case 1:
                    result = new PingMessage(buffer);
                    break;
                case 2:
                    result = new PongMessage(buffer);
                    break;
                case 3:
                    result = new HelloMessage(buffer);
                    break;
                case 4:
                    result = new TextMessage(buffer);
                default:
                    break;
            }
        } catch (IOException e) {
            result = null;
        }

        return result;
    }

    @Nullable
    @Override
    public EasyMessage readMessage(@Nonnull ByteBuffer buffer) {
        readMessageContentLength(buffer);
        return readMessageContent(buffer);
    }
}
