package com.bydavy.easy.network;

import javax.annotation.Nonnull;

public interface EasyClientHandler<T extends EasyClient> {

    void onMessageReceived(@Nonnull T easyClient, @Nonnull EasyMessage message);

    void onClientError(@Nonnull T easyClient);

    void onClientStart(@Nonnull T easyClient);

    void onClientStop(@Nonnull T easyClient);

    void onEndOfInputStream(@Nonnull T easyClient);

    void onEndOfOutputStream(@Nonnull T easyClient);
}
