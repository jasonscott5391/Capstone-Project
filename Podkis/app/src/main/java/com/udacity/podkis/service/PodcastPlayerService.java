package com.udacity.podkis.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.Html;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter;
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.udacity.podkis.PodcastDetailActivity;
import com.udacity.podkis.R;

import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_EPISODE_ID;
import static com.udacity.podkis.service.PodcastPlayerWidgetService.ACTION_PODCAST_PLAYER_WIDGET;

public class PodcastPlayerService extends Service {

    public static final String TAG = PodcastPlayerService.class.getSimpleName();
    public static final String PLAYBACK_CHANNEL_ID = "playback_channel";
    public static final int PLAYBACK_NOTIFICATION_ID = 1;
    public static final String MEDIA_SESSION_TAG = "audio_demo";
    public static final String INTENT_KEY_EPISODE_TITLE = "episode_title";
    public static final String INTENT_KEY_EPISODE_DESCRIPTION = "episode_description";
    public static final String INTENT_KEY_EPISODE_URL = "episode_url";
    public static final String INTENT_KEY_EPISODE_IMAGE_URL = "episode_image_url";
    private static final String PREFS_KEY_CURRENT_POSITION = "current_position";
    private static final long FIVE_SECONDS = 5 * 1000;

    private final IBinder mAudioPlayerServiceBinder = new AudioPlayerServiceBinder();

    private static int sStartId;

    private Context mContext;
    private Bundle mBundle;
    private SimpleExoPlayer mSimpleExoPlayer;
    private PlayerNotificationManager mPlayerNotificationManager;
    private MediaSessionCompat mMediaSession;
    private MediaSessionConnector mMediaSessionConnector;
    private Bitmap mEpisodeBitmap;

    private Long mEpisodeId;
    private String mEpisodeTitle;
    private String mEpisodeDescription;
    private String mEpisodeImageUrl;
    private long mCurrentPosition = -1L;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // Determine if podcast over.
        long duration = mSimpleExoPlayer.getDuration();
        long currentPosition = mSimpleExoPlayer.getCurrentPosition();
        long positionToCommit = currentPosition;
        if ((duration - currentPosition) <= FIVE_SECONDS) {
            // Start over.
            positionToCommit = -1L;
        }

        // Commit current position for current episode to shared preferences.
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(getString(R.string.podcast_checksum_prefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(String.format("%s-%s", mEpisodeId, PREFS_KEY_CURRENT_POSITION), positionToCommit);
        editor.apply();

        releasePlayer();
        super.onDestroy();
    }

    private void releasePlayer() {
        mMediaSession.release();
        mMediaSessionConnector.setPlayer(null, null);
        mPlayerNotificationManager.setPlayer(null);
        mSimpleExoPlayer.release();
        mSimpleExoPlayer = null;

        // Create intent to clear widget.
        Intent widgetIntent = new Intent(mContext, PodcastPlayerWidgetService.class);
        widgetIntent.setAction(ACTION_PODCAST_PLAYER_WIDGET);
        mContext.startService(widgetIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mAudioPlayerServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d(TAG, "onRebind");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, String.format("onStartCommand - intent: %s, flags:%d, startId:%d", intent, flags, startId));
        sStartId = startId;
        if (intent == null) {
            return START_STICKY;
        }

        mContext = this;
        mBundle = intent.getExtras();
        mEpisodeBitmap = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.web_hi_res_512)).getBitmap();

