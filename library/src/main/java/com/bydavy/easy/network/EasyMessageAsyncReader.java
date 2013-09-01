package com.bydavy.easy.network;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.channels.spi.AbstractSelectableChannel;

public interface EasyMessageAsyncReader {

    void setListener(@Nonnull StreamThreadListener<EasyMessageAsyncReader> listener);

    void setChannel(@Nonnull AbstractSelectableChannel socketChannel);

    void start();

    void stop();

    boolean isStarted();

    void endStream() throws IOException;

    void interrupt();

    boolean isReading();

    public boolean read() throws IOException;
}
