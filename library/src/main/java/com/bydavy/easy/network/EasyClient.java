package com.bydavy.easy.network;


import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public interface EasyClient<T extends EasyClientHandler> {

    void start();

    void stop();

    void interrupt();

    void send(@Nonnull EasyMessage msg);

    void join() throws InterruptedException;

    void join(@Nonnegative long millis) throws InterruptedException;

    void setHandler(@Nonnull T handler);

    void setMessageReader(@Nonnull EasyMessageAsyncReader messageReader);

    void setMessageWriter(@Nonnull EasyMessageAsyncWriter messageWriter);

    boolean isStarted();
}
