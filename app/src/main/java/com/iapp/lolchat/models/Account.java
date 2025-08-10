package com.iapp.lolchat.models;

public class Account {

    private final String username;
    private final String login;
    private final String password;


    public Account(String username, String login, String password) {
        this.username = username;
        this.login = login;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
