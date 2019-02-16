package com.udacity.podkis;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_IMAGE_URL;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_TITLE;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_EPISODE_ID;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_IS_DUAL_PANE;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_PREVIOUS_EPISODE_ID;

public class PodcastDetailActivity extends AppCompatActivity implements PodcastDetailFragment.OnEpisodeSelectedListener, EpisodeDetailFragment.OnPodcastEpisodeBackSelectedListener {

    private static final String TAG = PodcastDetailActivity.class.getSimpleName();

    private static boolean sIsDualPane;
    private static Long sEpisodeId;
    private static Long sPreviousEpisodeId;

    private String mPodcastTitle;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private ImageView mPodcastDetailImageView;
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

        sIsDualPane = findViewById(R.id.episode_detail_fragment_container) != null;

        Bundle bundle = getIntent().getExtras();
        mPodcastTitle = bundle.getString(INTENT_KEY_PODCAST_TITLE, getString(R.string.app_name));
        String imageUrl = bundle.getString(INTENT_KEY_PODCAST_IMAGE_URL);

        if (sIsDualPane) {
            mCollapsingToolbarLayout = findViewById(R.id.detail_collapsing_toolbar_layout);
            mCollapsingToolbarLayout.setTitle(mPodcastTitle);
            mToolbar = findViewById(R.id.detail_toolbar);
            mToolbar.setNavigationOnClickListener(v -> onBackPressed());
            mPodcastDetailImageView = findViewById(R.id.podcast_detail_image);

            if (imageUrl != null
                    && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .noFade()
                        .placeholder(ContextCompat.getDrawable(this, R.drawable.web_hi_res_512_square))
                        .into(mPodcastDetailImageView, new Callback() {
                            @Override
                            public void onSuccess() {
                                supportStartPostponedEnterTransition();
                            }

                            @Override
                            public void onError(Exception e) {
                                supportStartPostponedEnterTransition();
                            }
                        });
            } else {
                mPodcastDetailImageView.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_launcher_square));
            }

            if (savedInstanceState == null) {
                PodcastDetailFragment podcastDetailFragment = new PodcastDetailFragment();
                podcastDetailFragment.setOnEpisodeSelectedListener(this);
                Intent intent = new Intent();
                intent.putExtras(getIntent().getExtras());
                intent.putExtra(INTENT_KEY_IS_DUAL_PANE, sIsDualPane);
                podcastDetailFragment.setArguments(intent.getExtras());
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.podcast_detail_fragment_container, podcastDetailFragment);

                EpisodeDetailFragment episodeDetailFragment = new EpisodeDetailFragment();
                episodeDetailFragment.setArguments(intent.getExtras());
                episodeDetailFragment.setOnPodcastEpisodeBackSelectedListener(this);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.add(R.id.episode_detail_fragment_container, episodeDetailFragment);
                fragmentTransaction.commit();

            } else {
                for (Fragment fragment : mFragmentManager.getFragments()) {
                    if (fragment instanceof PodcastDetailFragment) {
                        ((PodcastDetailFragment) fragment).setOnEpisodeSelectedListener(this);
                    }
                }
            }
        } else {
            if (savedInstanceState == null) {
                PodcastDetailFragment podcastDetailFragment = new PodcastDetailFragment();
                podcastDetailFragment.setOnEpisodeSelectedListener(this);
                podcastDetailFragment.setArguments(getIntent().getExtras());
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.podcast_detail_fragment_container, podcastDetailFragment);
                fragmentTransaction.commit();
            } else {
                for (Fragment fragment : mFragmentManager.getFragments()) {
                    if (fragment instanceof PodcastDetailFragment) {
                        ((PodcastDetailFragment) fragment).setOnEpisodeSelectedListener(this);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        if (!sIsDualPane
                && mFragmentManager.getBackStackEntryCount() > 1) {
            popEpisodeDetailFragments();
        } else {
            finish();
        }
    }

    @Override
    public void onEpisodeSelected(Long id) {
        Log.d(TAG, String.format("onEpisodeSelected - id:%d", id));
        if (sEpisodeId != null) {
            sPreviousEpisodeId = sEpisodeId;
        }
        sEpisodeId = id;
        commitEpisodeDetailFragment();
    }

    private void commitEpisodeDetailFragment() {
        EpisodeDetailFragment episodeDetailFragment = new EpisodeDetailFragment();
        episodeDetailFragment.setOnPodcastEpisodeBackSelectedListener(this);
        Intent intent = new Intent();
        intent.putExtra(INTENT_KEY_EPISODE_ID, sEpisodeId);
        intent.putExtra(INTENT_KEY_PREVIOUS_EPISODE_ID, sPreviousEpisodeId);
        intent.putExtra(INTENT_KEY_IS_DUAL_PANE, sIsDualPane);
        episodeDetailFragment.setArguments(intent.getExtras());
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        if (sIsDualPane) {
            fragmentTransaction.replace(R.id.episode_detail_fragment_container, episodeDetailFragment);
        } else {
            fragmentTransaction.replace(R.id.podcast_detail_fragment_container, episodeDetailFragment);
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onPodcastEpisodeBackSelected() {
        mFragmentManager.popBackStackImmediate();
    }

    private void popEpisodeDetailFragments() {
        while (mFragmentManager.getBackStackEntryCount() > 1) {
            mFragmentManager.popBackStackImmediate();
        }
    }
}
