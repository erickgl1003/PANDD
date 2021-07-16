package com.example.pandd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(ParseUser.getCurrentUser() != null){
            goMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSign = findViewById(R.id.tvSign);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i(TAG,"onClick Login Button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if(verifyEmpty(username, "Username"))return;
                if(verifyEmpty(password, "Password"))return;
                loginUser(username, password);
            }
        });

        tvSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(i);
                finish();
            }
        });

    }

    private boolean verifyEmpty(String string, String field) {
        if(string.isEmpty()){
            Toast.makeText(LoginActivity.this,field + " field can't be empty!",Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void loginUser(String username, String password){
        Log.i(TAG, "Attempting to login user " + username);
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login ",e );
                    return;
                }

                Toast.makeText(LoginActivity.this,"Success!",Toast.LENGTH_SHORT);
                goMainActivity();
            }
        });
    }

    private void goMainActivity() {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
    }
}