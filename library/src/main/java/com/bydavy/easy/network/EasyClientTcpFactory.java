package com.bydavy.easy.network;


import javax.annotation.Nonnull;
import java.net.SocketAddress;

public class EasyClientTcpFactory {

    private EasyClientTcpFactory() {
        throw new IllegalAccessError();
    }

    public static EasyClientTcp create(@Nonnull SocketAddress address) {
        return new EasyClientTcpImpl(address);
    }
}
