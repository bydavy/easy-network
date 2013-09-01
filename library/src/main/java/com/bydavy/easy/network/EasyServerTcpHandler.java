package com.bydavy.easy.network;

import javax.annotation.Nonnull;

public interface EasyServerTcpHandler extends EasyServerHandler<EasyServerTcp> {
    void onClientConnected(@Nonnull EasyServerTcp easyServer, @Nonnull EasyClientTcp easyClient);

    void onClientDisconnected(@Nonnull EasyServerTcp easyServer, @Nonnull EasyClientTcp easyClient);
}
