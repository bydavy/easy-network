package com.bydavy.easy.network.examples;

import com.bydavy.easy.network.*;
import com.bydavy.easy.network.protocols.PrefixedBySizeWriter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ExampleMessageWriter implements PrefixedBySizeWriter {

    @Override
    public void writeMessage(@Nonnull EasyMessage message, @Nonnull ByteBuffer buffer) throws IOException {
        final int initialPosition = buffer.position();
        for (int i = 0; i < ExampleConstants.MESSAGE_SIZE_LENGTH; i++) {
            buffer.put((byte) 0);
        }

        if (message instanceof PingMessage) {
            buffer.putInt(1);
        } else if (message instanceof PongMessage) {
            buffer.putInt(2);
        } else if (message instanceof HelloMessage) {
            buffer.putInt(3);
        }  else if (message instanceof TextMessage) {
            buffer.putInt(4);
        }

        message.writeTo(buffer);

        final int endPosition = buffer.position();

        buffer.position(initialPosition);
        buffer.putShort((short) (endPosition - initialPosition - ExampleConstants.MESSAGE_SIZE_LENGTH));

        buffer.position(endPosition);
    }

    @Override
    public int getEasyMessageSizeMaxInBytes() {
        return ExampleConstants.MESSAGE_SIZE_MAX;
    }
}
