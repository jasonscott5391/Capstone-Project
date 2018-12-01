package com.udacity.podkis;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.Toolbar;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_EPISODE_ID;


public class EpisodeDetailFragment extends Fragment implements ExoPlayer.EventListener {

    private static final String TAG = EpisodeDetailFragment.class.getSimpleName();
    private static final String INTENT_KEY_CURRENT_POSITION = "current_position";

    private Context mContext;
    private OnPodcastEpisodeBackSelectedListener mOnPodcastEpisodeBackSelectedListener;
    private Long mEpisodeId;

    private Toolbar mToolbar;
    private TextView mEpisodeSeasonNumberTextView;
    private TextView mEpisodeNumberTextView;
    private TextView mEpisodePublishedDateTextView;
    private TextView mEpisodeDescriptionTextView;

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

        mToolbar.setTitle(getString(R.string.test_episode_title));
        mEpisodeSeasonNumberTextView.setText(String.format(Locale.getDefault(), "Season %d", Integer.valueOf(getString(R.string.test_episode_season_number).replace("Season ", ""))));
        mEpisodeNumberTextView.setText(String.format(Locale.getDefault(), "Episode %d", Integer.valueOf(getString(R.string.test_episode_number).replace("Episode ", ""))));
        try {
            mEpisodePublishedDateTextView.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).parse(getString(R.string.test_episode_published_date))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mEpisodeDescriptionTextView.setText(getResources().getString(R.string.test_episode_description));

        String episodeImageUrl = "https://content.production.cdn.art19.com/images/8b/14/c0/af/8b14c0af-828c-4a64-9625-b164ace2fcae/67e5066ddc647cfbd4a4afd089e40d16d896c5ead04ee394d6e893ebca15b8250ba009c2dfde7ebcf5929ae62f9648bafc85cb4b32fd2e008e38d66587acc742.jpeg";
        Picasso.get().load(episodeImageUrl).into(new Target() {
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

        String episodeUrl = "https://rss.art19.com/episodes/9e9abb60-285b-4ffc-a2d2-d7db399eebf6.mp3";
        initializePlayer(Uri.parse(episodeUrl));
        initializeMediaSession();

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

    }

    @Override
    public void onPositionDiscontinuity() {

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
