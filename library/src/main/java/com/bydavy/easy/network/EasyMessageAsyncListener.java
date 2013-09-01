package com.bydavy.easy.network;

import com.bydavy.easy.network.utils.Checker;

import javax.annotation.Nonnull;

abstract class EasyMessageAsyncListener<T> implements StreamThreadListener<T> {
    @Nonnull
    protected final EasyClient mClient;
    @Nonnull
    protected final EasyClientHandler mHandler;

    public EasyMessageAsyncListener(@Nonnull EasyClient client, @Nonnull EasyClientHandler handler) {
        Checker.nonNull(client);
        Checker.nonNull(handler);

        mClient = client;
        mHandler = handler;
    }

    @Override
    public void onStarted(@Nonnull T obj) {
        mHandler.onEndOfOutputStream(mClient);
    }

    @Override
    public void onStreamClosed(@Nonnull T obj) {
    }

    @Override
    public void onMessageWritten(@Nonnull T obj, @Nonnull EasyMessage message) {
    }

    @Override
    public void onMessageRead(@Nonnull T obj, @Nonnull EasyMessage msg) {
    }

    @Override
    public void onError(@Nonnull T obj) {
        mHandler.onClientError(mClient);
    }

    @Override
    public void onStopped(@Nonnull T obj) {
    }

    static class Writer extends EasyMessageAsyncListener<EasyMessageAsyncWriter> {

        public Writer(@Nonnull EasyClient client, @Nonnull EasyClientHandler handler) {
            super(client, handler);
        }

        @Override
        public void onMessageRead(@Nonnull EasyMessageAsyncWriter obj, @Nonnull EasyMessage msg) {
            throw new IllegalAccessError("Should never be invoked");
        }
    }

    static class Reader extends EasyMessageAsyncListener<EasyMessageAsyncReader> {

        public Reader(@Nonnull EasyClient client, @Nonnull EasyClientHandler handler) {
            super(client, handler);
        }

        @Override
        public void onMessageWritten(@Nonnull EasyMessageAsyncReader obj, @Nonnull EasyMessage message) {
            throw new IllegalAccessError("Should never be invoked");
        }

        @Override
        public void onMessageRead(@Nonnull EasyMessageAsyncReader obj, @Nonnull EasyMessage msg) {
            mHandler.onMessageReceived(mClient, msg);
        }
    }
}
