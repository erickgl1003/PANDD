package com.example.pandd;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import es.dmoral.toasty.Toasty;

public class SignupActivity extends AppCompatActivity {

    public static final String TAG = "SignupActivity";
    public static final int UPLOAD_REQUEST = 50;

    private EditText etUsername;
    private EditText etPassword;
    private EditText etPasswordConfirm;
    private EditText etEmail;
    private Button btnSignup;
    private Button btnPhoto;
    private TextView tvLog;
    ProgressDialog progressdialog;

    Bitmap bitmap = null;
    File imagT = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnSignup = findViewById(R.id.btnSignup);
        btnPhoto = findViewById(R.id.btnPhoto);
        tvLog = findViewById(R.id.tvLog);

        //Set progressdialog properties
        progressdialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        progressdialog.setMessage("Please wait...");
        progressdialog.setCancelable(false);


        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUploadPhoto();
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.i(TAG,"onClick Sign in Button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String passwordConfirm = etPasswordConfirm.getText().toString();
                String email = etEmail.getText().toString();

                if(verifyEmpty(username, "Username")) return;
                if(verifyEmpty(password, "Password")) return;
                if(verifyEmpty(passwordConfirm, "Confirm password")) return;
                if(verifyEmpty(email, "Email")) return;

                if(!password.equals(passwordConfirm)){
                    Toasty.warning(SignupActivity.this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressdialog.show();
                signUser(username, password, email,  imagT);
            }
        });

        tvLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private boolean verifyEmpty(String string, String field) {
        if(string.isEmpty()){
            Toasty.warning(SignupActivity.this,field + " field can't be empty!",Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void onUploadPhoto(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, UPLOAD_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == UPLOAD_REQUEST && resultCode == RESULT_OK && data != null){
            Uri photoUri = data.getData();
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }catch (FileNotFoundException e){
                e.printStackTrace();
                Log.e(TAG, "File not found");
                Toasty.info(SignupActivity.this,"File not found. Using default user profile picture",Toast.LENGTH_LONG).show();
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.user);

            } catch (IOException e){
                Log.e(TAG, e.getLocalizedMessage());
                Toasty.info(SignupActivity.this,"Error getting the image. Using default user profile picture",Toast.LENGTH_LONG).show();
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.user);
            }
            File testDir = getApplicationContext().getFilesDir();
            imagT = new File(testDir, "photo.jpg");
            OutputStream os;
            try {
                os = new FileOutputStream(imagT);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
                btnSignup.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toasty.error(SignupActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }
        }
    }

    private void signUser( String username, String password, String email, File file){
        Log.i(TAG, "Attempting to signup user " + username);
        ParseUser user = new ParseUser();
        // Set core properties
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    ParseUser curr = ParseUser.getCurrentUser();
                    savePhoto(curr, new ParseFile(file));
                }
                else{
                    progressdialog.dismiss();
                    Toasty.error(SignupActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error signing user:" + e.getMessage() + "\n" + e.getCause());
                }
            }
        });
    }

    private void savePhoto(ParseUser curr, ParseFile parseFile) {
        curr.put("profile", parseFile);
        curr.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    progressdialog.dismiss();
                    goMainAcitivty();
                }
                else{
                    progressdialog.dismiss();
                    Toasty.error(SignupActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving photo:" + e.getMessage() + "\n" + e.getCause());
                }
            }
        });
    }


    private void goMainAcitivty() {
        Intent i = new Intent(this,MainActivity.class);
        startActivity(i);
        finish();
    }
}