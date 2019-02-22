package com.udacity.podkis.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.udacity.podkis.MainActivity;
import com.udacity.podkis.PodcastDetailActivity;
import com.udacity.podkis.R;

import java.io.IOException;

import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_TITLE;
import static com.udacity.podkis.service.PodcastPlayerService.INTENT_KEY_EPISODE_IMAGE_URL;
import static com.udacity.podkis.service.PodcastPlayerService.INTENT_KEY_EPISODE_TITLE;

public class PodcastPlayerWidgetProvider extends AppWidgetProvider {

    private static final String TAG = PodcastPlayerWidgetProvider.class.getSimpleName();
    private static boolean sInitialized = false;

    public static void updatePodcastPlayerWidget(final Context context,
                                                 AppWidgetManager appWidgetManager,
                                                 int[] appWidgetIds,
                                                 String podcastTitle,
                                                 String episodeTitle,
                                                 String episodeImageUrl) {
        Log.d(TAG, "updatePodcastPlayerWidget");

        // Construct RemoveViewsObject.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.podcast_player_widget);

        Intent intent;
        if (podcastTitle == null
                || podcastTitle.isEmpty()) {
            podcastTitle = context.getString(R.string.app_name);
            episodeTitle = context.getString(R.string.widget_default_title);
            intent = new Intent(context, MainActivity.class);
            sInitialized = false;
        } else {
            intent = new Intent(context, PodcastDetailActivity.class);
            intent.putExtra(INTENT_KEY_PODCAST_TITLE, podcastTitle);
            intent.putExtra(INTENT_KEY_EPISODE_TITLE, episodeTitle);
            intent.putExtra(INTENT_KEY_EPISODE_IMAGE_URL, episodeImageUrl);
        }

        views.setTextViewText(R.id.widget_podcast_title, podcastTitle);
        views.setTextViewText(R.id.widget_podcast_episode_title, episodeTitle);

        if (episodeImageUrl == null
                || episodeImageUrl.isEmpty()) {
            views.setImageViewResource(R.id.widget_podcast_episode_thumbnail, R.drawable.web_hi_res_512);
        } else {
            try {
                views.setImageViewBitmap(R.id.widget_podcast_episode_thumbnail, Picasso.get().load(episodeImageUrl).get());
            } catch (IOException e) {
                Log.e(TAG, String.format("updatePodcastPlayerWidget - e: %s", e.getMessage()));
                views.setImageViewResource(R.id.widget_podcast_episode_thumbnail, R.drawable.web_hi_res_512);
            }
        }

        // Set PendingIntent for when clicked.
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(episodeTitle);
        setPendingIntent(context, intent, views);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetIds, views);

    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled");
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");

        if (!sInitialized) {
            // Construct RemoveViewsObject.
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.podcast_player_widget);
            Intent intent = new Intent(context, MainActivity.class);
            setPendingIntent(context, intent, views);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetIds, views);
            sInitialized = true;
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        Log.d(TAG, "onRestored");
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "onDisabled");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted");
    }

    private static void setPendingIntent(Context context, Intent intent, RemoteViews views) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_podcast_episode_thumbnail, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_podcast_title, pendingIntent);
        views.setOnClickPendingIntent(R.id.widget_podcast_episode_title, pendingIntent);
    }
}