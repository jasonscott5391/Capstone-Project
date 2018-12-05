package com.udacity.podkis.repository;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.podkis.data.PodkisDatabase;
import com.udacity.podkis.entity.Episode;
import com.udacity.podkis.entity.Podcast;
import com.udacity.podkis.sync.PodkisSyncUtils;

import java.util.List;

public class PodkisRepository {

    private static final String TAG = PodkisRepository.class.getSimpleName();

    private static boolean sInitialized;

    private static MutableLiveData<List<Podcast>> sPodcastList;
    private static MutableLiveData<List<Episode>> sEpisodeList;
    private static MutableLiveData<Episode> sEpisode;


    public static void init(@NonNull final Context context) {
        Log.d(TAG, "init");
        if (sInitialized) {
            return;
        }

        sInitialized = true;
        sPodcastList = new MutableLiveData<>();
        sEpisodeList = new MutableLiveData<>();
        sEpisode = new MutableLiveData<>();

        PodkisSyncUtils.startImmediateSync(context);
    }

    public static MutableLiveData<List<Podcast>> getPodcastList() {
        return sPodcastList;
    }

    public static void updatePodcasts(@NonNull final Context context) {
        Log.d(TAG, "updatePodcasts");
        PodkisSyncUtils.startImmediateSync(context);
    }

    public static MutableLiveData<List<Episode>> getPodcastEpisodes(@NonNull final Context context, @NonNull Long podcastId) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                PodkisDatabase podkisDatabase = PodkisDatabase.getInstance(context);
                sEpisodeList.postValue(podkisDatabase.podkisDao().getPodcastEpisodes(podcastId));
                return null;
            }
        }.execute();

        return sEpisodeList;
    }

    public static MutableLiveData<Episode> getPodcastEpisode(@NonNull final Context context, @NonNull Long episodeId) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                PodkisDatabase podkisDatabase = PodkisDatabase.getInstance(context);
                sEpisode.postValue(podkisDatabase.podkisDao().getPodcastEpisode(episodeId));
                return null;
            }
        }.execute();

        return sEpisode;
    }
}
