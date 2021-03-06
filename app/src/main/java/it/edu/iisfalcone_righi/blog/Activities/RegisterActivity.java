package it.edu.iisfalcone_righi.blog.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import it.edu.iisfalcone_righi.blog.R;

public class RegisterActivity extends AppCompatActivity {

    static final int pReqCode = 1;
    static final int reqCode = 1;
    ImageView userPhoto;
    Uri pickedImgUri;

    private EditText userEmail, userPassword, userPassword2, userName;
    private ProgressBar loadingProgress;
    private Button regBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //liste di view

        userEmail = findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPasswd);
        userPassword2 = findViewById(R.id.regPasswd2);
        userName = findViewById(R.id.regName);
        loadingProgress = findViewById(R.id.progressBar);
        regBtn = findViewById(R.id.regBtn);

        loadingProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();


        regBtn.setOnClickListener(v -> {
            regBtn.setVisibility(View.INVISIBLE);
            loadingProgress.setVisibility(View.VISIBLE);
            final String email = userEmail.getText().toString();
            final String password = userPassword.getText().toString();
            final String password2 = userPassword2.getText().toString();
            final String name = userName.getText().toString();

            if (email.isEmpty() || password.isEmpty() || password2.isEmpty() || name.isEmpty()) {
                showMessage("Assicurati di inserire tutti i dati.");
                regBtn.setVisibility(View.VISIBLE);
                loadingProgress.setVisibility(View.INVISIBLE);

            } else {
                CreateUserAccount(email, name, password);
            }
        });


        userPhoto = findViewById(R.id.regUserPhoto);
        userPhoto.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 22) {
                checkRequestPermission();
            } else {
                openGallery();
            }
        });
    }

    private void CreateUserAccount(String email, String name, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                //account creato
                showMessage("Account creato!");
                //controllo se l'utente ha scelto una foto:
                if(pickedImgUri!=null){
                //dopo la creazione imposto foto profilo e nome
                updateUserInfo(name, pickedImgUri, mAuth.getCurrentUser());}
                else updateUserInfoWithoutPhoto(name,mAuth.getCurrentUser());
            } else {
                //account non creato
                showMessage("Creazione account fallita" + task.getException().getLocalizedMessage());
                regBtn.setVisibility(View.VISIBLE);
                loadingProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    //cambio foto profilo e nome
    private void updateUserInfo(String name, @NotNull Uri pickedImgUri, FirebaseUser currentUser) {
        //carico la foto sullo storage di Firebase e chiedo l'url
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("foto utenti");
        StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(taskSnapshot -> {
            //immagine caricata, ora posso avere l'url
            imageFilePath.getDownloadUrl().addOnSuccessListener(uri -> {
                //l'url contiene l'immagine profilo dell'utente
                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(name).setPhotoUri(uri).build();

                currentUser.updateProfile(profileUpdate).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        //informazioni utente modificate
                        showMessage("Registrato con successo!");
                        updateUI();
                    }
                });

            });
        });
    }

    private void updateUserInfoWithoutPhoto(String name, FirebaseUser currentUser) {

        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(name).build();

        currentUser.updateProfile(profileUpdate).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //informazioni utente modificate
                showMessage("Registrato con successo!");
                updateUI();
            }
        });

    }

    private void updateUI() {
        Intent homeActivity = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(homeActivity);
        finish();
    }

    //metodo veloce per visualizzare messaggi
    private void showMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, reqCode);
    }

    private void checkRequestPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(RegisterActivity.this, "Consentire il permesso per favore", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, pReqCode);
            }
        } else openGallery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == reqCode && data != null) {
            pickedImgUri = data.getData();
            userPhoto.setImageURI(pickedImgUri);
        }
    }
}