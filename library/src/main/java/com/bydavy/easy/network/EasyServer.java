package com.bydavy.easy.network;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

interface EasyServer<T extends EasyServerHandler> {
    void start();

    void stop();

    void interrupt();

    void join() throws InterruptedException;

    void join(@Nonnegative long millis) throws InterruptedException;

    void setHandler(@Nonnull T handler);
}
