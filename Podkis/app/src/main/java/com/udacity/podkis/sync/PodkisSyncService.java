package com.udacity.podkis.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Process;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.udacity.podkis.R;
import com.udacity.podkis.data.PodkisDao;
import com.udacity.podkis.data.PodkisDatabase;
import com.udacity.podkis.repository.PodkisRepository;

public class PodkisSyncService extends Service {

    private static final String TAG = PodkisSyncService.class.getSimpleName();
    protected static final String INTENT_KEY_PODCAST = "podcast_key";

    private Looper mServiceLooper;
    private PodkisSynceServiceHandler mPodkisSynceServiceHandler;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        HandlerThread handlerThread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        mServiceLooper = handlerThread.getLooper();
        mPodkisSynceServiceHandler = new PodkisSynceServiceHandler(this, mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, String.format("onStartCommand - intent:%s, flags: %d, startId:%d", intent.toString(), flags, startId));
        Message message = mPodkisSynceServiceHandler.obtainMessage();
        message.arg1 = startId;
        message.obj = intent;
        mPodkisSynceServiceHandler.sendMessage(message);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Don't bind, return null.
        return null;
    }

    private final class PodkisSynceServiceHandler extends Handler {
        private Context mContext;

        PodkisSynceServiceHandler(Context context, Looper looper) {
            super(looper);
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, String.format("handleMessage - msg:%s", msg.toString()));

            Intent intent = (Intent) msg.obj;

            ConnectivityManager cm =
                    (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = null;
            if (cm != null) {
                activeNetwork = cm.getActiveNetworkInfo();
            }

            PodkisDatabase podkisDatabase = PodkisDatabase.getInstance(mContext);
            PodkisDao podkisDao = podkisDatabase.podkisDao();

            if (activeNetwork == null ||
                    !activeNetwork.isConnectedOrConnecting()) {
                PodkisRepository.getPodcastList().postValue(podkisDao.getPodcasts());
                Log.d(TAG, "onHandleIntent - Network is not connected!");
                return;
            }

            SharedPreferences sharedPreferences = mContext.getSharedPreferences(getString(R.string.podcast_checksum_prefs), Context.MODE_PRIVATE);

            PodkisSyncTask.syncPodkis(podkisDao, sharedPreferences, intent.getStringExtra(INTENT_KEY_PODCAST));
            // Stop service.
            stopSelf(msg.arg1);
        }
    }

}
