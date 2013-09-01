package com.bydavy.easy.network;

import javax.annotation.Nonnull;

public interface StreamThreadListener<T> {
    void onStarted(@Nonnull T obj);

    void onStreamClosed(@Nonnull T obj);

    void onMessageWritten(@Nonnull T obj, @Nonnull EasyMessage message);

    void onMessageRead(@Nonnull T obj, @Nonnull EasyMessage msg);

    void onError(@Nonnull T obj);

    void onStopped(@Nonnull T obj);
}