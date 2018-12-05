package com.udacity.podkis;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.udacity.podkis.entity.Episode;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    private static final String TAG = EpisodeAdapter.class.getSimpleName();

    private final Context mContext;
    private EpisodeClickHandler mEpisodeClickHandler;
    private List<Episode> mEpisodeList;
    private String mPodcastImageUrl = null;

    public EpisodeAdapter(@NonNull final Context context, EpisodeClickHandler episodeClickHandler, List<Episode> episodeList) {
        this.mContext = context;
        this.mEpisodeClickHandler = episodeClickHandler;
        this.mEpisodeList = episodeList;
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.episode_item, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {

        Episode episode = mEpisodeList.get(position);

        // Check which image URL to use.
        String episodeImageUrl = episode.imageUrl;
        if (episodeImageUrl == null
                || episodeImageUrl.isEmpty()) {
            episodeImageUrl = mPodcastImageUrl;
        }

        if (episodeImageUrl != null
                && !episodeImageUrl.isEmpty()) {
            Picasso.get()
                    .load(episodeImageUrl)
                    .transform(new Transformation() {
                        @Override
                        public Bitmap transform(Bitmap source) {
                            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                            int targetWidth = holder.mEpisodeImage.getWidth();
                            int targetHeight = (int) (targetWidth * aspectRatio);
                            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                            if (result != source) {
                                source.recycle();
                            }
                            return result;
                        }

                        @Override
                        public String key() {
                            return TAG;
                        }
                    })
                    .placeholder(ContextCompat.getDrawable(mContext, R.drawable.web_hi_res_512_square))
                    .into(holder.mEpisodeImage);
        } else {
            holder.mEpisodeImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.ic_launcher_square));
        }


        holder.mEpisodeTitle.setText(episode.title);
        if (episode.seasonNumber != null) {
            holder.mEpisodeSeasonNumber.setText(String.format(Locale.getDefault(), "Season %d", episode.seasonNumber));
        } else {
            holder.mEpisodeSeasonNumber.setVisibility(View.GONE);
        }
        holder.mEpisodeNumber.setText(String.format(Locale.getDefault(), "Episode %d", episode.episodeNumber));

        // Format published date.
        holder.mEpisodePublishedDate.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(episode.publishedDate));

    }

    @Override
    public int getItemCount() {
        if (mEpisodeList == null) {
            return 0;
        }
        return mEpisodeList.size();
    }

    public void swapEpisodes(List<Episode> episodeList) {
        this.mEpisodeList = episodeList;
        notifyDataSetChanged();
    }

    public void setPodcastImageUrl(String podcastImageUrl) {
        this.mPodcastImageUrl = podcastImageUrl;
    }

    public interface EpisodeClickHandler {
        void onClickEpisode(Long id);
    }

    public class EpisodeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView mEpisodeImage;
        final TextView mEpisodeTitle;
        final TextView mEpisodeSeasonNumber;
        final TextView mEpisodeNumber;
        final TextView mEpisodePublishedDate;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);

            mEpisodeImage = itemView.findViewById(R.id.episode_image);
            mEpisodeTitle = itemView.findViewById(R.id.episode_title);
            mEpisodeSeasonNumber = itemView.findViewById(R.id.episode_season_number);
            mEpisodeNumber = itemView.findViewById(R.id.episode_number);
            mEpisodePublishedDate = itemView.findViewById(R.id.episode_published_date);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mEpisodeClickHandler.onClickEpisode(mEpisodeList.get(getAdapterPosition()).id);
        }
    }
}
