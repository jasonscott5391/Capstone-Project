package com.udacity.podkis.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

public class EpisodeViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Context mContext;
    private final Long mEpisodeId;

    public EpisodeViewModelFactory(@NonNull final Context context, @NonNull Long episodeId) {
        this.mContext = context;
        this.mEpisodeId = episodeId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EpisodeViewModel(mContext, mEpisodeId);
    }
}
