package com.bydavy.easy.network;

import javax.annotation.Nonnull;

public interface EasyClientTcpHandler extends EasyClientHandler<EasyClientTcp> {

    void onConnected(@Nonnull EasyClientTcp easyClient);

    void onDisconnected(@Nonnull EasyClientTcp easyClient);
}
