package com.udacity.podkis.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;

import com.udacity.podkis.entity.Episode;
import com.udacity.podkis.repository.PodkisRepository;

public class EpisodeViewModel extends ViewModel {

    private MutableLiveData<Episode> mEpisode;

    public EpisodeViewModel(@NonNull Context context, @NonNull Long episodeId) {
        mEpisode = PodkisRepository.getPodcastEpisode(context, episodeId);
    }

    public MutableLiveData<Episode> getEpisode() {
        return mEpisode;
    }
}
