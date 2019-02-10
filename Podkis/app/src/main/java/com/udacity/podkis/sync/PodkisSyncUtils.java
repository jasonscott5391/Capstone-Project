package com.udacity.podkis.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.podkis.R;

import static com.udacity.podkis.sync.PodkisSyncService.INTENT_KEY_PODCAST;

public class PodkisSyncUtils {

    private static final String TAG = PodkisSyncUtils.class.getSimpleName();

    public static void startImmediateSync(@NonNull final Context context) {
        Log.d(TAG, "startImmediateSync()");
        String[] podcastList = context.getResources().getStringArray(R.array.podcast_list);
        for (String podcast : podcastList) {
            Intent intentToSyncImmediately = new Intent(context, PodkisSyncService.class);
            intentToSyncImmediately.putExtra(INTENT_KEY_PODCAST, podcast);
            context.startService(intentToSyncImmediately);
        }
    }
}
