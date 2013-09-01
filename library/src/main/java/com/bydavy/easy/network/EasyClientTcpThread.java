package com.bydavy.easy.network;

import com.bydavy.easy.network.utils.Checker;
import com.bydavy.easy.network.utils.EasyNetworkUtils;
import com.bydavy.easy.network.utils.LogHelper;
import com.bydavy.easy.network.utils.LogHelperErrors;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

class EasyClientTcpThread extends Thread implements SelectorLooper.SelectorMainLoopCallback {

    private static final String TAG = "EasyClientTcpThread";
    private static final boolean DEBUG = EasyInternalSettings.DEBUG ? true : EasyInternalSettings.DEBUG_DEFAULT_FALSE;
    private static final boolean DEBUG_SELECTOR = EasyInternalSettings.DEBUG ? false : EasyInternalSettings.DEBUG_DEFAULT_FALSE;

    private static final String THREAD_NAME = "EasyClientTcp Thread";

    @Nonnull
    private final SocketChannel mSocket;
    @Nonnull
    private final Selector mSelector;
    @Nonnull
    private final EasyMessageAsyncReader mMessageReader;
    @Nonnull
    private final EasyMessageAsyncWriter mMessageWriter;
    @Nonnull
    private final SelectorLooper mSelectorLooper;
    @Nonnull
    private final EasyClientTcpImpl mClient;
    @Nonnull
    private final EasyClientHandler mHandler;

    public EasyClientTcpThread(@Nonnull EasyClientTcpImpl client, @Nonnull EasyClientHandler handler, @Nonnull SocketChannel socket, @Nonnull Selector selector, @Nonnull EasyMessageAsyncReader messageReader, @Nonnull EasyMessageAsyncWriter messageWriter) {
        super(THREAD_NAME);
        Checker.nonNull(client);
        Checker.nonNull(handler);
        Checker.nonNull(socket);
        Checker.nonNull(selector);
        Checker.nonNull(messageReader);
        Checker.nonNull(messageWriter);

        mClient = client;
        mHandler = handler;
        mSocket = socket;
        mSelector = selector;
        mMessageReader = messageReader;
        mMessageWriter = messageWriter;

        mSelectorLooper = new SelectorLooper(selector, this);
    }

    public void finish() {
        mSelectorLooper.finish();
    }

    @Override
    public void run() {
        if (DEBUG) {
            LogHelper.d(TAG, "Starting " + getName());
        }


        mHandler.onClientStart(mClient);

        mMessageReader.start();
        mMessageWriter.start();

        mSelectorLooper.interestedInRead(mSocket, null);
        mSelectorLooper.run();


        mHandler.onClientStop(mClient);
        closeSocket();

        if (DEBUG) {
            LogHelper.d(TAG, "Stopping " + getName());
        }
    }

    private void closeSocket() {
        EasyNetworkUtils.close(mSocket);
    }

    @Override
    public void connectable(SelectorLooper selectorMainLoop, SelectionKey key) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void acceptable(SelectorLooper selectorMainLoop, SelectionKey key) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void writable(SelectorLooper selectorMainLoop, SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        if (DEBUG_SELECTOR) {
            LogHelper.d(TAG, "Write " + clientChannel);
        }
        try {
            if (!mMessageWriter.write()) {
                mSelectorLooper.notInterestedInWrite(clientChannel);
            }
        } catch (IOException e) {
            LogHelper.e(TAG, LogHelperErrors.NETWORK, "Error while writing data from channel", e);
        } finally {
            detectAndNotifyClientStop();
        }
    }

    @Override
    public void readable(SelectorLooper selectorMainLoop, SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        if (DEBUG_SELECTOR) {
            LogHelper.d(TAG, "Read " + clientChannel);
        }
        try {
            mMessageReader.read();
        } catch (IOException e) {
            LogHelper.e(TAG, LogHelperErrors.NETWORK, "Error while reading data from channel", e);
        } finally {
            detectAndNotifyClientStop();
        }
    }

    public void detectAndNotifyClientStop() {
        if (!mMessageReader.isReading() && !mMessageWriter.isWriting()) {
            closeSocket();
        }
    }

    public void messageWaitingToBeSend() {
        mSelectorLooper.interestedInWrite(mSocket, null);
    }
}