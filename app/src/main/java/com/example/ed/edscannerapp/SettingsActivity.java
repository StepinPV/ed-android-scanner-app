package com.example.ed.edscannerapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.ed.edscannerapp.entities.User;

/**
 * Класс экрана настроек
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        init();
    }

    private void init(){
        this.updateAccount();
        this.initServerSetting();
    }

    private void updateAccount() {
        AccountManager.getInstance().getUser(new AccountManager.UserCallback() {
            @Override
            public void success(User user) {
                if(user != null) {
                    TextView userNameView = (TextView) findViewById(R.id.check_user_name);
                    userNameView.setText(user.getFullName());
                }
            }
        });
    }

    private void initServerSetting() {
        String server = Settings.getServer();

        final LinearLayout customServerBlock = ((LinearLayout) findViewById(R.id.custom_server));

        if (server.equals(Settings.FIRST_SERVER)) {
            ((RadioButton)findViewById(R.id.first_server_radio)).setChecked(true);
        } else if (server.equals(Settings.SECOND_SERVER)) {
            ((RadioButton)findViewById(R.id.second_server_radio)).setChecked(true);
        } else {
            ((RadioButton)findViewById(R.id.custom_server_radio)).setChecked(true);

            ((EditText) findViewById(R.id.custom_server_field)).setText(server);
            customServerBlock.setVisibility(View.VISIBLE);
        }

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.server_radio_group);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.first_server_radio:
                    case R.id.second_server_radio:
                        customServerBlock.setVisibility(View.GONE);
                        break;
                    case R.id.custom_server_radio:
                        customServerBlock.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    public void saveAndExit(View w) {
        saveServerSettings();

        finish();
    }

    private void saveServerSettings() {
        RadioGroup serverRadioGroup = (RadioGroup) findViewById(R.id.server_radio_group);

        switch (serverRadioGroup.getCheckedRadioButtonId()) {
            case R.id.first_server_radio:
                Settings.setServer(Settings.FIRST_SERVER);
                break;
            case R.id.second_server_radio:
                Settings.setServer(Settings.SECOND_SERVER);
                break;
            case R.id.custom_server_radio:
                String server = ((EditText) findViewById(R.id.custom_server_field)).getText().toString();
                Settings.setServer(server);
                break;

            default:
                break;
        }
    }


    public void exit(View w) {
        finish();
    }
}