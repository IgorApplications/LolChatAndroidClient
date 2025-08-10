package com.iapp.lolchat.controllers.services;

import android.app.Activity;
import android.content.Context;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneralController {

    private static final GeneralController INSTANCE = new GeneralController();

    private boolean initialised = false;
    private ClientController clientController;
    private LocalDataController localDataController;
    private ExecutorService threadPool;
    private Gson gson;

    public static GeneralController getInstance() {
        return INSTANCE;
    }

    // TODO parallel
    public void loadResources(Activity anyActivity) {
        if (initialised) return;

        gson = new Gson();
        threadPool = Executors.newFixedThreadPool(2);

        // !!!
        clientController = new ClientController(anyActivity);
        localDataController = new LocalDataController(anyActivity);
        initialised = true;
    }

    public Gson getGson() {
        return gson;
    }

    public void execute(Runnable task) {
        threadPool.execute(task);
    }

    public ClientController getClientController() throws IllegalStateException {
        if (clientController == null) {
            throw new IllegalStateException("It looks like the main controller was not initialized: clientController == null");
        }

        return clientController;
    }

    public LocalDataController getLocalDataController() throws IllegalStateException {
        if (localDataController == null) {
            throw new IllegalStateException("It looks like the main controller was not initialized: localDataController == null");
        }

        return localDataController;
    }

    private GeneralController() {}
}
