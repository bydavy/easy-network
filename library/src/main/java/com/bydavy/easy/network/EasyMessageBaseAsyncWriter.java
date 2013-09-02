package com.bydavy.easy.network;

import com.bydavy.easy.network.utils.Checker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Queue;

public abstract class EasyMessageBaseAsyncWriter implements EasyMessageAsyncWriter {
    @Nonnull
    protected final Object mLock;
    @GuardedBy("mLock")
    protected boolean mIsStarted;
    @GuardedBy("mLock")
    private boolean mWasStartedAtLeastOnce;
    @GuardedBy("mLock")
    private boolean mPendingStop;
    @Nullable
    @GuardedBy("mLock")
    private SocketChannel mChannel;
    @Nullable
    @GuardedBy("mLock")
    protected StreamThreadListener<EasyMessageAsyncWriter> mListener;
    @Nullable
    protected Queue<EasyMessage> mIncomingQueue;


    public EasyMessageBaseAsyncWriter() {
        mLock = new Object();
    }

    @Override
    public void setListener(@Nonnull StreamThreadListener<EasyMessageAsyncWriter> listener) {
        mListener = listener;
    }

    @Override
    public void setIncomingQueue(@Nonnull Queue<EasyMessage> incomingQueue) {
        mIncomingQueue = incomingQueue;
    }

    @Override
    public void setChannel(@Nonnull AbstractSelectableChannel channel) {
        Checker.nonNull(channel);
        if (!(channel instanceof SocketChannel)) {
            throw new IllegalArgumentException();
        }

        mChannel = (SocketChannel) channel;
    }

    public SocketChannel getChannel() {
        return mChannel;
    }

    public void endStream() throws IOException {
        synchronized (mLock) {
            try {
                if (mChannel != null) {
                    mChannel.shutdownOutput();
                }
                mListener.onStreamClosed(this);
            } catch (IOException e) {
                mListener.onError(this);
                throw e;
            }
        }
    }

    public void start() {
        synchronized (mLock) {
            if (mWasStartedAtLeastOnce) {
                throw new IllegalStateException();
            }

            mIsStarted = true;
            mWasStartedAtLeastOnce = true;
            mListener.onStarted(this);
        }
    }

    @Override
    public boolean isStarted() {
        synchronized (mLock) {
            return mIsStarted;
        }
    }

    public void stop() {
        synchronized (mLock) {
            if (!mPendingStop) {
                mIncomingQueue.add(InternalEasyMessageStop.INSTANCE);
                mPendingStop = true;
            }
        }
    }

    protected boolean isStopMessage(EasyMessage easyMessage) {
        return easyMessage instanceof InternalEasyMessageStop;
    }

    public void interrupt() {
        synchronized (mLock) {
            mIsStarted = false;
        }
    }
}