        mEpisodeId = mBundle.getLong(INTENT_KEY_EPISODE_ID);
        mEpisodeTitle = mBundle.getString(INTENT_KEY_EPISODE_TITLE);
        mEpisodeDescription = mBundle.getString(INTENT_KEY_EPISODE_DESCRIPTION);
        String episodeUrl = mBundle.getString(INTENT_KEY_EPISODE_URL);
        mEpisodeImageUrl = mBundle.getString(INTENT_KEY_EPISODE_IMAGE_URL);

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(getString(R.string.podcast_checksum_prefs), Context.MODE_PRIVATE);
        mCurrentPosition = sharedPreferences.getLong(String.format("%s-%s", mEpisodeId, PREFS_KEY_CURRENT_POSITION), -1L);

        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());
        mSimpleExoPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady
                        && playbackState == Player.STATE_READY
                        && mCurrentPosition != -1L
                        && mCurrentPosition != C.TIME_UNSET) {
                    mSimpleExoPlayer.seekTo(mCurrentPosition);
                    mCurrentPosition = -1L;
                }
            }
        });

        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(episodeUrl), new DefaultDataSourceFactory(
                mContext, Util.getUserAgent(mContext, getString(R.string.app_name))), new DefaultExtractorsFactory(), null, null);

        mSimpleExoPlayer.prepare(mediaSource);
        mSimpleExoPlayer.setPlayWhenReady(true);

        mPlayerNotificationManager = PlayerNotificationManager.createWithNotificationChannel(
                mContext,
                PLAYBACK_CHANNEL_ID,
                R.string.playback_channel_name,
                PLAYBACK_NOTIFICATION_ID,
                new MediaDescriptionAdapter() {

                    @Override
                    public String getCurrentContentTitle(Player player) {
                        return mEpisodeTitle;
                    }

                    @Nullable
                    @Override
                    public PendingIntent createCurrentContentIntent(Player player) {
                        Intent intent = new Intent(mContext, PodcastDetailActivity.class);
                        intent.putExtras(mBundle);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setAction(mEpisodeTitle);
                        return PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    @Override
                    public String getCurrentContentText(Player player) {
                        return Html.fromHtml(mEpisodeDescription).toString();
                    }

                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        Picasso.get()
                                .load(mEpisodeImageUrl)
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        Log.d(TAG, String.format("onBitmapLoaded - bitmap:%s, from:%s", bitmap.toString(), from.toString()));
                                        callback.onBitmap(bitmap);
                                        mEpisodeBitmap = bitmap;
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                        Log.e(TAG, String.format("onBitmapFailed - e:%s", e.getMessage()));
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                        return mEpisodeBitmap;
                    }

                }
        );
        mPlayerNotificationManager.setNotificationListener(new NotificationListener() {
            @Override
            public void onNotificationStarted(int notificationId, Notification notification) {
                Log.d(TAG, String.format("onNotificationStarted - notificationId: %d", notificationId));
                startForeground(notificationId, notification);
            }

            @Override
            public void onNotificationCancelled(int notificationId) {
                Log.d(TAG, String.format("onNotificationCancelled - notificationId: %d", notificationId));
                stopSelf(sStartId);
            }
        });
        mPlayerNotificationManager.setPlayer(mSimpleExoPlayer);

        mMediaSession = new MediaSessionCompat(mContext, MEDIA_SESSION_TAG);
        mMediaSession.setActive(true);
        mPlayerNotificationManager.setMediaSessionToken(mMediaSession.getSessionToken());

        mMediaSessionConnector = new MediaSessionConnector(mMediaSession);
        mMediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mMediaSession) {
            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                Bundle extras = new Bundle();
                extras.putParcelable(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, mEpisodeBitmap);
                extras.putParcelable(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, mEpisodeBitmap);
                return new MediaDescriptionCompat.Builder()
                        .setMediaId(mEpisodeTitle)
                        .setIconBitmap(mEpisodeBitmap)
                        .setTitle(mEpisodeTitle)
                        .setDescription(mEpisodeDescription)
                        .setExtras(extras)
                        .build();
            }
        });
        mMediaSessionConnector.setPlayer(mSimpleExoPlayer, null);
        return START_STICKY;
    }

    public class AudioPlayerServiceBinder extends Binder {
        public SimpleExoPlayer getSimpleExoPlayer() {
            return mSimpleExoPlayer;
        }
    }
}