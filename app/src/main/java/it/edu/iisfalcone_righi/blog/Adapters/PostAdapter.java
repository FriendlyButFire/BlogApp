package it.edu.iisfalcone_righi.blog.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.edu.iisfalcone_righi.blog.Activities.PostDetailActivity;
import it.edu.iisfalcone_righi.blog.Models.Post;
import it.edu.iisfalcone_righi.blog.R;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    Context mContext;
    List<Post> mData;

    public PostAdapter(Context mContext, List<Post> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @NotNull
    @Override
    public PostAdapter.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View row = LayoutInflater.from(mContext).inflate(R.layout.row_post_item, parent, false);
        return new MyViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PostAdapter.MyViewHolder holder, int position) {
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

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView imgPost;
        ImageView imgPostProfile;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.row_post_title);
            imgPost = itemView.findViewById(R.id.row_post_img);
            imgPostProfile = itemView.findViewById(R.id.row_post_profile_img);

            //invio i dati all'activity che gestisce i post

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postDetailActivity = new Intent(mContext, PostDetailActivity.class);
                    int position = getAdapterPosition();
                    postDetailActivity.putExtra("title", mData.get(position).getTitle());
                    postDetailActivity.putExtra("postImage", mData.get(position).getPicture());
                    postDetailActivity.putExtra("description", mData.get(position).getDescription());
                    postDetailActivity.putExtra("postKey", mData.get(position).getKey());
                    postDetailActivity.putExtra("userPhoto", mData.get(position).getUserPhoto());
                    //postDetailActivity.putExtra("userName",mData.get(position).getUserName());
                    long timestamp = (long) mData.get(position).getTimeStamp();
                    postDetailActivity.putExtra("postDate", timestamp);
                    mContext.startActivity(postDetailActivity);
                }
            });
        }

    }
}
