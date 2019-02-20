package com.udacity.podkis;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.udacity.podkis.entity.Episode;
import com.udacity.podkis.service.PodcastPlayerService;
import com.udacity.podkis.service.PodcastPlayerWidgetService;
import com.udacity.podkis.viewmodel.EpisodeViewModel;
import com.udacity.podkis.viewmodel.EpisodeViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_IMAGE_URL;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_TITLE;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_EPISODE_ID;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_IS_DUAL_PANE;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_PREVIOUS_EPISODE_ID;
import static com.udacity.podkis.service.PodcastPlayerService.INTENT_KEY_EPISODE_DESCRIPTION;
import static com.udacity.podkis.service.PodcastPlayerService.INTENT_KEY_EPISODE_IMAGE_URL;
import static com.udacity.podkis.service.PodcastPlayerService.INTENT_KEY_EPISODE_TITLE;
import static com.udacity.podkis.service.PodcastPlayerService.INTENT_KEY_EPISODE_URL;
import static com.udacity.podkis.service.PodcastPlayerWidgetService.ACTION_PODCAST_PLAYER_WIDGET;

public class EpisodeDetailFragment extends Fragment {

    private static final String TAG = EpisodeDetailFragment.class.getSimpleName();

    private static int sOrientation;
    private static boolean sIsDualPane;
    private static boolean mIsBound = false;

    private Context mContext;
    private Bundle mBundle;
    private OnPodcastEpisodeBackSelectedListener mOnPodcastEpisodeBackSelectedListener;
    private Long mEpisodeId;
    private Long mPreviousEpisodeId;
    private String mPodcastTitle;
    private String mEpisodeTitle;
    private String mEpisodeDescription;
    private String mPodcastImageUrl;

    private Toolbar mToolbar;
    private TextView mEpisodeSeasonNumberTextView;
    private TextView mEpisodeNumberTextView;
    private TextView mEpisodePublishedDateTextView;
    private TextView mEpisodeDescriptionTextView;
    private EpisodeViewModel mEpisodeViewModel;
    private PodcastPlayerServiceConnection mPodcastPlayerServiceConnection;
    private PlayerView mPlayerView;

