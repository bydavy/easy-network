package com.bydavy.easy.network;


import javax.annotation.Nonnull;

public interface EasyServerClient {
    void send(@Nonnull EasyMessage msg);
}
