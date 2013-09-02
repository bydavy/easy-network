package com.bydavy.easy.network;

import javax.annotation.Nonnull;

interface EasyServerHandler<T extends EasyServer> {
    void onServerStart(@Nonnull T easyServer);

    void onServerStop(@Nonnull T easyServer);

    void onServerError(@Nonnull T easyServer);
}
