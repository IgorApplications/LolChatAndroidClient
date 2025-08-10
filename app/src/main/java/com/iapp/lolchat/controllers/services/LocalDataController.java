package com.iapp.lolchat.controllers.services;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iapp.lolchat.models.Account;

import java.lang.reflect.Type;
import java.util.*;

public class LocalDataController {

    private static final String FILE_NAME = "lolchat_data";
    private static final String PERSONAL_ACCOUNT_KEY = "personal_account_key";
    private static final String LOGINS_PERSONAL_CHAT_HISTORY = "logins_personal_chat_history";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private Optional<Account> opPersonalAccount = Optional.empty();
    private final Set<String> loginsPersonalChatHistory = new HashSet<>();

    LocalDataController(Context anyContext) {
        gson = GeneralController.getInstance().getGson();
        sharedPreferences = anyContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        loadData();
    }

    public Set<String> getLoginsPersonalChatHistory() {
        return loginsPersonalChatHistory;
    }

    public void addLoginToPersonalChatHistory(String login) {
        loginsPersonalChatHistory.add(login);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LOGINS_PERSONAL_CHAT_HISTORY, gson.toJson(loginsPersonalChatHistory));
        editor.apply();
    }

    public Optional<Account> getOpPersonalAccount() {
        return opPersonalAccount;
    }

    public void savePersonalAccount(Account account) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PERSONAL_ACCOUNT_KEY, gson.toJson(account));
        editor.apply();
        opPersonalAccount = Optional.of(account);
    }

    private void loadData() {
        String json = sharedPreferences.getString(PERSONAL_ACCOUNT_KEY, null);
        if (json != null) {
            opPersonalAccount = Optional.of(gson.fromJson(json, Account.class));
        }
        json = sharedPreferences.getString(LOGINS_PERSONAL_CHAT_HISTORY, null);
        if (json != null) {
            Type listType = new TypeToken<HashSet<String>>(){}.getType();
            loginsPersonalChatHistory.addAll(gson.fromJson(json, listType));
        }
    }
}
