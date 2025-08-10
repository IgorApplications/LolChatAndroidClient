package com.iapp.lolchat.models;

public class ClientResult {

    private final ServerStatus typeResult;
    private final String message;

    public ClientResult(ServerStatus serverStatus, String message) {
        this.typeResult = serverStatus;
        this.message = message;
    }

    public ServerStatus getTypeResult() {
        return typeResult;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ClientResult{" +
                "typeResult=" + typeResult +
                ", message='" + message + '\'' +
                '}';
    }
}