    public EpisodeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_episode_detail, container, false);

        mBundle = getArguments();
        if (mBundle == null) {
            mBundle = getActivity().getIntent().getExtras();
        }

        sIsDualPane = mBundle.getBoolean(INTENT_KEY_IS_DUAL_PANE, false);
        mPodcastTitle = mBundle.getString(INTENT_KEY_PODCAST_TITLE, null);
        mEpisodeId = mBundle.getLong(INTENT_KEY_EPISODE_ID, -1L);
        mPreviousEpisodeId = mBundle.getLong(INTENT_KEY_PREVIOUS_EPISODE_ID, -1L);
        mPodcastImageUrl = mBundle.getString(INTENT_KEY_PODCAST_IMAGE_URL);

        if (!sIsDualPane) {
            mToolbar = view.findViewById(R.id.episode_detail_toolbar);
            mToolbar.setNavigationOnClickListener(v -> {
                if (mOnPodcastEpisodeBackSelectedListener != null) {
                    mOnPodcastEpisodeBackSelectedListener.onPodcastEpisodeBackSelected();
                }
            });
        }

        sOrientation = getResources().getConfiguration().orientation;

        if (sOrientation != ORIENTATION_LANDSCAPE) {
            mEpisodeSeasonNumberTextView = view.findViewById(R.id.episode_detail_season_number);
            mEpisodeNumberTextView = view.findViewById(R.id.episode_detail_number);
            mEpisodePublishedDateTextView = view.findViewById(R.id.episode_detail_published_date);
            mEpisodeDescriptionTextView = view.findViewById(R.id.episode_detail_description);
        }

        mPlayerView = view.findViewById(R.id.podcast_episode_player);
        mPlayerView.setPlayer(null);
        if (mEpisodeId == -1L) {
            mPlayerView.setVisibility(View.GONE);
        }

        mContext = getContext();

        mEpisodeViewModel = ViewModelProviders.of(this, new EpisodeViewModelFactory(mContext, mEpisodeId)).get(EpisodeViewModel.class);
        mEpisodeViewModel.getEpisode().observe(this, this::bindEpisode);

        mPodcastPlayerServiceConnection = new PodcastPlayerServiceConnection();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mIsBound) {
            mContext.unbindService(mPodcastPlayerServiceConnection);
            mIsBound = false;
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void bindEpisode(Episode episode) {
        Log.d(TAG, String.format("bindEpisode - episode:%s", episode));
        if (episode == null
                || !episode.id.equals(mEpisodeId)) {
            return;
        }

        mEpisodeTitle = episode.title;
        mEpisodeDescription = episode.description;

        if (!sIsDualPane) {
            mToolbar.setTitle(mEpisodeTitle);
        } else {
            if (mPlayerView.getVisibility() == View.GONE) {
                mPlayerView.setVisibility(View.VISIBLE);
            }
        }

        if (sOrientation != ORIENTATION_LANDSCAPE) {
            if (episode.seasonNumber != null) {
                mEpisodeSeasonNumberTextView.setText(String.format(Locale.getDefault(), "Season %d", episode.seasonNumber));
            }

            if (episode.episodeNumber != null) {
                mEpisodeNumberTextView.setText(String.format(Locale.getDefault(), "Episode %d", episode.episodeNumber));
            }

            mEpisodePublishedDateTextView.setText(new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(episode.publishedDate));
            Spanned html;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                html = Html.fromHtml(episode.description, Html.FROM_HTML_MODE_COMPACT);
            } else {
                html = Html.fromHtml(episode.description);
            }
            mEpisodeDescriptionTextView.setText(html);
        }

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
                        mPlayerView.setDefaultArtwork(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Log.e(TAG, String.format("onBitmapFailed - e:%s", e.getMessage()));
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

        String episodeUrl = episode.url;
        Intent audioPlayerServiceIntent = new Intent(mContext, PodcastPlayerService.class);
        mBundle.putLong(INTENT_KEY_EPISODE_ID, mEpisodeId);
        mBundle.putString(INTENT_KEY_EPISODE_TITLE, mEpisodeTitle);
        mBundle.putString(INTENT_KEY_EPISODE_DESCRIPTION, mEpisodeDescription);
        mBundle.putString(INTENT_KEY_EPISODE_URL, episodeUrl);
        mBundle.putString(INTENT_KEY_EPISODE_IMAGE_URL, episodeImageUrl);
        audioPlayerServiceIntent.putExtras(mBundle);

        if (!mIsBound) {
            if (!mPreviousEpisodeId.equals(mEpisodeId)) {
                mContext.stopService(audioPlayerServiceIntent);
                Util.startForegroundService(mContext, audioPlayerServiceIntent);

                // Create intent to update widget.
                Intent widgetIntent = new Intent(mContext, PodcastPlayerWidgetService.class);
                widgetIntent.setAction(ACTION_PODCAST_PLAYER_WIDGET);
                widgetIntent.putExtra(INTENT_KEY_PODCAST_TITLE, mPodcastTitle);
                widgetIntent.putExtra(INTENT_KEY_EPISODE_TITLE, mEpisodeTitle);
                widgetIntent.putExtra(INTENT_KEY_EPISODE_IMAGE_URL, episodeImageUrl);
                mContext.startService(widgetIntent);
            }

            mContext.bindService(audioPlayerServiceIntent, mPodcastPlayerServiceConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    public interface OnPodcastEpisodeBackSelectedListener {
        void onPodcastEpisodeBackSelected();
    }

    public void setOnPodcastEpisodeBackSelectedListener(OnPodcastEpisodeBackSelectedListener onPodcastEpisodeBackSelectedListener) {
        this.mOnPodcastEpisodeBackSelectedListener = onPodcastEpisodeBackSelectedListener;
    }

    class PodcastPlayerServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, String.format("onServiceConnected - name:%s, service:%s", name, service));
            if (service instanceof PodcastPlayerService.AudioPlayerServiceBinder) {
                PodcastPlayerService.AudioPlayerServiceBinder audioPlayerServiceBinder = (PodcastPlayerService.AudioPlayerServiceBinder) service;
                mPlayerView.setPlayer(audioPlayerServiceBinder.getSimpleExoPlayer());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, String.format("onServiceDisconnected - name:%s", name));
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.d(TAG, String.format("onBindingDied - name:%s", name));
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.d(TAG, String.format("onNullBinding - name:%s", name));
        }
    }
}
