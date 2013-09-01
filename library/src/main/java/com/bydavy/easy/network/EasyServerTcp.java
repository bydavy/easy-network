package com.bydavy.easy.network;

import javax.annotation.Nonnull;
import java.util.List;

public interface EasyServerTcp extends EasyServer<EasyServerTcpHandler> {
    @Nonnull
    List<EasyClientTcp> getEasyClients();

    void sendToAll(@Nonnull EasyMessage message);
}
