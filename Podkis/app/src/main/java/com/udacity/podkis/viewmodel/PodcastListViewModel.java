package com.udacity.podkis.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.udacity.podkis.entity.Podcast;
import com.udacity.podkis.repository.PodkisRepository;

import java.util.List;

public class PodcastListViewModel extends AndroidViewModel {

    private static final String TAG = PodcastListViewModel.class.getSimpleName();

    private MutableLiveData<List<Podcast>> mPodcastList;

    public PodcastListViewModel(@NonNull Application application) {
        super(application);

        Log.d(TAG, "Initializing repository.");
        Context context = this.getApplication();

        PodkisRepository.init(context);

        Log.d(TAG, "Assigning podcasts from repository.");
        mPodcastList = PodkisRepository.getPodcastList();
    }

    public MutableLiveData<List<Podcast>> getPodcastList() {
        Log.d(TAG, "Retrieving podcasts.");
        return mPodcastList;
    }
}
