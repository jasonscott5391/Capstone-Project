package com.udacity.podkis.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

public class EpisodeListViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Context mContext;
    private final Long mPodcastId;

    public EpisodeListViewModelFactory(@NonNull final Context context, @NonNull Long podcastId) {
        this.mContext = context;
        this.mPodcastId = podcastId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EpisodeListViewModel(mContext, mPodcastId);
    }
}
