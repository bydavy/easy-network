package com.bydavy.easy.network;

import com.bydavy.easy.network.utils.Checker;
import com.bydavy.easy.network.utils.EasyNetworkUtils;
import com.bydavy.easy.network.utils.LogHelper;
import com.bydavy.easy.network.utils.LogHelperErrors;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class EasyServerTcpThread extends Thread implements SelectorLooper.SelectorMainLoopCallback, EasyServerTcpClientImpl.EasyServerTcpClientImplListener {
    private static final String TAG = "EasyServerTcpThread";
    private static final boolean DEBUG = EasyInternalSettings.DEBUG ? true : EasyInternalSettings.DEBUG_DEFAULT_FALSE;
    private static final boolean DEBUG_SELECTOR = EasyInternalSettings.DEBUG ? false : EasyInternalSettings.DEBUG_DEFAULT_FALSE;
    private static final String THREAD_NAME = "EasyServer TCP Thread";

    @Nonnull
    private final EasyServerTcpImpl mServer;
    @Nonnull
    private final ServerSocketChannel mChannel;
    @Nonnull
    private final EasyServerTcpHandler mHandler;
    @Nonnull
    private final Map<SocketChannel, EasyServerTcpClient> mClients;
    @Nonnull
    private final SelectorLooper mSelectorLooper;

    public EasyServerTcpThread(@Nonnull EasyServerTcpImpl server, @Nonnull ServerSocketChannel channel, @Nonnull Selector selector, @Nonnull EasyServerTcpHandler handler) {
        super(THREAD_NAME);
        Checker.nonNull(server);
        Checker.nonNull(channel);
        Checker.nonNull(handler);
        Checker.nonNull(selector);

        mServer = server;
        mChannel = channel;
        mHandler = handler;
        mClients = new HashMap<SocketChannel, EasyServerTcpClient>();
        mSelectorLooper = new SelectorLooper(selector, this);
    }

    @Override
    public void run() {
        if (DEBUG) {
            LogHelper.d(TAG, "Starting " + getName());
        }
        mHandler.onServerStart(mServer);

        mSelectorLooper.interestedInAccept(mChannel, null);
        mSelectorLooper.run();

        // Too late to properly stop clients (selector main loop exited)
        for (EasyServerTcpClient client : mClients.values()) {
            client.interrupt();
        }

        mHandler.onServerStop(mServer);
        EasyNetworkUtils.close(mChannel);
        if (DEBUG) {
            LogHelper.d(TAG, "Stopped " + getName());
        }
    }

    @Override
    public void connectable(@Nonnull SelectorLooper selectorMainLoop, @Nonnull SelectionKey key) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void acceptable(@Nonnull SelectorLooper selectorMainLoop, @Nonnull SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            if (DEBUG_SELECTOR) {
                LogHelper.d(TAG, "Accepted " + clientChannel);
            }
            onClientAccepted(clientChannel);
        } catch (IOException e) {
            LogHelper.e(TAG, LogHelperErrors.NETWORK, "Error while accepting a client", e);
        }
    }

    private void onClientAccepted(@Nonnull SocketChannel channel) {
        try {
            // Non blocking I/O
            channel.configureBlocking(false);

            EasyServerTcpClientImpl client = new EasyServerTcpClientImpl(mServer, channel, this);
            mClients.put(channel, client);

            // Let the user decide what to do with it
            mHandler.onClientConnected(mServer, client);

            // If user started the client we keep it an listen to incoming stream
            if (client.isStarted()) {
                mSelectorLooper.interestedInRead(channel, client);
            } else {
                disconnectClient(channel);
            }
        } catch (IOException e) {
            disconnectClient(channel);
        }
    }

    @Override
    public void readable(@Nonnull SelectorLooper selectorMainLoop, @Nonnull SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        if (DEBUG_SELECTOR) {
            LogHelper.d(TAG, "Read " + clientChannel);
        }
        onClientRead((EasyServerTcpClientImpl) key.attachment());
    }

    private void onClientRead(@Nonnull EasyServerTcpClientImpl client) {
        try {
            client.read();
        } catch (IOException e) {
            LogHelper.e(TAG, LogHelperErrors.NETWORK, "Error while reading data from client " + client.getChannel(), e);
        }
    }

    @Override
    public void writable(@Nonnull SelectorLooper selectorMainLoop, @Nonnull SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        if (DEBUG_SELECTOR) {
            LogHelper.d(TAG, "Write " + clientChannel);
        }
        onClientWrite((EasyServerTcpClientImpl) key.attachment());
    }

    private void onClientWrite(@Nonnull EasyServerTcpClientImpl client) {
        try {
            if (!client.write()) {
                mSelectorLooper.notInterestedInWrite(client.getChannel());
            }
        } catch (IOException e) {
            LogHelper.e(TAG, LogHelperErrors.NETWORK, "Error while writing data to client " + client.getChannel(), e);
        }
    }

    private void disconnectClient(@Nonnull SocketChannel client) {
        EasyNetworkUtils.close(client);
        mClients.remove(client);
    }

    public void finish() {
        mSelectorLooper.finish();
    }

    public void joinThreadAndExecutors() throws InterruptedException {
        joinThreadAndExecutors(0);
    }

    public void joinThreadAndExecutors(@Nonnegative long millis) throws InterruptedException {
        long targetTime = System.currentTimeMillis() + millis;
        super.join(millis);
        long timeoutMs = targetTime - System.currentTimeMillis();
        if (timeoutMs < 0) {
            timeoutMs = 0;
        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    @Override
    public void onMessageToSend(final @Nonnull EasyServerTcpClientImpl easyServerTcpClient) {
        mSelectorLooper.interestedInWrite(easyServerTcpClient.getChannel(), easyServerTcpClient);
    }
}
