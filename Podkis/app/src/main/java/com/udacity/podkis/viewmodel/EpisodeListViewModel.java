package com.udacity.podkis.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.support.annotation.NonNull;

import com.udacity.podkis.data.PodkisDatabase;
import com.udacity.podkis.entity.Episode;

import java.util.concurrent.Executors;

public class EpisodeListViewModel extends ViewModel {

    private LiveData<PagedList<Episode>> mEpisodeList;

    public EpisodeListViewModel(@NonNull Context context, @NonNull Long podcastId, @NonNull int size) {
        PodkisDatabase podkisDatabase = PodkisDatabase.getInstance(context);
        mEpisodeList = new LivePagedListBuilder<>(podkisDatabase.podkisDao().getPodcastEpisodes(podcastId), size)
                .setFetchExecutor(Executors.newFixedThreadPool(size))
                .build();
    }

    public LiveData<PagedList<Episode>> getEpisodeList() {
        return mEpisodeList;
    }
}
