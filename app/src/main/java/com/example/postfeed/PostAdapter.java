package com.example.postfeed;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context myContext;
    List<Post> posts;
    PostAdapter(Context context, ArrayList<Post> posts)
    {
        this.myContext=context;
        this.posts=posts;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.recyclerviewunit, parent, false);
        return new PostViewHolder(itemView,myContext);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((PostViewHolder) holder).setPostDetails(posts.get(position));
    }
    public void updateList(List<Post> list){
     this.posts = list;
     notifyDataSetChanged();
}

    @Override
    public int getItemCount() {
        return posts.size();
    }
    static class PostViewHolder extends RecyclerView.ViewHolder{
        TextView username,text;
        ImageView image;
        Context myContext;
        public PostViewHolder(@NonNull View itemView,Context context) {
            super(itemView);
            username=itemView.findViewById(R.id.username);
            text=itemView.findViewById(R.id.text);
            image=itemView.findViewById(R.id.image);
            this.myContext=context;
        }
        public  void setPostDetails(Post post)
        {
            username.setText(post.getUsername());
            text.setText(post.contenttext);
            //set image using glide;
            Log.i("🙏🏻",post.getContentimageurl());
            if(post.getContentimageurl()!=null && post.getContentimageurl().compareTo("null")!=0)
            {
                Glide.with(myContext).load(post.getContentimageurl()).into(image);
            }
        }
    }
}
