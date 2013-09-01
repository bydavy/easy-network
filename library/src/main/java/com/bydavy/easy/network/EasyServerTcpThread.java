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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EasyServerTcpThread extends Thread implements SelectorLooper.SelectorMainLoopCallback, EasyServerTcpClientImpl.EasyServerTcpClientImplListener {
    private static final String TAG = "EasyServerTcpThread";
    private static final boolean DEBUG = EasyInternalSettings.DEBUG ? true : EasyInternalSettings.DEBUG_DEFAULT_FALSE;
    private static final boolean DEBUG_SELECTOR = EasyInternalSettings.DEBUG ? false : EasyInternalSettings.DEBUG_DEFAULT_FALSE;
    private static final String THREAD_NAME = "EasyServer TCP Thread";

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 1;
    private static final int QUEUE_SIZE = 10;

    @Nonnull
    private final EasyServerTcpImpl mServer;
    @Nonnull
    private final ServerSocketChannel mChannel;
    @Nonnull
    private final EasyServerTcpHandler mHandler;
    @Nonnull
    private final Map<SocketChannel, EasyServerTcpClient> mClients;
    @Nonnull
    private final ExecutorService mExecutor;
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
        final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(QUEUE_SIZE);
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, workQueue, new MyThreadFactory());
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
        mExecutor.shutdown();
        EasyNetworkUtils.close(mChannel);
        if (DEBUG) {
            LogHelper.d(TAG, "Stopped " + getName());
        }
    }

    @Override
    public void connectable(SelectorLooper selectorMainLoop, SelectionKey key) {
        throw new RuntimeException("Not used");
    }

    @Override
    public void acceptable(SelectorLooper selectorMainLoop, SelectionKey key) {
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
    public void readable(SelectorLooper selectorMainLoop, SelectionKey key) {
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
    public void writable(SelectorLooper selectorMainLoop, SelectionKey key) {
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
        mExecutor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        mExecutor.shutdownNow();
    }

    @Override
    public void onMessageToSend(final @Nonnull EasyServerTcpClientImpl easyServerTcpClient) {
        mSelectorLooper.interestedInWrite(easyServerTcpClient.getChannel(), easyServerTcpClient);
    }

    static class MyThreadFactory implements ThreadFactory {
        @Nonnull
        private final ThreadGroup group;
        // FIXME could reach Integer.MAX_VALUE (not likely but we should prevent it)
        @Nonnull
        private final AtomicInteger threadNumber;
        @Nonnull
        private final String namePrefix;

        MyThreadFactory() {
            threadNumber = new AtomicInteger(1);
            final SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = THREAD_NAME + "-pool-thread#";
        }

        public Thread newThread(@Nonnull Runnable r) {
            final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
