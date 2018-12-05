package com.udacity.podkis.sync;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

public class PodkisSyncUtils {

    private static final String TAG = PodkisSyncUtils.class.getSimpleName();

    public static void startImmediateSync(@NonNull final Context context) {
        Log.d(TAG, "startImmediateSync()");
        Intent intentToSyncImmediately = new Intent(context, PodkisSyncIntentService.class);
        context.startService(intentToSyncImmediately);
    }
}
