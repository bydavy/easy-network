package com.bydavy.easy.network;

import com.bydavy.easy.network.utils.Checker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public abstract class EasyMessageBaseAsyncReader implements EasyMessageAsyncReader {
    @Nullable
    protected StreamThreadListener<EasyMessageAsyncReader> mListener;

    @Nonnull
    protected final Object mLock;
    @GuardedBy("mLock")
    private boolean mIstarted;
    @GuardedBy("mLock")
    private boolean mWasStartedAtLeastOnce;
    @Nullable
    @GuardedBy("mLock")
    private SocketChannel mChannel;

    public EasyMessageBaseAsyncReader() {
        mLock = new Object();
    }

    @Override
    public void setListener(@Nonnull StreamThreadListener<EasyMessageAsyncReader> listener) {
        mListener = listener;
    }

    @Override
    public void setChannel(@Nonnull AbstractSelectableChannel channel) {
        Checker.nonNull(channel);
        if (!(channel instanceof SocketChannel)) {
            throw new IllegalArgumentException();
        }

        synchronized (mLock) {
            mChannel = (SocketChannel) channel;
        }
    }

    protected SocketChannel getChannel() {
        synchronized (mLock) {
            return mChannel;
        }
    }

    @Override
    public void start() {
        synchronized (mLock) {
            if (mWasStartedAtLeastOnce) {
                throw new IllegalStateException();
            }
            mIstarted = true;
            mWasStartedAtLeastOnce = true;
            mListener.onStarted(this);
        }
    }

    @Override
    public boolean isStarted() {
        synchronized (mLock) {
            return mIstarted;
        }
    }

    @Override
    public void endStream() throws IOException {
        synchronized (mLock) {
            try {
                if (mChannel != null) {
                    mChannel.shutdownInput();
                }
                mListener.onStreamClosed(this);
            } catch (IOException e) {
                mListener.onError(this);
                throw e;
            }
        }
    }

    @Override
    public void stop() {
        synchronized (mLock) {
            if (mIstarted) {
                mIstarted = false;
                mListener.onStopped(this);
            }
        }
    }

    @Override
    public void interrupt() {
        synchronized (mLock) {
            if (mIstarted) {
                mIstarted = false;
                mListener.onStopped(this);
            }
        }
    }

    @Override
    public boolean isReading() {
        return mIstarted;
    }
}
