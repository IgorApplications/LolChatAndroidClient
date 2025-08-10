package com.iapp.lolchat.controllers.activities;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.github.library.bubbleview.BubbleTextView;
import com.iapp.lolchat.R;
import com.iapp.lolchat.controllers.services.ClientController;
import com.iapp.lolchat.controllers.services.GeneralController;
import com.iapp.lolchat.controllers.services.LocalDataController;
import com.iapp.lolchat.models.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PersonalChatActivity extends AppCompatActivity {

    private ClientController clientController;
    private LocalDataController localDataController;

    // dangerous
    private Account personalAccount;
    private String secondLogin;

    private boolean single = false;
    private EditText sendMessageEditText;
    private ElementAdapter<Message> listOfFoundAccountsAdapter;
    private final List<Message> currentMessages = new ArrayList<>();

    private AtomicBoolean stop = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_chat);

        clientController = GeneralController.getInstance().getClientController();
        localDataController = GeneralController.getInstance().getLocalDataController();
        // TODO dangerous
        personalAccount = localDataController.getOpPersonalAccount().get();

        // TODO very dangerous
        Intent intent = getIntent();
        secondLogin = (String) intent.getSerializableExtra("secondLogin");

        initUI();
        Runnable task = () -> {
            while (!stop.get()) {
                ClientConsumer<List<Message>> consumer = (result, messages) -> {
                    // ui thread
                    if (result.getTypeResult() == ServerStatus.SUCCESS) {
                        if (!messages.equals(currentMessages)) {
                            currentMessages.clear();
                            currentMessages.addAll(messages);
                            updateMessages(currentMessages);
                        }
                    } else {
                        // TODO
                        showMessageDialog("Ошибка", result.toString());
                        single = true;
                    }
                };
                clientController.getMessages(personalAccount, secondLogin, consumer);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted " + e);
                }
            }
        };
        GeneralController.getInstance().execute(task);
    }

    private void initUI() {
        TextView loginTextView = findViewById(R.id.personal_chat_login_text);
        loginTextView.setText(secondLogin);
        sendMessageEditText = findViewById(R.id.personal_chat_edit_text);

        ListView listView = findViewById(R.id.list_of_messages);
        OnChangeElement<Message> onChange = (view, message) -> {
            // SEE VISIBLE AND INVISIBLE !!!!!!!!!
            BubbleTextView personMessageText = view.findViewById(R.id.person_message_text);
            BubbleTextView userMessageText = view.findViewById(R.id.user_message_text);
            TextView personMessageTime = view.findViewById(R.id.person_message_time);
            TextView userMessageTime = view.findViewById(R.id.user_message_time);
            if (message.getSender().equals(personalAccount.getLogin())) {
                personMessageText.setVisibility(View.VISIBLE);
                personMessageTime.setVisibility(View.VISIBLE);
                userMessageText.setVisibility(View.INVISIBLE);
                userMessageTime.setVisibility(View.INVISIBLE);

                personMessageText.setText(message.getContent());
                personMessageTime.setText(TimeUtil.defineTimeView(this, message.getTime()));
            } else {
                personMessageText.setVisibility(View.INVISIBLE);
                personMessageTime.setVisibility(View.INVISIBLE);
                userMessageText.setVisibility(View.VISIBLE);
                userMessageTime.setVisibility(View.VISIBLE);

                userMessageText.setText(message.getContent());
                userMessageTime.setText(TimeUtil.defineTimeView(this, message.getTime()));
            }
        };

        listOfFoundAccountsAdapter = new ElementAdapter<>(
                this, R.layout.list_item_chat, onChange);
        listView.setAdapter(listOfFoundAccountsAdapter);
    }

    public void sendMessage(View view) {
        ClientCallback callback = result -> {
            if (result.getTypeResult() != ServerStatus.SUCCESS) {
                // TODO
                showMessageDialog("Ошибка", result.toString());
            } else {
                Message message = new Message(new Date().getTime(),
                        personalAccount.getLogin(), sendMessageEditText.getText().toString());
                addNewMessage(message);
            }
        };
        clientController.sendMessage(personalAccount, secondLogin, sendMessageEditText.getText().toString(), callback);
    }

    public void goToGeneralMenu(View view) {
        stop.set(true);
        Intent intent = new Intent(this, GeneralMenuActivity.class);
        startActivity(intent);
    }

    private void showMessageDialog(String title, String message) {
        // TODO
        if (single) return;

        AlertDialog confirmDialog =
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(getString(R.string.accept), (dialog, which) -> {
                            single = false;
                        })
                        .setOnCancelListener(dialog -> single = false)
                        .create();
        confirmDialog.show();
    }

    private void addNewMessage(Message message) {
        listOfFoundAccountsAdapter.add(message);
    }

    private void updateMessages(List<Message> newMessages) {
        listOfFoundAccountsAdapter.clear();
        for (Message message : newMessages) {
            listOfFoundAccountsAdapter.add(message);
        }
    }
}