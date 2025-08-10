package com.iapp.lolchat.controllers.activities;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.iapp.lolchat.R;
import com.iapp.lolchat.controllers.services.ClientController;
import com.iapp.lolchat.controllers.services.GeneralController;
import com.iapp.lolchat.controllers.services.LocalDataController;
import com.iapp.lolchat.models.Account;
import com.iapp.lolchat.models.ElementAdapter;
import com.iapp.lolchat.models.OnChangeElement;

import java.util.Set;

public class GeneralMenuActivity extends AppCompatActivity {

    private ClientController clientController;
    private LocalDataController localDataController;
    private Account personalAccount;

    private ElementAdapter<String> listOfPersonalChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO обновление в режиме реального времени
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_menu);

        // TODO in real called single time
        GeneralController.getInstance().loadResources(this);
        if (!GeneralController.getInstance().getLocalDataController().getOpPersonalAccount().isPresent()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        clientController = GeneralController.getInstance().getClientController();
        localDataController = GeneralController.getInstance().getLocalDataController();
        personalAccount = localDataController.getOpPersonalAccount().get();

        initUI();
    }

    private void initUI() {
        ListView listOfPersonalChatView = findViewById(R.id.list_of_personal_chat_history);

        OnChangeElement<String> onChange = (view, login) -> {
            TextView loginIdView = view.findViewById(R.id.login_id);
            loginIdView.setText(login);
            ImageButton startChatButton = view.findViewById(R.id.start_chat_id);
            startChatButton.setOnClickListener(v -> {
                Intent intent = new Intent(GeneralMenuActivity.this, PersonalChatActivity.class);
                intent.putExtra("secondLogin", login);
                startActivity(intent);
            });
        };

        listOfPersonalChatAdapter = new ElementAdapter<>(this,
                R.layout.list_item_search, onChange);
        listOfPersonalChatView.setAdapter(listOfPersonalChatAdapter);

        updatePersonalChatHistoryListView(localDataController.getLoginsPersonalChatHistory());
    }

    public void goToSearchActivity(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    private void updatePersonalChatHistoryListView(Set<String> logins) {
        listOfPersonalChatAdapter.clear();
        for (String login : logins) {
            listOfPersonalChatAdapter.add(login);
        }
    }
}