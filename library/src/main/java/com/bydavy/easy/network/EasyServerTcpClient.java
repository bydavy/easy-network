package com.bydavy.easy.network;


import java.io.IOException;

public interface EasyServerTcpClient extends EasyClientTcp {
    boolean read() throws IOException;

    boolean write() throws IOException;
}
