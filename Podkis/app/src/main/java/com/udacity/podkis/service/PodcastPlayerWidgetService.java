package com.udacity.podkis.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.udacity.podkis.provider.PodcastPlayerWidgetProvider;

import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_TITLE;
import static com.udacity.podkis.service.PodcastPlayerService.INTENT_KEY_EPISODE_IMAGE_URL;
import static com.udacity.podkis.service.PodcastPlayerService.INTENT_KEY_EPISODE_TITLE;

public class PodcastPlayerWidgetService extends IntentService {

    private static final String TAG = PodcastPlayerWidgetService.class.getSimpleName();

    public static final String ACTION_PODCAST_PLAYER_WIDGET = "action_podcast_player_widget";

    public PodcastPlayerWidgetService() {
        super(TAG);
    }

    public PodcastPlayerWidgetService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, String.format("onHandleIntent - intent: %s", intent.toString()));

        if (intent.getAction() != null
                && intent.getAction().equalsIgnoreCase(ACTION_PODCAST_PLAYER_WIDGET)) {

            String podcastTitle = intent.getStringExtra(INTENT_KEY_PODCAST_TITLE);
            String episodeTitle = intent.getStringExtra(INTENT_KEY_EPISODE_TITLE);
            String episodeImageUrl = intent.getStringExtra(INTENT_KEY_EPISODE_IMAGE_URL);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, PodcastPlayerWidgetProvider.class));
            PodcastPlayerWidgetProvider.updatePodcastPlayerWidget(this,
                    appWidgetManager,
                    appWidgetIds,
                    podcastTitle,
                    episodeTitle,
                    episodeImageUrl);
        }

    }
}
