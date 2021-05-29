package it.edu.iisfalcone_righi.blog.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import it.edu.iisfalcone_righi.blog.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int pReqCode = 3;
    private static final int reqCode = 3;

    private FirebaseAuth mAuth;
    private ImageView userImage;
    private ProgressBar changeUserImgProgress;
    private TextView userName;
    private Button changeUserImgBtn;
    private ImageView selected_Pic;

    private Uri pickedImgUri = null;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        selected_Pic = view.findViewById(R.id.selected_pic);
        TextView userAuthEmail = view.findViewById(R.id.user_auth_email);
        userName = view.findViewById(R.id.account_user_name_text_view);
        userImage = view.findViewById(R.id.account_user_image_view);
        changeUserImgBtn = view.findViewById(R.id.change_pic_btn);

        changeUserImgProgress = view.findViewById(R.id.change_pic_bar);

        changeUserImgProgress.setVisibility(View.INVISIBLE);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userAuthEmail.setText(user.getEmail());
            userName.setText(user.getDisplayName());
            //con Glide carico l'immagine
            if (user.getPhotoUrl() != null)
                Glide.with(this).load(user.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(userImage);
            else
                Glide.with(this).load(R.drawable.userphoto).apply(RequestOptions.circleCropTransform()).into(userImage);

        }

        selected_Pic.setOnClickListener(v -> checkRequestPermission());

        changeUserImgBtn.setOnClickListener(v -> {

            changeUserImgBtn.setVisibility(View.INVISIBLE);
            changeUserImgProgress.setVisibility(View.VISIBLE);

            if(pickedImgUri==null){
                showMessage("Seleziona prima un'immagine");
                changeUserImgBtn.setVisibility(View.VISIBLE);
                changeUserImgProgress.setVisibility(View.INVISIBLE);

            }else {

                updateUserInfo(userName.getText().toString(), pickedImgUri, mAuth.getCurrentUser());


                if (user.getPhotoUrl() != null)
                    Glide.with(FragmentManager.findFragment(v)).load(pickedImgUri).apply(RequestOptions.circleCropTransform()).into(userImage);
                else
                    Glide.with(FragmentManager.findFragment(v)).load(R.drawable.userphoto).apply(RequestOptions.circleCropTransform()).into(userImage);
            }
        });


        return view;
    }

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
                        showMessage("Immagine aggiornata!");
                        changeUserImgBtn.setVisibility(View.VISIBLE);
                        changeUserImgProgress.setVisibility(View.INVISIBLE);

                    } else {
                        showMessage("Cambio immagine fallita" + task.getException().getLocalizedMessage());
                        changeUserImgBtn.setVisibility(View.VISIBLE);
                        changeUserImgProgress.setVisibility(View.INVISIBLE);
                    }
                });

                //cambio l'immagine a tutti i post dell'utente che ha richiesto la modifica
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference rootRef = FirebaseDatabase.getInstance("https://blogapp-b229c-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                DatabaseReference postsRef = rootRef.child("Post");
                Query query = postsRef
                        .orderByChild("userId").equalTo(uid);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ds.child("userPhoto")
                                    .getRef()
                                    .setValue(uri.toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("ERRORE", error.getMessage()); //Don't ignore errors!
                    }
                });


                rootRef.child("Commenti").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            for (DataSnapshot dataSnapshot : ds.getChildren()) {
                                if (dataSnapshot.child("userId").getValue().equals(uid)) {
                                    dataSnapshot.child("userImg")
                                            .getRef()
                                            .setValue(uri.toString());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Log.d("", error.getMessage()); //Don't ignore errors!
                    }
                });

                updateNavHeader();

            });
        });
    }

    public void updateNavHeader() {
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
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
                Glide.with(getActivity().getApplicationContext()).load(user.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(navUserPhoto);
            else
                Glide.with(getActivity().getApplicationContext()).load(R.drawable.userphoto).apply(RequestOptions.circleCropTransform()).into(navUserPhoto);


        });
    }

    //metodo veloce per visualizzare messaggi
    private void showMessage(String msg) {
        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void checkRequestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getActivity(), "Consentire il permesso per favore", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, pReqCode);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == reqCode && data != null) {
            pickedImgUri = data.getData();
            selected_Pic.setImageURI(pickedImgUri);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }
}