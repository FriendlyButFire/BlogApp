package it.edu.iisfalcone_righi.blog.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.edu.iisfalcone_righi.blog.Activities.PostDetailActivity;
import it.edu.iisfalcone_righi.blog.Models.Post;
import it.edu.iisfalcone_righi.blog.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    final Context mContext;
    final List<Post> mData;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    public PostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @NotNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View row = LayoutInflater.from(mContext).inflate(R.layout.row_post_item, parent, false);
        return new PostViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PostViewHolder holder, int position) {
        holder.tvTitle.setText(mData.get(position).getTitle());
        Glide.with(mContext).load(mData.get(position).getPicture()).into(holder.imgPost);
        String userimg = mData.get(position).getUserPhoto();
        if (userimg != null)
            Glide.with(mContext).load(userimg).apply(RequestOptions.circleCropTransform()).into(holder.imgPostProfile);
        else
            Glide.with(mContext).load(R.drawable.userphoto).apply(RequestOptions.circleCropTransform()).into(holder.imgPostProfile);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final ImageView imgPost;
        final ImageView imgPostProfile;

        public PostViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.row_post_title);
            imgPost = itemView.findViewById(R.id.row_post_img);
            imgPostProfile = itemView.findViewById(R.id.row_post_profile_img);


            firebaseDatabase = FirebaseDatabase.getInstance("https://blogapp-b229c-default-rtdb.europe-west1.firebasedatabase.app/");
            databaseReference = firebaseDatabase.getReference("Post");

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Post numero " + position)
                        .setMessage("Sei sicuro di voler eliminare questo post?")
                        .setPositiveButton("Si", (dialog, which) -> {

                            databaseReference.child(mData.
                                    get(position).
                                    getKey())
                                    .removeValue()
                                    .addOnSuccessListener(unused -> Toast.makeText(mContext, "Post eliminato con successo!", Toast.LENGTH_LONG).show()).addOnFailureListener(e -> Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_LONG).show());

                            mData.remove(position);
                            notifyDataSetChanged();

                        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
                return false;
            });

            //invio i dati all'activity che gestisce i post

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                Intent postDetailActivity = new Intent(mContext, PostDetailActivity.class);
                postDetailActivity.putExtra("title", mData.get(position).getTitle());
                postDetailActivity.putExtra("postImage", mData.get(position).getPicture());
                postDetailActivity.putExtra("description", mData.get(position).getDescription());
                postDetailActivity.putExtra("postKey", mData.get(position).getKey());
                postDetailActivity.putExtra("userPhoto", mData.get(position).getUserPhoto());
                postDetailActivity.putExtra("userName",mData.get(position).getUserName());
                long timestamp = (long) mData.get(position).getTimeStamp();
                postDetailActivity.putExtra("postDate", timestamp);
                mContext.startActivity(postDetailActivity);
            });
        }

    }
}
