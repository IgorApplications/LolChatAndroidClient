package com.iapp.lolchat.models;

public interface ClientBiConsumer<T1, T2> {

    void onClient(ClientResult result, T1 t1, T2 t2);
}
