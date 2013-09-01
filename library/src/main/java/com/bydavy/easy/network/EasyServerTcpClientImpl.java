package com.bydavy.easy.network;


import com.bydavy.easy.network.utils.Checker;
import com.bydavy.easy.network.utils.EasyNetworkUtils;
import com.bydavy.easy.network.utils.LogHelper;
import com.bydavy.easy.network.utils.LogHelperErrors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

class EasyServerTcpClientImpl implements EasyServerTcpClient {
    private static final String TAG = "EasyServerTcpClientImpl";
    private static final boolean DEBUG = EasyInternalSettings.DEBUG ? true : EasyInternalSettings.DEBUG_DEFAULT_FALSE;

    @Nonnegative
    private static final int QUEUE_SIZE = 128;

    public static interface EasyServerTcpClientImplListener {
        void onMessageToSend(EasyServerTcpClientImpl easyServerTcpClient);
    }

    @Nonnull
    private final EasyServerTcpImpl mServer;
    @Nonnull
    private final SocketChannel mChannel;
    @Nonnull
    private final Queue<EasyMessage> mOutgoingQueue;
    @Nonnull
    private final Queue<EasyMessage> mIncomingQueue;
    @Nonnull
    private final EasyServerTcpClientImplListener mListener;
    @Nonnull
    private EasyMessageAsyncReader mMessageAsyncReader;
    @Nonnull
    private EasyMessageAsyncWriter mMessageAsyncWriter;

    @Nonnull
    private final Object mLock;
    @GuardedBy("mLock")
    private boolean mIsStarted;
    @GuardedBy("mLock")
    private boolean mWasStartedAtLeastOnce;
    @Nullable
    @GuardedBy("mLock")
    private EasyClientHandler mHandler;

    public EasyServerTcpClientImpl(@Nonnull EasyServerTcpImpl server, @Nonnull SocketChannel channel, @Nonnull EasyServerTcpClientImplListener listener) {
        Checker.nonNull(server);
        Checker.nonNull(channel);
        Checker.nonNull(listener);

        mLock = new Object();
        mServer = server;
        mChannel = channel;
        mListener = listener;
        mOutgoingQueue = new ArrayBlockingQueue<EasyMessage>(QUEUE_SIZE);
        mIncomingQueue = new ArrayBlockingQueue<EasyMessage>(QUEUE_SIZE);
    }

    public SocketChannel getChannel() {
        return mChannel;
    }

    @Override
    public void setHandler(@Nonnull EasyClientHandler handler) {
        synchronized (mLock) {
            mHandler = handler;
        }
    }

    @Override
    public void setMessageReader(@Nonnull EasyMessageAsyncReader messageReader) {
        synchronized (mLock) {
            mMessageAsyncReader = messageReader;
            mMessageAsyncReader.setChannel(mChannel);
        }
    }

    @Override
    public void setMessageWriter(@Nonnull EasyMessageAsyncWriter messageWriter) {
        synchronized (mLock) {
            mMessageAsyncWriter = messageWriter;
            mMessageAsyncWriter.setChannel(mChannel);
            mMessageAsyncWriter.setIncomingQueue(mOutgoingQueue);
        }
    }

    @Override
    public void start() {
        synchronized (mLock) {
            if (mHandler == null || mMessageAsyncReader == null || mMessageAsyncWriter == null) {
                throw new IllegalStateException();
            }
            if (mWasStartedAtLeastOnce) {
                throw new IllegalStateException("Clients cannot be reused");
            }

            mIsStarted = true;
            mWasStartedAtLeastOnce = true;

            mMessageAsyncReader.setListener(new EasyMessageAsyncListener.Reader(this, mHandler));
            mMessageAsyncWriter.setListener(new EasyMessageAsyncListener.Writer(this, mHandler));
            mMessageAsyncReader.start();
            mMessageAsyncWriter.start();
        }
        mServer.addClient(this);
        mHandler.onClientStart(this);
    }

    @Override
    public void stop() {
        synchronized (mLock) {
            if (!mIsStarted) {
                mMessageAsyncReader.stop();
                mMessageAsyncWriter.stop();
                mIsStarted = false;
            }
        }
    }

    @Override
    public boolean isStarted() {
        synchronized (mLock) {
            return mIsStarted;
        }
    }

    @Override
    public void interrupt() {
        closeChannel();
    }

    private void closeChannel() {
        synchronized (mLock) {
            mIsStarted = false;
            EasyNetworkUtils.close(mChannel);
            mHandler.onClientStop(this);

            mLock.notifyAll();
        }
    }

    public void detectAndNotifyClientStop() {
        if (mIsStarted && !mMessageAsyncReader.isReading() && !mMessageAsyncWriter.isWriting()) {
            closeChannel();
        }
    }

    @Override
    public void send(@Nonnull EasyMessage msg) {
        try {
            mOutgoingQueue.add(msg);
            mListener.onMessageToSend(this);
        } catch (IllegalStateException e) {
            LogHelper.e(TAG, LogHelperErrors.IMPLEMENTATION, "Outgoing queue is full", e);
        }
    }

    @Override
    public void join() throws InterruptedException {
        synchronized (mLock) {
            while (mIsStarted) {
                mLock.wait();
            }
        }
    }

    @Override
    public void join(@Nonnegative long millis) throws InterruptedException {
        synchronized (mLock) {
            long targetTime = System.currentTimeMillis() + millis;
            while (mIsStarted) {
                long timeoutMs = targetTime - System.currentTimeMillis();

                if (timeoutMs <= 0) {
                    break;
                }

                mLock.wait(timeoutMs);
            }
        }
    }

    @Override
    public boolean read() throws IOException {
        try {
            return mMessageAsyncReader.read();
        } catch (IOException e) {
            mHandler.onClientError(this);
            throw e;
        } finally {
            detectAndNotifyClientStop();
        }
    }

    @Override
    public boolean write() throws IOException {
        try {
            return mMessageAsyncWriter.write();
        } catch (IOException e) {
            mHandler.onClientError(this);
            throw e;
        } finally {
            detectAndNotifyClientStop();
        }
    }

}
