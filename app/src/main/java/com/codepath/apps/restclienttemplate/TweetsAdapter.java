package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {
    Context context;
    List<Tweet> tweets;

    public void clear(){
        tweets.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Tweet> list){
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    //Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    @NonNull
    //For each row, inflate the layout
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    //Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //get data at position
        Tweet tweet = tweets.get(position);
        //bind the tweet with view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    //Define a viewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvScreenName;
        TextView tvBody;
        ImageView ivProfile;
        TextView tvDate;
        ImageView ivPhoto;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvBody = itemView.findViewById(R.id.tvBody);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
        }

        public void bind(Tweet tweet){
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            tvDate.setText(tweet.createdAt);
            tvName.setText(tweet.user.name);
            int margin = 0;
            int radius = 100;
            Glide.with(context).load(tweet.user.profileImageUrl).transform(new RoundedCornersTransformation(radius, margin)).into(ivProfile);
            if(tweet.entityPhoto != null){
                Glide.with(context).load(tweet.entityPhoto).transform(new RoundedCornersTransformation(radius, margin)).into(ivPhoto);
                ivPhoto.setVisibility(View.VISIBLE);
            }else{
                ivPhoto.setVisibility(View.GONE);
            }
        }
    }
}
