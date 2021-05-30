package it.edu.iisfalcone_righi.blog.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import it.edu.iisfalcone_righi.blog.R;

public class LoginActivity extends AppCompatActivity {

    private EditText userMail, userPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private Intent HomeActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userMail = findViewById(R.id.login_mail);
        userPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.loginBtn);
        loginProgress = findViewById(R.id.login_progress);
        mAuth = FirebaseAuth.getInstance();
        HomeActivity = new Intent(this, it.edu.iisfalcone_righi.blog.Activities.HomeActivity.class);
        ImageView loginPhoto = findViewById(R.id.login_photo);
        loginPhoto.setOnClickListener(v -> {
            Intent registerActivity = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(registerActivity);
        });

        loginProgress.setVisibility(View.INVISIBLE);
        btnLogin.setOnClickListener(v -> {
            loginProgress.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.INVISIBLE);

            final String mail = userMail.getText().toString();
            final String password = userPassword.getText().toString();

            if (mail.isEmpty() || password.isEmpty()) {
                showMessage("Assicurati di inserire tutti i dati.");
                btnLogin.setVisibility(View.VISIBLE);
                loginProgress.setVisibility(View.INVISIBLE);
            }else {
                signIn(mail,password);
            }

        });
    }
    //metodo per far il login
    private void signIn(String mail, String password) {
        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                loginProgress.setVisibility(View.INVISIBLE);
                btnLogin.setVisibility(View.VISIBLE);
                updateUI();
            }else {
                showMessage(task.getException().getLocalizedMessage());
                btnLogin.setVisibility(View.VISIBLE);
                loginProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void updateUI() {
        startActivity(HomeActivity);
        finish();
    }

    //metodo veloce per visualizzare messaggi
    private void showMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            //se l'utente è loggato
            updateUI();
        }
    }
}