package com.udacity.podkis;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_EPISODE_ID;

public class PodcastDetailActivity extends AppCompatActivity implements PodcastDetailFragment.OnEpisodeSelectedListener, EpisodeDetailFragment.OnPodcastEpisodeBackSelectedListener {

    private static final String TAG = PodcastDetailActivity.class.getSimpleName();

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        setContentView(R.layout.activity_podcast_detail);

        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            PodcastDetailFragment podcastDetailFragment = new PodcastDetailFragment();
            podcastDetailFragment.setOnEpisodeSelectedListener(this);
            podcastDetailFragment.setArguments(getIntent().getExtras());
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.podcast_detail_fragment_container, podcastDetailFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onEpisodeSelected(Long id) {
        Log.d(TAG, String.format("onEpisodeSelected - id:%d", id));
        commitEpisodeDetailFragment(id);
    }

    private void commitEpisodeDetailFragment(Long recipeId) {
        EpisodeDetailFragment episodeDetailFragment = new EpisodeDetailFragment();
        episodeDetailFragment.setOnPodcastEpisodeBackSelectedListener(this);
        Intent intent = new Intent();
        intent.putExtra(INTENT_KEY_EPISODE_ID, recipeId);
        episodeDetailFragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.podcast_detail_fragment_container, episodeDetailFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onPodcastEpisodeBackSelected() {
        mFragmentManager.popBackStackImmediate();
    }
}
