package com.bydavy.easy.network;

import javax.annotation.Nonnull;
import java.net.SocketAddress;

public class EasyServerTcpFactory {
    private EasyServerTcpFactory() {
        throw new IllegalAccessError();
    }

    public static EasyServerTcp create(@Nonnull SocketAddress address) {
        return new EasyServerTcpImpl(address);
    }
}
