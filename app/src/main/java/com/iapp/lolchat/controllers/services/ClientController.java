package com.iapp.lolchat.controllers.services;

import android.app.Activity;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.iapp.lolchat.models.*;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

public class ClientController {

    private static final String SERVER_URL = "http://lolchat.space/server";

    private final Activity activity;
    private final MediaType jsonType;
    private final OkHttpClient client;
    private final Gson gson;

    ClientController(Activity activity) {
        this.activity = activity;
        jsonType = MediaType.get("application/json");
        client = new OkHttpClient();
        gson = GeneralController.getInstance().getGson();
    }

    public void sendMessage(Account personalAccount, String secondLogin, String textMessage, ClientCallback callback) {

        Runnable task = () -> {

            String json = String.format(
                    "{\"sender_login\":\"%s\",\"sender_password\":\"%s\",\"recipient_login\":\"%s\",\"content\":\"%s\"}",
                    personalAccount.getLogin(), personalAccount.getPassword(), secondLogin, textMessage);

            RequestBody body = RequestBody.create(json, jsonType);
            Request request = new Request.Builder()
                    .url(SERVER_URL + "/createMessage")
                    .post(body)
                    .build();
            log("/createMessage: " + request);

            try (Response response = client.newCall(request).execute()) {

                log("/createMessage: " + response.headers());

                if (!response.isSuccessful()) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/createMessage:!response.isSuccessful())");
                    logError("/createMessage: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result));
                    return;
                }

                if (response.body() == null) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/createMessage:response.body() == null)");
                    logError("/createMessage: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result));
                    return;
                }

                Map<String, JsonElement> map = parseToMap(response.body().string());
                String message = "!map.containsKey(\"message\")";
                if (map.containsKey("message")) {
                    message = map.get("message").getAsString();
                }

                if (!map.containsKey("status")) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/createMessage:!map.containsKey(\"status\"))");
                    logError("/createMessage: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result));
                    return;
                }
                String status = map.get("status").getAsString();

                if (status.equals("SUCCESS")) {
                    ClientResult result = new ClientResult(ServerStatus.SUCCESS, response + "(/createMessage:message = " + message + ")");
                    log("/createMessage: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result));
                } else {
                    ClientResult result = new ClientResult(getServerStatus(status), response + "(/createMessage:message = "+ message + ")");
                    logError("/createMessage: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result));
                }

            } catch (IOException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/createMessage:IOException)");
                logError("/createMessage: " + result);
                activity.runOnUiThread(() -> callback.onClient(result));
            } catch (JsonSyntaxException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/createMessage:JsonSyntaxException)");
                logError("/createMessage: " + result);
                activity.runOnUiThread(() -> callback.onClient(result));
            } catch (IndexOutOfBoundsException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/createMessage:IndexOutOfBoundsException)");
                logError("/createMessage: " + result);
                activity.runOnUiThread(() -> callback.onClient(result));
            } catch (Throwable t) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/createMessage:Throwable)");
                logError("/createMessage: " + result);
                activity.runOnUiThread(() -> callback.onClient(result));
            }
        };
        GeneralController.getInstance().execute(task);

    }

    public void getMessages(Account personalAccount, String secondLogin, ClientConsumer<List<Message>> callback) {

        Runnable task = () -> {

            String json = String.format(
                    "{\"sender_login\":\"%s\",\"sender_password\":\"%s\",\"recipient_login\":\"%s\"}",
                    personalAccount.getLogin(), personalAccount.getPassword(), secondLogin);

            RequestBody body = RequestBody.create(json, jsonType);
            Request request = new Request.Builder()
                    .url(SERVER_URL + "/getMessages")
                    .post(body)
                    .build();
            log("/getMessages: " + request);

            try (Response response = client.newCall(request).execute()) {

                log("/getMessages: " + response.headers());

                if (!response.isSuccessful()) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + " (/getMessages:!response.isSuccessful())");
                    logError("/getMessages: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, new ArrayList<>()));
                    return;
                }

                if (response.body() == null) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + " (/getMessages:response.body() == null)");
                    logError("/getMessages: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, new ArrayList<>()));
                    return;
                }

                String serverData = response.body().string();
                Map<String, JsonElement> map = parseToMap(serverData);

                String message = "!map.containsKey(\"message\")";
                if (map.containsKey("message")) {
                    message = map.get("message").getAsString();
                }

                if (!map.containsKey("data")) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + " (/getMessages:!map.containsKey(\"data\"))");
                    logError("/getMessages: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, new ArrayList<>()));
                    return;
                }

                List<Message> messages = new ArrayList<>();
                for (JsonElement element : gson.fromJson(map.get("data"), JsonArray.class)) {
                    Map<String, JsonElement> jsonMessagesMap = parseToMap(element.toString());

                    long time = -1;
                    String senderLogin = "";
                    String content = "";

                    if (jsonMessagesMap.containsKey("time")) {
                        time = jsonMessagesMap.get("time").getAsLong();
                    } else {
                        ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/getMessages:!jsonMessagesMap.containsKey(\"time\")");
                        logError("/getMessages: " + result);
                        activity.runOnUiThread(() -> callback.onClient(result, messages));
                        return;
                    }

                    if (jsonMessagesMap.containsKey("sender_login")) {
                        senderLogin = jsonMessagesMap.get("sender_login").getAsString();
                    } else {
                        ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/getMessages:!jsonMessagesMap.containsKey(\"sender_login\")");
                        logError("/getMessages: " + result);
                        activity.runOnUiThread(() -> callback.onClient(result, messages));
                        return;
                    }

                    if (jsonMessagesMap.containsKey("content")) {
                        content = jsonMessagesMap.get("content").getAsString();
                    } else {
                        ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/getMessages:!jsonMessagesMap.containsKey(\"content\")");
                        logError("/getMessages: " + result);
                        activity.runOnUiThread(() -> callback.onClient(result, messages));
                        return;
                    }

                    Message newMessage = new Message(time, senderLogin, content);
                    messages.add(newMessage);
                }

                if (!map.containsKey("status")) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/getMessages:!map.containsKey(\"status\"))");
                    logError("/getMessages: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, new ArrayList<>()));
                    return;
                }
                String status = map.get("status").getAsString();

                if (status.equals("SUCCESS")) {
                    ClientResult result = new ClientResult(ServerStatus.SUCCESS, response.toString());
                    log("/getMessages: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, messages));
                } else {
                    ClientResult result = new ClientResult(getServerStatus(status), response + " (/getMessages:message = " + message + ")");
                    logError("/getMessages: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, messages));
                }


            } catch (IOException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/getMessages:IOException)");
                logError("/getMessages: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, new ArrayList<>()));
            } catch (JsonSyntaxException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/getMessages:JsonSyntaxException)");
                logError("/getMessages: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, new ArrayList<>()));
            } catch (IndexOutOfBoundsException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/getMessages:IndexOutOfBoundsException)");
                logError("/getMessages: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, new ArrayList<>()));
            } catch (Throwable t) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/getMessages:Throwable)");
                logError("/getMessages: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, new ArrayList<>()));
            }
        };
        GeneralController.getInstance().execute(task);

    }

    public void register(String login, String email, String password, ClientConsumer<Optional<Account>> callback) {

        Runnable task = () -> {

            String json = String.format(
                    "{\"login\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}",
                    login, email, password);

            RequestBody body = RequestBody.create(json, jsonType);
            Request request = new Request.Builder()
                    .url(SERVER_URL + "/register")
                    .post(body)
                    .build();
            log("/register: " + request);

            try (Response response = client.newCall(request).execute()) {

                log("/register:headers: " + response.headers());

                if (!response.isSuccessful()) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/register:!response.isSuccessful())");
                    logError("/register: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
                    return;
                }

                if (response.body() == null) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/register:response.body() == null)");
                    logError("/register: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
                    return;
                }

                Map<String, JsonElement> map = parseToMap(response.body().string());
                String message = "";
                if (map.containsKey("message")) {
                    message = map.get("message").getAsString();
                }

                if (!map.containsKey("status")) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/register:!map.containsKey(\"status\"))");
                    logError("/register: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
                    return;
                }
                String status = map.get("status").getAsString();

                if (status.equals("SUCCESS")) {
                    Account personalAccount = new Account("", login, password);
                    ClientResult result = new ClientResult(ServerStatus.SUCCESS, response + "(/register:message = " + message + ")");
                    log("/register: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.of(personalAccount)));
                } else {
                    ClientResult result = new ClientResult(getServerStatus(status), response + "(/register:message = " + message + ")");
                    logError("/register: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
                }

            } catch (IOException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/register:IOException)");
                logError("/register: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
            } catch (JsonSyntaxException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/register:JsonSyntaxException)");
                logError("/register: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
            } catch (IndexOutOfBoundsException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/register:IndexOutOfBoundsException)");
                logError("/register: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
            } catch (Throwable t) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/register:Throwable)");
                logError("/register: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
            }
        };
        GeneralController.getInstance().execute(task);
    }

    public void login(String login, String password, ClientConsumer<Optional<Account>> callback) {
        Runnable task = () -> {

            String json = String.format(
                    "{\"login\":\"%s\",\"password\":\"%s\"}",
                    login, password);

            RequestBody body = RequestBody.create(json, jsonType);
            Request request = new Request.Builder()
                    .url(SERVER_URL + "/login")
                    .post(body)
                    .build();
            log("/login: " + request);

            try (Response response = client.newCall(request).execute()) {

                log("/login: " + response.headers());

                if (!response.isSuccessful()) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/login:!response.isSuccessful())");
                    logError("/login: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
                    return;
                }

                if (response.body() == null) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/login:response.body() == null)");
                    logError("/login: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
                    return;
                }

                Map<String, JsonElement> map = parseToMap(response.body().string());
                String message = "!map.containsKey(\"message\")";
                if (map.containsKey("message")) {
                    message = map.get("message").getAsString();
                }

                if (!map.containsKey("status")) {
                    ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, response + "(/login:!map.containsKey(\"status\"))");
                    logError("/login: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
                    return;
                }
                String status = map.get("status").getAsString();

                if (status.equals("SUCCESS")) {
                    Account personalAccount = new Account("", login, password);
                    ClientResult result = new ClientResult(ServerStatus.SUCCESS, response + "(/login:message = " + message + ")");
                    log("/login: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.of(personalAccount)));
                } else {
                    ClientResult result = new ClientResult(getServerStatus(status), response + "(/login:message = " + message + ")");
                    logError("/login: " + result);
                    activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
                }

            } catch (IOException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/login:IOException)");
                logError("/login: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
            } catch (JsonSyntaxException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/login:JsonSyntaxException)");
                logError("/login: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
            } catch (IndexOutOfBoundsException e) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/login:IndexOutOfBoundsException)");
                logError("/login: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
            } catch (Throwable t) {
                ClientResult result = new ClientResult(ServerStatus.HTTP_ERROR, "(/login:Throwable)");
                logError("/login: " + result);
                activity.runOnUiThread(() -> callback.onClient(result, Optional.empty()));
            }

        };
        GeneralController.getInstance().execute(task);

    }

    private Map<String, JsonElement> parseToMap(String res) {
        return gson.fromJson(res, new TypeToken<Map<String, JsonElement>>(){}.getType());
    }

    private void logError(String s) {
        System.out.println("LOG ClientController: " + s);
    }

    private void log(String s) {
        System.out.println("LOG ClientController: " + s);
    }

    private ServerStatus getServerStatus(String s) {
        switch (s) {
            case "SUCCESS":
                return ServerStatus.SUCCESS;
            case "WRONG_LOGIN":
                return ServerStatus.WRONG_LOGIN;
            case "WRONG_PASSWORD":
                return ServerStatus.WRONG_PASSWORD;
            case "UNKNOWN_ERROR":
                return ServerStatus.UNKNOWN_ERROR;
            case "LOGIN_ALREADY_TAKEN":
                return ServerStatus.LOGIN_ALREADY_TAKEN;
        }
        return ServerStatus.UNKNOWN;
    }
}
