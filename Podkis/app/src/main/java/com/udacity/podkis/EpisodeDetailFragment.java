package com.udacity.podkis;

import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.udacity.podkis.entity.Episode;
import com.udacity.podkis.viewmodel.EpisodeViewModel;
import com.udacity.podkis.viewmodel.EpisodeViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_IMAGE_URL;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_EPISODE_ID;


public class EpisodeDetailFragment extends Fragment implements ExoPlayer.EventListener {

    private static final String TAG = EpisodeDetailFragment.class.getSimpleName();
    private static final String INTENT_KEY_CURRENT_POSITION = "current_position";

    private Context mContext;
    private OnPodcastEpisodeBackSelectedListener mOnPodcastEpisodeBackSelectedListener;
    private Long mEpisodeId;
    private String mPodcastImageUrl;

    private Toolbar mToolbar;
    private TextView mEpisodeSeasonNumberTextView;
    private TextView mEpisodeNumberTextView;
    private TextView mEpisodePublishedDateTextView;
    private TextView mEpisodeDescriptionTextView;
    private EpisodeViewModel mEpisodeViewModel;

    private SimpleExoPlayerView mSimpleExoPlayerView;
    private SimpleExoPlayer mSimpleExoPlayer;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;
    private long mCurrentPosition;

    public EpisodeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_episode_detail, container, false);
        mToolbar = view.findViewById(R.id.episode_detail_toolbar);
        mToolbar.setNavigationOnClickListener(v -> mOnPodcastEpisodeBackSelectedListener.onPodcastEpisodeBackSelected());

        mEpisodeSeasonNumberTextView = view.findViewById(R.id.episode_detail_season_number);
        mEpisodeNumberTextView = view.findViewById(R.id.episode_detail_number);
        mEpisodePublishedDateTextView = view.findViewById(R.id.episode_detail_published_date);
        mEpisodeDescriptionTextView = view.findViewById(R.id.episode_detail_description);
        mSimpleExoPlayerView = view.findViewById(R.id.podcast_episode_player);

        mContext = getContext();

        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = getActivity().getIntent().getExtras();
        }

        mEpisodeId = bundle.getLong(INTENT_KEY_EPISODE_ID, -1L);
        mPodcastImageUrl = bundle.getString(INTENT_KEY_PODCAST_IMAGE_URL);

        mEpisodeViewModel = ViewModelProviders.of(this, new EpisodeViewModelFactory(mContext, mEpisodeId)).get(EpisodeViewModel.class);
        mEpisodeViewModel.getEpisode().observe(this, this::bindEpisode);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSimpleExoPlayer != null) {
            outState.putLong(INTENT_KEY_CURRENT_POSITION, mSimpleExoPlayer.getCurrentPosition());
            mSimpleExoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        long currentPosition = 0L;
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getLong(INTENT_KEY_CURRENT_POSITION, 0L);
        }
        mCurrentPosition = currentPosition;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer();
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e(TAG, error.getMessage());
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    private void bindEpisode(Episode episode) {
        Log.d(TAG, String.format("bindEpisode - episode:%s", episode));
        if (episode != null
                && !episode.id.equals(mEpisodeId)) {
            return;
        }
        mToolbar.setTitle(episode.title);
        if (episode.seasonNumber != null) {
            mEpisodeSeasonNumberTextView.setText(String.format(Locale.getDefault(), "Season %d", Integer.valueOf(episode.seasonNumber)));
        }
        mEpisodeNumberTextView.setText(String.format(Locale.getDefault(), "Episode %d", Integer.valueOf(episode.episodeNumber)));
        mEpisodePublishedDateTextView.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(episode.publishedDate));
        Spanned html;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            html = Html.fromHtml(episode.description, Html.FROM_HTML_MODE_COMPACT);
        } else {
            html = Html.fromHtml(episode.description);
        }
        mEpisodeDescriptionTextView.setText(html);
        String episodeImageUrl = episode.imageUrl;
        if (episodeImageUrl == null
                || episodeImageUrl.isEmpty()) {
            episodeImageUrl = mPodcastImageUrl;
        }
        Picasso.get()
                .load(episodeImageUrl)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mSimpleExoPlayerView.setDefaultArtwork(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

        String episodeUrl = episode.url;
        initializePlayer(Uri.parse(episodeUrl));
        initializeMediaSession();
    }

    private void initializePlayer(Uri uri) {

        if (mSimpleExoPlayer == null) {

            mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext,
                    new DefaultTrackSelector(), new DefaultLoadControl());
            mSimpleExoPlayerView.setPlayer(mSimpleExoPlayer);

            String userAgent = Util.getUserAgent(mContext, getString(R.string.app_name));
            MediaSource mediaSource = new ExtractorMediaSource(uri, new DefaultDataSourceFactory(
                    mContext, userAgent), new DefaultExtractorsFactory(), null, null);
            if (mCurrentPosition != C.TIME_UNSET) {
                mSimpleExoPlayer.seekTo(mCurrentPosition);
            }
            mSimpleExoPlayer.prepare(mediaSource);
            mSimpleExoPlayer.setPlayWhenReady(true);
        }

    }

    private void releasePlayer() {
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }

        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.stop();
            mSimpleExoPlayer.release();
            mSimpleExoPlayer = null;
        }

        if (mMediaSession != null) {
            mMediaSession.setActive(false);
        }
    }

    private void initializeMediaSession() {

        mMediaSession = new MediaSessionCompat(mContext, TAG);
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mMediaSession.setMediaButtonReceiver(null);

        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());


        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                mSimpleExoPlayer.setPlayWhenReady(true);
            }

            @Override
            public void onPause() {
                mSimpleExoPlayer.setPlayWhenReady(false);
            }

            @Override
            public void onSkipToPrevious() {
                mSimpleExoPlayer.seekTo(0);
            }
        });

        mMediaSession.setActive(true);

    }

    public interface OnPodcastEpisodeBackSelectedListener {
        void onPodcastEpisodeBackSelected();
    }

    public void setOnPodcastEpisodeBackSelectedListener(OnPodcastEpisodeBackSelectedListener onPodcastEpisodeBackSelectedListener) {
        this.mOnPodcastEpisodeBackSelectedListener = onPodcastEpisodeBackSelectedListener;
    }
}
