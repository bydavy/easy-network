package com.bydavy.easy.network;

import com.bydavy.easy.network.utils.Checker;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;

public class EasyClientTcpHandlerExecutor implements EasyClientHandler<EasyClientTcp> {

    @Nonnull
    private final Executor mExecutor;
    @Nonnull
    private final EasyClientHandler<EasyClientTcp> mHandler;

    public EasyClientTcpHandlerExecutor(@Nonnull Executor executor, @Nonnull EasyClientHandler<EasyClientTcp> handler) {
        Checker.nonNull(executor);
        Checker.nonNull(handler);

        mExecutor = executor;
        mHandler = handler;
    }

    @Override
    public void onMessageReceived(final @Nonnull EasyClientTcp easyClient, final @Nonnull EasyMessage message) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mHandler.onMessageReceived(easyClient, message);
            }
        });
    }

    @Override
    public void onClientError(final @Nonnull EasyClientTcp easyClient) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mHandler.onClientError(easyClient);
            }
        });
    }

    @Override
    public void onClientStart(final @Nonnull EasyClientTcp easyClient) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mHandler.onClientStart(easyClient);
            }
        });
    }

    @Override
    public void onClientStop(final @Nonnull EasyClientTcp easyClient) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mHandler.onClientStop(easyClient);
            }
        });
    }

    @Override
    public void onEndOfInputStream(final @Nonnull EasyClientTcp easyClient) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mHandler.onEndOfInputStream(easyClient);
            }
        });
    }

    @Override
    public void onEndOfOutputStream(final @Nonnull EasyClientTcp easyClient) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mHandler.onEndOfOutputStream(easyClient);
            }
        });
    }
}
