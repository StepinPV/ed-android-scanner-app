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

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        init();
    }

    private void init(){
        this.updateAccount();
        this.initServerIPSetting();
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

    private void initServerIPSetting() {
        String serverIPType = Settings.getServerIPType();

        if (serverIPType.equals("1")) {
            ((RadioButton)findViewById(R.id.server_ip_type_1)).setChecked(true);
        } else if (serverIPType.equals("2")) {
            ((RadioButton)findViewById(R.id.server_ip_type_2)).setChecked(true);
        } else {
            ((RadioButton)findViewById(R.id.server_ip_type_3)).setChecked(true);
            ((LinearLayout) findViewById(R.id.server_ip_additional)).setVisibility(View.VISIBLE);

            // Вставляем в поле IP адрес
            String serverIP = Settings.getServerIP();
            ((EditText) findViewById(R.id.server_ip_ip_value)).setText(serverIP);

            // Вставляем в поле port
            String serverPort = Settings.getServerPort();
            ((EditText) findViewById(R.id.server_ip_port_value)).setText(serverPort);
        }

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.server_ip_radio_group);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.server_ip_type_1:
                        ((LinearLayout) findViewById(R.id.server_ip_additional)).setVisibility(View.GONE);
                        break;
                    case R.id.server_ip_type_2:
                        ((LinearLayout) findViewById(R.id.server_ip_additional)).setVisibility(View.GONE);
                        break;
                    case R.id.server_ip_type_3:
                        ((LinearLayout) findViewById(R.id.server_ip_additional)).setVisibility(View.VISIBLE);
                        break;

                    default:
                        break;
                }
            }
        });
    }

    public void saveAndExit(View w) {
        this.saveServerIP();
        finish();
    }

    private void saveServerIP() {
        RadioGroup ipradioGroup = (RadioGroup) findViewById(R.id.server_ip_radio_group);

        switch (ipradioGroup.getCheckedRadioButtonId()) {
            case R.id.server_ip_type_1:
                Settings.setServerIP("1", null, null);
                break;
            case R.id.server_ip_type_2:
                Settings.setServerIP("2", null, null);
                break;
            case R.id.server_ip_type_3:
                String serverIP = ((EditText) findViewById(R.id.server_ip_ip_value)).getText().toString();
                String serverPort = ((EditText) findViewById(R.id.server_ip_port_value)).getText().toString();
                Settings.setServerIP("3", serverIP, serverPort);
                break;

            default:
                break;
        }
    }


    public void exit(View w) {
        finish();
    }
}