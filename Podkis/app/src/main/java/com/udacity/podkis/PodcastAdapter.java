package com.udacity.podkis;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.udacity.podkis.entity.Podcast;

import java.util.List;

public class PodcastAdapter extends RecyclerView.Adapter<PodcastAdapter.PodcastViewHolder> {

    private final Context mContext;
    private final PodcastClickHandler mPodcastClickHandler;
    private List<Podcast> mPodcastList;

    public PodcastAdapter(Context context, PodcastClickHandler podcastClickHandler, List<Podcast> podcastList) {
        this.mContext = context;
        this.mPodcastClickHandler = podcastClickHandler;
        this.mPodcastList = podcastList;
    }

    @NonNull
    @Override
    public PodcastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.podcast_item, parent, false);
        return new PodcastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PodcastViewHolder holder, int position) {
        Podcast podcast = mPodcastList.get(position);
        String imageUrl = podcast.imageUrl;
        if (imageUrl != null
                && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(ContextCompat.getDrawable(mContext, R.drawable.web_hi_res_512_square))
                    .into(holder.mPodcastThumbnail);
        } else {
            holder.mPodcastThumbnail.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.web_hi_res_512));
        }

        ViewCompat.setTransitionName(holder.mPodcastThumbnail, podcast.title);
    }

    @Override
    public int getItemCount() {
        if (mPodcastList == null) {
            return 0;
        }
        return mPodcastList.size();
    }

    public void swapPodcasts(List<Podcast> podcastList) {
        this.mPodcastList = podcastList;
        notifyDataSetChanged();
    }

    public interface PodcastClickHandler {
        void onClickPodcast(Long id, String title, String description, String imageUrl, ImageView sharedImageView);
    }

    public class PodcastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView mPodcastThumbnail;

        public PodcastViewHolder(@NonNull View itemView) {
            super(itemView);
            mPodcastThumbnail = itemView.findViewById(R.id.podcast_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Podcast podcast = mPodcastList.get(getAdapterPosition());
            mPodcastClickHandler.onClickPodcast(podcast.id, podcast.title, podcast.description, podcast.imageUrl, mPodcastThumbnail);
        }
    }
}
