package com.bydavy.easy.network.examples;

import com.bydavy.easy.network.*;
import com.bydavy.easy.network.utils.LogHelper;
import com.bydavy.easy.network.protocols.PrefixedBySizeAsyncReader;
import com.bydavy.easy.network.protocols.PrefixedBySizeAsyncWriter;

import javax.annotation.Nonnull;
import java.net.InetSocketAddress;
import java.util.List;

public class Example1 {
    private static final String TAG = "Example1";

    private static final String IP = "localhost";
    private static final int PORT = 9595;

    public static void main(String args[]) {
        EasyServerTcp easyServer = EasyServerTcpFactory.create(new InetSocketAddress(IP, PORT));
        easyServer.setHandler(new MyEasyServerHandler());
        easyServer.start();

        EasyClientTcp easyClient = EasyClientTcpFactory.create(new InetSocketAddress(IP, PORT));
        easyClient.setHandler(new MyEasyClientHandler());
        easyClient.setMessageReader(new PrefixedBySizeAsyncReader(new ExampleMessageReader()));
        easyClient.setMessageWriter(new PrefixedBySizeAsyncWriter(new ExampleMessageWriter()));
        easyClient.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Nothing
        }

        List<EasyClientTcp> easyClients = easyServer.getEasyClients();
        System.out.println("Client count: " + easyClients.size());

        easyServer.sendToAll(new TextMessage("Hello to all clients!"));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Nothing
        }

        easyClient.stop();
        try {
            easyClient.join();
        } catch (InterruptedException e) {
            // Nothing
        }
        //easyClient.interrupt(); // Abrupt stop or Forced exit

        easyServer.stop();
        try {
            easyServer.join();
        } catch (InterruptedException e) {
            // Nothing
        }
        //easyServer.interrupt();
    }

    // Client code
    public static class MyEasyClientHandler implements EasyClientTcpHandler {

        @Override
        public void onMessageReceived(@Nonnull EasyClientTcp easyClient, @Nonnull EasyMessage message) {
            LogHelper.d(TAG, "Client received " + message);
            if (message instanceof PingMessage) {
                easyClient.send(new PongMessage());
            }
        }

        @Override
        public void onClientError(@Nonnull EasyClientTcp easyClient) {
        }

        @Override
        public void onClientStart(@Nonnull EasyClientTcp easyClient) {
            easyClient.send(new HelloMessage());
        }

        @Override
        public void onClientStop(@Nonnull EasyClientTcp easyClient) {
        }

        @Override
        public void onEndOfInputStream(@Nonnull EasyClientTcp easyClient) {
        }

        @Override
        public void onEndOfOutputStream(@Nonnull EasyClientTcp easyClient) {
        }
    }

    // Server code
    public static class MyEasyServerHandler implements EasyServerTcpHandler {
        @Override
        public void onServerStart(@Nonnull EasyServerTcp easyServer) {
        }

        @Override
        public void onServerStop(@Nonnull EasyServerTcp easyServer) {
        }

        @Override
        public void onServerError(@Nonnull EasyServerTcp easyServer) {
        }

        @Override
        public void onClientConnected(@Nonnull EasyServerTcp easyServer, @Nonnull EasyClientTcp easyClient) {
            easyClient.setHandler(new MyEasyServerClientHandler());
            easyClient.setMessageReader(new PrefixedBySizeAsyncReader(new ExampleMessageReader()));
            easyClient.setMessageWriter(new PrefixedBySizeAsyncWriter(new ExampleMessageWriter()));
            easyClient.start();
            //easyClient.send(new PingMessage());
        }

        @Override
        public void onClientDisconnected(@Nonnull EasyServerTcp easyServer, @Nonnull EasyClientTcp easyClient) {
        }
    }

    // Server code per client
    public static class MyEasyServerClientHandler implements EasyClientTcpHandler {

        @Override
        public void onMessageReceived(@Nonnull EasyClientTcp easyClient, @Nonnull EasyMessage message) {
            LogHelper.d(TAG, "Server received " + message);
            if (message instanceof HelloMessage) {
                easyClient.send(new PingMessage());
            }
        }

        @Override
        public void onClientError(@Nonnull EasyClientTcp easyClient) {
        }

        @Override
        public void onClientStart(@Nonnull EasyClientTcp easyClient) {
        }

        @Override
        public void onClientStop(@Nonnull EasyClientTcp easyClient) {
        }

        @Override
        public void onEndOfInputStream(@Nonnull EasyClientTcp easyClient) {
        }

        @Override
        public void onEndOfOutputStream(@Nonnull EasyClientTcp easyClient) {
        }
    }
}

