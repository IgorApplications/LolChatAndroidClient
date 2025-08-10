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
import com.iapp.lolchat.models.*;

import java.util.Optional;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private ClientController clientController;
    private LocalDataController localDataController;

    private EditText inputLogin;
    private EditText inputEmail;
    private EditText inputPassword;
    private EditText inputRepeatPassword;

    private Pattern emailPattern;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        inputLogin = findViewById(R.id.registrationLoginInput);
        inputEmail = findViewById(R.id.registrationEmailInput);
        inputPassword = findViewById(R.id.registrationPasswordInput);
        inputRepeatPassword = findViewById(R.id.registrationRepeatPasswordInput);

        emailPattern = Pattern.compile(".+@.+\\..+");
        clientController = GeneralController.getInstance().getClientController();
        localDataController = GeneralController.getInstance().getLocalDataController();
    }

    public void confirmRegistration(View view) {
        if (!inputPassword.getText().toString().equals(inputRepeatPassword.getText().toString())) {
            showMessageDialog(getString(R.string.incorrect_passwords), getString(R.string.entered_password_not_occur));
            return;
        }

        if (!emailPattern.matcher(inputEmail.getText().toString()).matches()) {
            showMessageDialog(getString(R.string.incorrect_mail), getString(R.string.entered_email_syntax));
            return;
        }

        if (inputLogin.getText().toString().length() > 50) {
            showMessageDialog(getString(R.string.incorrect_mail), getString(R.string.email_longer_50));
            return;
        }

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
        ClientConsumer<Optional<Account>> callback = (result, account) -> {
            if (result.getTypeResult() == ServerStatus.SUCCESS) {
                if (!account.isPresent()) {
                    showMessageDialog(getString(R.string.error), "account == null");
                    return;
                }

                localDataController.savePersonalAccount(account.get());
                startActivity(intent);
            } else {
                if (result.getTypeResult() == ServerStatus.LOGIN_ALREADY_TAKEN) {
                    showMessageDialog(getString(R.string.incorrect_login), getString(R.string.login_exists));
                } else if (result.getTypeResult() == ServerStatus.WRONG_LOGIN) {
                    showMessageDialog(getString(R.string.incorrect_login), getString(R.string.login_does_not_exist));
                } else if (result.getTypeResult() == ServerStatus.WRONG_PASSWORD) {
                    showMessageDialog(getString(R.string.incorrect_login), getString(R.string.password_does_not_fit));
                }  else {
                    showMessageDialog(getString(R.string.error_equal) + result.getTypeResult().toString(), result.getMessage());
                }
            }
        };

        clientController.register(inputLogin.getText().toString(),
                inputEmail.getText().toString(), inputPassword.getText().toString(), callback);
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