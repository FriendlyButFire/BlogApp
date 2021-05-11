package it.edu.iisfalcone_righi.blog.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.edu.iisfalcone_righi.blog.Adapters.CommentAdapter;
import it.edu.iisfalcone_righi.blog.Models.Comment;
import it.edu.iisfalcone_righi.blog.R;

public class PostDetailActivity extends AppCompatActivity {


    ImageView imgPost, imgUserPost, imgCurrentUser;
    TextView txtPostDesc, txtPostDateName, txtPostTitle;
    EditText editTextComment;
    Button btnAddComment;
    String postKey;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    FirebaseDatabase firebaseDatabase;

    RecyclerView RvComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //barra di stato trasparente

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getSupportActionBar().hide();

        //init View
        RvComment = findViewById(R.id.rv_comment);
        imgPost = findViewById(R.id.post_detail_img);
        imgUserPost = findViewById(R.id.post_detail_user_img);
        imgCurrentUser = findViewById(R.id.post_detail_currentuser_img);

        txtPostDesc = findViewById(R.id.post_detail_desc);
        txtPostDateName = findViewById(R.id.post_detail_date_name);
        txtPostTitle = findViewById(R.id.post_detail_title);

        editTextComment = findViewById(R.id.post_detail_comment);

        btnAddComment = findViewById(R.id.post_detail_add_comment_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance("https://blogapp-b229c-default-rtdb.europe-west1.firebasedatabase.app/");

        //Aggiungo il tasto commenta

        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddComment.setVisibility(View.INVISIBLE);
                DatabaseReference commentReference = firebaseDatabase.getReference("Commenti").child(postKey).push();
                String comment_content = editTextComment.getText().toString();
                String userId = firebaseUser.getUid();
                String userName = firebaseUser.getDisplayName();
                String userImg = firebaseUser.getPhotoUrl().toString();
                Comment comment = new Comment(comment_content,userId,userImg,userName);

                commentReference.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showMessage("Commento inserito!");
                        editTextComment.setText("");
                        btnAddComment.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        showMessage("Commento non inserito: "+e.getMessage());
                    }
                });
            }
        });

        //ottengo i dati del post per collegarli alle view sopra

        String postImage = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImage).into(imgPost);

        String title = getIntent().getExtras().getString("title");
        txtPostTitle.setText(title);

        String userPhoto = getIntent().getExtras().getString("userPhoto");
        Glide.with(this).load(userPhoto).apply(RequestOptions.circleCropTransform()).into(imgUserPost);

        String description = getIntent().getExtras().getString("description");
        txtPostDesc.setText(description);

        //imposto l'immagine dell'utente che commenta

        Glide.with(this).load(firebaseUser.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(imgCurrentUser);

        //ottengo l'id del post

        postKey = getIntent().getExtras().getString("postKey");

        String date = timestampToString(getIntent().getExtras().getLong("postDate"));

        txtPostDateName.setText(date);

        //init recycleView Commenti
        initRvComment();

    }

    private void initRvComment() {
        RvComment.setLayoutManager(new LinearLayoutManager(this));
        DatabaseReference commentRef = firebaseDatabase.getReference("Commenti").child(postKey);
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                listComment = new ArrayList<>();
                for(DataSnapshot commentSnap : snapshot.getChildren()){
                    Comment comment = commentSnap.getValue(Comment.class);
                    listComment.add(comment);
                }
                commentAdapter=new CommentAdapter(getApplicationContext(), listComment);
                RvComment.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private String timestampToString(long time){
        Calendar calendar = Calendar.getInstance(Locale.ITALIAN);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy",calendar).toString();
        return date;
    }

    //metodo veloce per visualizzare messaggi
    private void showMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}