package com.iapp.lolchat.controllers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.iapp.lolchat.R;
import com.iapp.lolchat.controllers.services.ClientController;
import com.iapp.lolchat.controllers.services.GeneralController;
import com.iapp.lolchat.controllers.services.LocalDataController;
import com.iapp.lolchat.models.Account;
import com.iapp.lolchat.models.ClientConsumer;
import com.iapp.lolchat.models.ServerStatus;
import com.iapp.lolchat.models.ClientBiConsumer;

import java.util.Optional;

public class LoginActivity extends AppCompatActivity {

    private ClientController clientController;
    private LocalDataController localDataController;

    private EditText inputLogin;
    private EditText inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        inputLogin = findViewById(R.id.authLoginInput);
        inputPassword = findViewById(R.id.authPasswordInput);

        clientController = GeneralController.getInstance().getClientController();
        localDataController = GeneralController.getInstance().getLocalDataController();
    }

    public void login(View view) {
        // login [3;20] password [6;50]

        if (inputLogin.getText().toString().length() < 3) {
            showMessageDialog(getString(R.string.incorrect_login), getString(R.string.login_least_3));
            return;
        }

        if (inputLogin.getText().toString().length() > 20) {
            showMessageDialog(getString(R.string.incorrect_login), getString(R.string.login_longer_20));
            return;
        }

        if (inputPassword.getText().toString().length() < 6) {
            showMessageDialog(getString(R.string.incorrect_password), getString(R.string.password_least_6));
            return;
        }

        if (inputPassword.getText().toString().length() > 50) {
            showMessageDialog(getString(R.string.incorrect_password), getString(R.string.password_longer_50));
            return;
        }

        Intent intent = new Intent(this, GeneralMenuActivity.class);
        ClientConsumer<Optional<Account>> consumer = (clientResult, account) -> {
            if (clientResult.getTypeResult() == ServerStatus.SUCCESS) {
                if (!account.isPresent()) {
                    showMessageDialog(getString(R.string.error), "account == null");
                    return;
                }

                localDataController.savePersonalAccount(account.get());
                startActivity(intent);
            } else {
                if (clientResult.getTypeResult() == ServerStatus.WRONG_LOGIN) {
                    showMessageDialog(getString(R.string.login_error), getString(R.string.login_does_not_exist));
                } else if (clientResult.getTypeResult() == ServerStatus.WRONG_PASSWORD) {
                    showMessageDialog(getString(R.string.login_error), getString(R.string.password_does_not_fit));
                } else {
                    showMessageDialog(getString(R.string.error_equal) + clientResult.getTypeResult().toString(), clientResult.getMessage());
                }
            }
        };

        clientController.login(inputLogin.getText().toString(), inputPassword.getText().toString(), consumer);
    }

    public void goToRegistration(View view) {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
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