package com.bydavy.easy.network;

import com.bydavy.easy.network.utils.Checker;
import com.bydavy.easy.network.utils.EasyNetworkUtils;
import com.bydavy.easy.network.utils.LogHelper;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class EasyClientTcpImpl implements EasyClientTcp {
    private static final String TAG = "EasyClientTcpImpl";
    private static final boolean DEBUG = EasyInternalSettings.DEBUG ? true : EasyInternalSettings.DEBUG_DEFAULT_FALSE;

    @Nonnegative
    private static final int QUEUE_SIZE = 128;

    @Nonnull
    private final SocketAddress mAddress;
    @Nonnull
    private final BlockingQueue<EasyMessage> mOutgoingQueue;
    @Nonnull
    private final BlockingQueue<EasyMessage> mIncomingQueue;

    @Nonnull
    private final Object mLock;
    @GuardedBy("mLock")
    private boolean isStarted;
    @GuardedBy("mLock")
    private boolean wasStartedAtLeastOnce;
    @Nullable
    @GuardedBy("mLock")
    private EasyClientTcpThread mThread;
    @Nullable
    @GuardedBy("mLock")
    private EasyClientHandler mHandler;
    @Nullable
    @GuardedBy("mLock")
    private EasyMessageAsyncWriter mMessageWriter;
    @Nullable
    @GuardedBy("mLock")
    private EasyMessageAsyncReader mMessageReader;
    @Nullable
    @GuardedBy("mLock")
    private SocketChannel mSocket;

    public EasyClientTcpImpl(@Nonnull SocketAddress address) {
        Checker.nonNull(address);

        mAddress = address;

        mLock = new Object();
        mOutgoingQueue = new ArrayBlockingQueue<EasyMessage>(QUEUE_SIZE);
        mIncomingQueue = new ArrayBlockingQueue<EasyMessage>(QUEUE_SIZE);
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
            mMessageReader = messageReader;
        }
    }

    @Override
    public void setMessageWriter(@Nonnull EasyMessageAsyncWriter messageWriter) {
        synchronized (mLock) {
            mMessageWriter = messageWriter;
            mMessageWriter.setIncomingQueue(mOutgoingQueue);
        }
    }

    @Override
    public void start() {
        start(0);
    }

    public void start(int timeout) {
        synchronized (mLock) {
            if (wasStartedAtLeastOnce || mHandler == null || mMessageReader == null || mMessageWriter == null) {
                throw new IllegalStateException();
            }

            SocketChannel socket = null;
            try {
                socket = SocketChannel.open();
                mSocket = socket;

                socket.configureBlocking(true);
                socket.socket().connect(mAddress, timeout);
                if (DEBUG) {
                    LogHelper.d(TAG, "Connected to " + mAddress);
                }
                socket.configureBlocking(false);

                Selector selector = Selector.open();

                mMessageReader.setListener(new EasyMessageAsyncListener.Reader(this, mHandler));
                mMessageWriter.setListener(new EasyMessageAsyncListener.Writer(this, mHandler));
                mMessageReader.setChannel(mSocket);
                mMessageWriter.setChannel(mSocket);

                mThread = new EasyClientTcpThread(this, mHandler, mSocket, selector, mMessageReader, mMessageWriter);
                mThread.start();

                isStarted = true;
                wasStartedAtLeastOnce = true;
            } catch (IOException e) {
                mHandler.onClientError(this);
                closeSocket();
            }
        }
    }

    @Override
    public void stop() {
        synchronized (mLock) {
            if (isStarted) {
                if (mThread != null) {
                    mThread.finish();
                }
                isStarted = false;
            }
        }
    }

    @Override
    public boolean isStarted() {
        synchronized (mLock) {
            return isStarted;
        }
    }

    @Override
    public void interrupt() {
        synchronized (mLock) {
            if (mThread != null) {
                mThread.interrupt();
            }
            closeSocket();
        }
    }

    private void closeSocket() {
        synchronized (mLock) {
            EasyNetworkUtils.close(mSocket);
            mSocket = null;
            isStarted = false;

            mHandler.onClientStop(this);
        }
    }

    @Override
    public void send(@Nonnull EasyMessage msg) {
        if (!wasStartedAtLeastOnce) {
            throw new IllegalStateException();
        }
        mOutgoingQueue.add(msg);
        synchronized (mLock) {
            if (mThread != null) {
                mThread.messageWaitingToBeSend();
            }
        }
    }

    @Override
    public void join() throws InterruptedException {
        join(0);
    }

    @Override
    public void join(@Nonnegative long millis) throws InterruptedException {
        Thread thread = null;

        synchronized (mLock) {
            if (mThread != null) {
                thread = mThread;
            }
        }

        // Join holds the monitor (moved outside of the synchronized statement)
        thread.join(millis);
    }
}
