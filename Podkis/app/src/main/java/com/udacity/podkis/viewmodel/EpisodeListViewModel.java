package com.udacity.podkis.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;

import com.udacity.podkis.entity.Episode;
import com.udacity.podkis.repository.PodkisRepository;

import java.util.List;

public class EpisodeListViewModel extends ViewModel {

    private MutableLiveData<List<Episode>> mEpisodeList;

    public EpisodeListViewModel(@NonNull Context context, @NonNull Long podcastId) {
        mEpisodeList = PodkisRepository.getPodcastEpisodes(context, podcastId);
    }

    public MutableLiveData<List<Episode>> getEpisodeList() {
        return mEpisodeList;
    }
}
