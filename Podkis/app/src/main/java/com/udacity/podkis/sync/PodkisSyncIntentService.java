package com.udacity.podkis.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;

import com.udacity.podkis.data.PodkisDatabase;

public class PodkisSyncIntentService extends IntentService {

    private static final String TAG = PodkisSyncIntentService.class.getSimpleName();

    public PodkisSyncIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, String.format("onHandleIntent - intent:%s", intent));
        assert intent != null;

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }

        if (activeNetwork == null ||
                !activeNetwork.isConnectedOrConnecting()) {
            return;
        }

        PodkisDatabase podkisDatabase = PodkisDatabase.getInstance(this.getApplication());
        PodkisSyncTask.syncPodkis(podkisDatabase.podkisDao());
    }
}
