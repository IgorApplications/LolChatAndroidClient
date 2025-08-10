package com.iapp.lolchat.controllers.activities;

import android.content.Intent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.iapp.lolchat.R;
import com.iapp.lolchat.controllers.services.ClientController;
import com.iapp.lolchat.controllers.services.GeneralController;
import com.iapp.lolchat.controllers.services.LocalDataController;
import com.iapp.lolchat.models.*;

import java.util.Optional;

public class SearchActivity extends AppCompatActivity {

    private ClientController clientController;
    private LocalDataController localDataController;
    private Account personalAccount;

    private ElementAdapter<String> listOfFoundAccountsAdapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // TODO controllers maybe null?
        clientController = GeneralController.getInstance().getClientController();
        localDataController = GeneralController.getInstance().getLocalDataController();

        Optional<Account> optionalAccount = GeneralController.getInstance().getLocalDataController().getOpPersonalAccount();
        if (!optionalAccount.isPresent()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        personalAccount = optionalAccount.get();

        initUI();
    }

    private void initUI() {
        searchEditText = findViewById(R.id.search_edit_text);
        ListView listOfFoundAccountsView = findViewById(R.id.list_of_found_accounts);

        OnChangeElement<String> onChange = (view, login) -> {
            TextView loginIdView = view.findViewById(R.id.login_id);
            loginIdView.setText(login);
            ImageButton startChatButton = view.findViewById(R.id.start_chat_id);
            startChatButton.setOnClickListener(v -> {
                localDataController.addLoginToPersonalChatHistory(login);
                Intent intent = new Intent(SearchActivity.this, PersonalChatActivity.class);
                intent.putExtra("secondLogin", login);
                startActivity(intent);
            });
        };

        listOfFoundAccountsAdapter = new ElementAdapter<>(this,
                R.layout.list_item_search, onChange);
        listOfFoundAccountsView.setAdapter(listOfFoundAccountsAdapter);
    }

    public void goToGeneralMenu(View view) {
        Intent intent = new Intent(this, GeneralMenuActivity.class);
        startActivity(intent);
    }

    public void search(View view) {
        String secondLogin = searchEditText.getText().toString();

        // TODO
        ClientConsumer<Optional<Account>> consumer = (result, op) -> {
            if (result.getTypeResult() == ServerStatus.WRONG_PASSWORD) {
                updateFoundAccountsListView(secondLogin);
            } else if (result.getTypeResult() == ServerStatus.WRONG_LOGIN) {
                Toast.makeText(this, "Ничего не найдено" , Toast.LENGTH_SHORT).show();
            } else {
                showMessageDialog("Ошибка", result.toString());
            }
        };
        // TODO very dangerous!
        // TODO getActivePersonalChat every time you have to use search
        // TODO sending to yourself
        clientController.login(secondLogin, "XXXyyy@@@000", consumer);

    }

    private void updateFoundAccountsListView(String secondLogin) {
        listOfFoundAccountsAdapter.clear();
        listOfFoundAccountsAdapter.add(secondLogin);
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog confirmDialog =
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(getString(R.string.accept), (dialog, which) -> {})
                        .create();
        confirmDialog.show();
    }
}