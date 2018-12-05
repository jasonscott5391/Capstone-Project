package com.udacity.podkis.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

public class EpisodeListViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Context mContext;
    private final Long mPodcastId;
    public final int mSize;

    public EpisodeListViewModelFactory(@NonNull final Context context, @NonNull Long podcastId, @NonNull int size) {
        this.mContext = context;
        this.mPodcastId = podcastId;
        this.mSize = size;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EpisodeListViewModel(mContext, mPodcastId, mSize);
    }
}
