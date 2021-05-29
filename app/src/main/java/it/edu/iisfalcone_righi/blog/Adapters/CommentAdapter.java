package it.edu.iisfalcone_righi.blog.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import it.edu.iisfalcone_righi.blog.Models.Comment;
import it.edu.iisfalcone_righi.blog.R;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context mContext;
    private final List<Comment> mData;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    public CommentAdapter(Context mContext, List<Comment> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @NotNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.row_comment, parent, false);
        return new CommentViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull CommentViewHolder holder, int position) {
        String userimg = mData.get(position).getUserImg();
        if (userimg != null)
            Glide.with(mContext).load(userimg).apply(RequestOptions.circleCropTransform()).into(holder.img_user);
        else
            Glide.with(mContext).load(R.drawable.userphoto).apply(RequestOptions.circleCropTransform()).into(holder.img_user);
        holder.tv_name.setText(mData.get(position).getUserName());
        holder.tv_content.setText(mData.get(position).getContent());
        holder.tv_date.setText(timestampToString((Long) mData.get(position).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ITALIAN);
        calendar.setTimeInMillis(time);
        return DateFormat.format("hh:mm", calendar).toString();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        final ImageView img_user;
        final TextView tv_name;
        final TextView tv_content;
        final TextView tv_date;

        public CommentViewHolder(View itemView) {
            super(itemView);
            img_user = itemView.findViewById(R.id.comment_user_img);
            tv_name = itemView.findViewById(R.id.comment_username);
            tv_content = itemView.findViewById(R.id.comment_content);
            tv_date = itemView.findViewById(R.id.comment_date);

            firebaseDatabase = FirebaseDatabase.getInstance("https://blogapp-b229c-default-rtdb.europe-west1.firebasedatabase.app/");


            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Commento numero " + position)
                        .setMessage("Sei sicuro di voler eliminare questo commento?")
                        .setPositiveButton("Si", (dialog, which) -> {
                            databaseReference = firebaseDatabase.getReference("Commenti/"+mData.get(position).getPostkey());
                            databaseReference.child(mData
                                    .get(position)
                                    .getKey())
                                    .removeValue();
                            mData.remove(position);
                            notifyDataSetChanged();


                        }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
                return false;
            });
        }
    }
}
