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
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;

class EasyServerTcpImpl implements EasyServerTcp {
    private static final String TAG = "EasyServerTcpImpl";
    private static final boolean DEBUG = EasyInternalSettings.DEBUG ? true : EasyInternalSettings.DEBUG_DEFAULT_FALSE;

    @Nonnull
    private final SocketAddress mAddress;
    @Nonnull
    @GuardedBy("mClients")
    private final List<EasyClientTcp> mClients;

    @Nonnull
    private final Object mLock;
    @Nullable
    @GuardedBy("mLock")
    private EasyServerTcpHandler mHandler;
    @Nullable
    @GuardedBy("mLock")
    private ServerSocketChannel mChannel;
    @Nullable
    @GuardedBy("mLock")
    private EasyServerTcpThread mServerThread;
    @GuardedBy("mLock")
    private boolean mStarted;

    public EasyServerTcpImpl(@Nonnull SocketAddress address) {
        Checker.nonNull(address);

        mAddress = address;
        mLock = new Object();
        mClients = new ArrayList<EasyClientTcp>();
    }

    @Override
    public void setHandler(@Nonnull EasyServerTcpHandler handler) {
        synchronized (mLock) {
            mHandler = handler;
        }
    }

    @Nonnull
    @Override
    public List<EasyClientTcp> getEasyClients() {
        synchronized (mClients) {
            return new ArrayList<EasyClientTcp>(mClients);
        }
    }

    @Override
    public void sendToAll(@Nonnull EasyMessage message) {
        final List<EasyClientTcp> clients;
        synchronized (mClients) {
            clients = new ArrayList<EasyClientTcp>(mClients);
        }

        final int size = clients.size();
        for (int i = 0; i < size; i++) {
            EasyClientTcp client = clients.get(i);
            client.send(message);
        }
    }

    @Override
    public void start() {
        synchronized (mLock) {
            if (mHandler == null) {
                throw new IllegalStateException();
            }
            if (mStarted) {
                throw new IllegalStateException("Server cannot be started twice or reused");
            }
            mStarted = true;

            ServerSocketChannel channel = null;
            try {
                channel = ServerSocketChannel.open();
                channel.configureBlocking(false);
                channel.bind(mAddress);

                mChannel = channel;

                Selector selector = Selector.open();

                mServerThread = new EasyServerTcpThread(this, channel, selector, mHandler);
                mServerThread.start();
            } catch (IOException e) {
                mHandler.onServerError(this);
                EasyNetworkUtils.close(channel);
                mChannel = null;
                LogHelper.e(TAG, LogHelperErrors.NETWORK, "Error while binding socket", e);
            }
        }
    }

    @Override
    public void stop() {
        synchronized (mLock) {
            if (mServerThread != null) {
                mServerThread.finish();
            }
        }
    }

    @Override
    public void interrupt() {
        synchronized (mLock) {
            if (mServerThread != null) {
                mServerThread.interrupt();
                mServerThread = null;
            }
            EasyNetworkUtils.close(mChannel);
            mChannel = null;
        }
    }

    @Override
    public void join() throws InterruptedException {
        join(0);
    }

    @Override
    public void join(@Nonnegative long millis) throws InterruptedException {
        final EasyServerTcpThread serverThread;
        synchronized (mLock) {
            serverThread = mServerThread;
        }

        // Join holds the monitor (moved outside of the synchronized block)
        if (serverThread != null) {
            serverThread.joinThreadAndExecutors(millis);
        }
    }

    /**
     * Thread safe
     */
    void addClient(EasyServerTcpClientImpl client) {
        synchronized (mClients) {
            mClients.add(client);
        }
    }

    /**
     * Thread safe
     */
    void removeClient(EasyServerTcpClientImpl client) {
        synchronized (mClients) {
            mClients.remove(client);
        }
    }
}
