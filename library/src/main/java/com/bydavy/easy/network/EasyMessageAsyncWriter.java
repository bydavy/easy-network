package com.bydavy.easy.network;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Queue;

public interface EasyMessageAsyncWriter {

    void setListener(@Nonnull StreamThreadListener<EasyMessageAsyncWriter> listener);

    void setIncomingQueue(@Nonnull Queue<EasyMessage> incomingQueue);

    void setChannel(@Nonnull AbstractSelectableChannel socketChannel);

    void endStream() throws IOException;

    void start();

    boolean isStarted();

    void stop();

    void interrupt();

    boolean isWriting();

    boolean write() throws IOException;
}
