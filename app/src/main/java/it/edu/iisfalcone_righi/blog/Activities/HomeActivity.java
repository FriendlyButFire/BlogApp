package it.edu.iisfalcone_righi.blog.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import it.edu.iisfalcone_righi.blog.Fragments.HomeFragment;
import it.edu.iisfalcone_righi.blog.Fragments.ProfileFragment;
import it.edu.iisfalcone_righi.blog.Models.Post;
import it.edu.iisfalcone_righi.blog.R;

public class HomeActivity extends AppCompatActivity {

    private static int pReqCode = 2;
    private static int reqCode = 2;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Dialog popAddPost;
    ImageView popupUserImage, popupPostImage, popupAddBtn;
    TextView popupTitle, popupDescription;
    ProgressBar popupClickProgress;
    private AppBarConfiguration mAppBarConfiguration;
    private Uri pickedImgUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

        //init firebase

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //init popup

        initPopup();

        setupPopupImageClick();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> popAddPost.show());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case R.id.nav_home:
                    getSupportActionBar().setTitle(R.string.nav_home);
                    fab.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
                    break;
                case R.id.nav_profile:
                    getSupportActionBar().setTitle(R.string.nav_profile);
                    fab.setVisibility(View.INVISIBLE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ProfileFragment()).commit();
                    break;
                case R.id.nav_signout:
                    FirebaseAuth.getInstance().signOut();
                    Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(loginActivity);
                    finish();
                    break;

            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        updateNavHeader();
        //imposto il fragment come default
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        MenuItem item = menu.findItem(R.id.action_settings);
        if (item != null) {
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void initPopup() {
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        //init popup widget

        popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
        popupPostImage = popAddPost.findViewById(R.id.popup_img);
        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupAddBtn = popAddPost.findViewById(R.id.popup_add);
        popupClickProgress = popAddPost.findViewById(R.id.popup_progressBar);

        //carico l'immagine dell'user

        Glide.with(this).load(currentUser.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(popupUserImage);

        //aggiungo un listener

        popupAddBtn.setOnClickListener(v -> {
            popupAddBtn.setVisibility(View.INVISIBLE);
            popupClickProgress.setVisibility(View.VISIBLE);

            //test titolo e desc

            if (!popupTitle.getText().toString().isEmpty() && !popupDescription.getText().toString().isEmpty() && pickedImgUri != null) {
                //tutto ok
                //accedo allo storage di firebase per l'immagine scelta

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("immagini_blog");
                StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());
                imageFilePath.putFile(pickedImgUri).addOnSuccessListener(taskSnapshot -> imageFilePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageDownloadLink = uri.toString();
                    //creo l'oggetto post

                    if (currentUser.getPhotoUrl() != null) {
                        Post post = new Post(popupTitle.getText().toString(),
                                popupDescription.getText().toString(),
                                imageDownloadLink, currentUser.getUid(),
                                currentUser.getPhotoUrl().toString()
                                , currentUser.getDisplayName()
                        );

                        //aggiungo il post al database

                        addPost(post);

                    } else {
                        Post post = new Post(popupTitle.getText().toString(),
                                popupDescription.getText().toString(),
                                imageDownloadLink, currentUser.getUid(),
                                null
                                , currentUser.getDisplayName()
                        );

                        //aggiungo il post al database

                        addPost(post);

                    }


                }).addOnFailureListener(e -> {
                    //qualcosa non va con la foto
                    showMessage(e.getLocalizedMessage());
                    popupClickProgress.setVisibility(View.INVISIBLE);
                    popupAddBtn.setVisibility(View.VISIBLE);
                }));


            } else {
                showMessage("Assicurati di inserire tutti i dati e di scegliere un immagine");
                popupAddBtn.setVisibility(View.VISIBLE);
                popupClickProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    //metodo veloce per visualizzare messaggi
    private void showMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    public void updateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_username);
        TextView navUserMail = headerView.findViewById(R.id.nav_user_mail);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

        //ottengo i dati dall'utente e li visualizzo

        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.getCurrentUser().reload().addOnSuccessListener(unused -> {
            FirebaseUser user = auth.getCurrentUser();

            navUserMail.setText(user.getEmail());
            navUsername.setText(user.getDisplayName());

            //con Glide carico l'immagine
            if (user.getPhotoUrl() != null)
                Glide.with(getApplicationContext()).load(user.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(navUserPhoto);
            else
                Glide.with(getApplicationContext()).load(R.drawable.userphoto).apply(RequestOptions.circleCropTransform()).into(navUserPhoto);


        });


    }

    private void setupPopupImageClick() {
        popupPostImage.setOnClickListener(v -> {
            //quando clicco l'immagine apre la galleria; prima però vedo se ho i permessi
            checkRequestPermission();
        });
    }

    private void checkRequestPermission() {
        if (ContextCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(HomeActivity.this, "Consentire il permesso per favore", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, pReqCode);
                pReqCode+=1;
            }
        } else openGallery();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, reqCode);
    }

    //quando l'utente sceglie un'immagine
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == reqCode && data != null) {
            pickedImgUri = data.getData();
            popupPostImage.setImageURI(pickedImgUri);
            reqCode+=1;

        }
    }


    private void addPost(Post post) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://blogapp-b229c-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference ref = database.getReference("Post").push();
        //ottengo l'id del post e carico la chiave
        String key = ref.getKey();
        post.setKey(key);

        //aggiungo i dati al database di firebase

        ref.setValue(post).addOnSuccessListener(unused -> {
            showMessage("Post aggiunto con successo!");
            popupClickProgress.setVisibility(View.INVISIBLE);
            popupAddBtn.setVisibility(View.VISIBLE);

            popAddPost.setContentView(R.layout.popup_add_post);
            popAddPost.dismiss();

            pickedImgUri = null;
        });


    }
}
