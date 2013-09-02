package com.bydavy.easy.network;


import com.bydavy.easy.network.utils.Checker;
import com.bydavy.easy.network.utils.EasyNetworkUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.GuardedBy;
import java.io.IOException;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

public class SelectorLooper implements Runnable {

    public interface SelectorMainLoopCallback {
        void connectable(@Nonnull SelectorLooper selectorMainLoop, @Nonnull SelectionKey key);

        void writable(@Nonnull SelectorLooper selectorMainLoop, @Nonnull SelectionKey key);

        void readable(@Nonnull SelectorLooper selectorMainLoop, @Nonnull SelectionKey key);

        void acceptable(@Nonnull SelectorLooper selectorMainLoop, @Nonnull SelectionKey key);
    }

    @Nonnull
    private final Selector mSelector;
    @Nonnull
    private final SelectorMainLoopCallback mCallback;
    @Nonnull
    private final Object mFinishLock;
    @GuardedBy("mFinishLock")
    private boolean finish;
    @Nonnull
    @GuardedBy("mTasks")
    private final Queue<Runnable> mTasks;
    @Nullable
    private volatile Thread mThread;

    /**
     * The main loop manages the selector lifecycle including the close
     */
    public SelectorLooper(@Nonnull @WillClose Selector selector, @Nonnull SelectorMainLoopCallback callback) {
        Checker.nonNull(selector);
        Checker.nonNull(callback);

        mSelector = selector;
        mCallback = callback;
        mFinishLock = new Object();
        mTasks = new ArrayDeque<Runnable>();
    }

    @Override
    public void run() {
        mThread = Thread.currentThread();
        try {
            while (!isFinishFlagSet()) {
                int keys = mSelector.select();
                if (keys != 0) {
                    final Set<SelectionKey> selectionKeys = mSelector.selectedKeys();
                    final Iterator<SelectionKey> iter = selectionKeys.iterator();
                    while (iter.hasNext()) {
                        try {
                            SelectionKey key = iter.next();
                            if (key.isConnectable()) {
                                mCallback.connectable(this, key);
                            }
                            if (key.isAcceptable()) {
                                mCallback.acceptable(this, key);
                            }
                            if (key.isReadable()) {
                                mCallback.readable(this, key);
                            }
                            if (key.isWritable()) {
                                mCallback.writable(this, key);
                            }
                        } catch (CancelledKeyException e) {
                            // NOPMD - Nothing
                        }
                        // Consumed the key's ready opts
                        iter.remove();
                    }
                }
                iterate();
            }
        } catch (IOException e) {
            // NOPMD - Nothing
        } finally {
            EasyNetworkUtils.close(mSelector);
        }
        mThread = null;
    }

    protected void iterate() {
        synchronized (mTasks) {
            while (!mTasks.isEmpty()) {
                mTasks.poll().run();
            }
        }
    }

    /**
     * Thread safe
     */
    public void post(Runnable runnable) {
        synchronized (mTasks) {
            mTasks.add(runnable);
        }
        // Wake up the thread
        mSelector.wakeup();
    }

    /**
     * Thread safe
     */
    protected void notInterestedIn(final SelectableChannel channel, final int ops) {
        if (Thread.currentThread() == mThread) {
            internalNotInterestedIn(channel, ops);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    internalNotInterestedIn(channel, ops);
                }
            });
        }
    }

    private void internalNotInterestedIn(SelectableChannel channel, int ops) {
        SelectionKey key = channel.keyFor(mSelector);
        if (key != null) {
            int interestOps = key.interestOps();
            if ((interestOps & ops) == ops) {
                key.interestOps(interestOps & ~ops);
            }
        }
    }

    /**
     * Thread safe
     */
    protected void interestedIn(final SelectableChannel channel, final Object attachment, final int ops) {
        if (Thread.currentThread() == mThread) {
            internalInterestedIn(channel, attachment, ops);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    internalInterestedIn(channel, attachment, ops);
                }
            });
        }
    }

    private void internalInterestedIn(SelectableChannel channel, Object attachment, int ops) {
        SelectionKey key = channel.keyFor(mSelector);
        if (key != null) {
            int interestOps = key.interestOps();
            key.interestOps(interestOps | ops);
        } else {
            try {
                SelectionKey register = channel.register(mSelector, ops);
                register.attach(attachment);
            } catch (ClosedChannelException e) {
                // NOPMD - Nothing
            }
        }
    }

    /**
     * Thread safe
     */
    public void interestedInWrite(SelectableChannel channel, Object attachement) {
        interestedIn(channel, attachement, SelectionKey.OP_WRITE);
    }

    /**
     * Thread safe
     */
    public void interestedInRead(SelectableChannel channel, Object attachement) {
        interestedIn(channel, attachement, SelectionKey.OP_READ);
    }

    /**
     * Thread safe
     */
    public void interestedInAccept(SelectableChannel channel, Object attachement) {
        interestedIn(channel, attachement, SelectionKey.OP_ACCEPT);
    }

    /**
     * Thread safe
     */
    public void interestedInConnect(SelectableChannel channel, Object attachement) {
        interestedIn(channel, attachement, SelectionKey.OP_CONNECT);
    }

    /**
     * Thread safe
     */
    public void notInterestedInWrite(SelectableChannel channel) {
        notInterestedIn(channel, SelectionKey.OP_WRITE);
    }

    /**
     * Thread safe
     */
    public void noInterestedInRead(SelectableChannel channel) {
        notInterestedIn(channel, SelectionKey.OP_READ);
    }

    /**
     * Thread safe
     */
    public void notInterestedInAccept(SelectableChannel channel) {
        notInterestedIn(channel, SelectionKey.OP_ACCEPT);
    }

    /**
     * Thread safe
     */
    public void notInterestedInConnect(SelectableChannel channel) {
        notInterestedIn(channel, SelectionKey.OP_CONNECT);
    }

    private boolean isFinishFlagSet() {
        synchronized (mFinishLock) {
            return finish;
        }
    }

    /**
     * Thread safe
     */
    public void finish() {
        synchronized (mFinishLock) {
            finish = true;
            mSelector.wakeup();
        }
    }
}
