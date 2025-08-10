package com.iapp.lolchat.models;

public interface ClientConsumer<T> {

    void onClient(ClientResult result, T t);
}
