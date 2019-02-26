package com.udacity.podkis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_DESCRIPTION;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_ID;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_IMAGE_URL;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_TITLE;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_EPISODE_ID;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_IS_DUAL_PANE;
import static com.udacity.podkis.PodcastDetailFragment.INTENT_KEY_PREVIOUS_EPISODE_ID;
import static com.udacity.podkis.service.PodcastPlayerService.INTENT_KEY_EPISODE_TITLE;

public class PodcastDetailActivity extends AppCompatActivity implements PodcastDetailFragment.OnEpisodeSelectedListener, EpisodeDetailFragment.OnPodcastEpisodeBackSelectedListener {

    private static final String TAG = PodcastDetailActivity.class.getSimpleName();

    private static boolean sReturning = false;
    private static boolean sIsDualPane;
    private static Long sEpisodeId;
    private static Long sPreviousEpisodeId;
    private static String sEpisodeTitle;

    private Long mPodcastId;
    private String mPodcastTitle;
    private String mPodcastDescription;
    private String mImageTransitionName;
    private String mPodcastImageUrl;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private ImageView mPodcastDetailImageView;
    private FragmentManager mFragmentManager;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        setContentView(R.layout.activity_podcast_detail);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mFragmentManager = getSupportFragmentManager();

        sIsDualPane = findViewById(R.id.episode_detail_fragment_container) != null;

        Intent callingIntent = getIntent();
        mPodcastId = callingIntent.getLongExtra(INTENT_KEY_PODCAST_ID, -1L);
        mPodcastTitle = callingIntent.getStringExtra(INTENT_KEY_PODCAST_TITLE);
        mPodcastDescription = callingIntent.getStringExtra(INTENT_KEY_PODCAST_DESCRIPTION);
        mImageTransitionName = callingIntent.getStringExtra(INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME);
        mPodcastImageUrl = callingIntent.getStringExtra(INTENT_KEY_PODCAST_IMAGE_URL);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.podkis_shared_prefs), Context.MODE_PRIVATE);

        if (mPodcastId == null
                || mPodcastId == -1L) {
            mPodcastId = sharedPreferences.getLong(INTENT_KEY_PODCAST_ID, -1L);
            mPodcastTitle = sharedPreferences.getString(INTENT_KEY_PODCAST_TITLE, null);
            mPodcastDescription = sharedPreferences.getString(INTENT_KEY_PODCAST_DESCRIPTION, null);
            mImageTransitionName = sharedPreferences.getString(INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME, null);
            mPodcastImageUrl = sharedPreferences.getString(INTENT_KEY_PODCAST_IMAGE_URL, null);
            sEpisodeId = sharedPreferences.getLong(INTENT_KEY_EPISODE_ID, -1L);
            sEpisodeTitle = sharedPreferences.getString(INTENT_KEY_EPISODE_TITLE, null);
            sPreviousEpisodeId = sharedPreferences.getLong(INTENT_KEY_PREVIOUS_EPISODE_ID, -1L);
            sReturning = true;
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(INTENT_KEY_PODCAST_ID, mPodcastId);
            editor.putString(INTENT_KEY_PODCAST_TITLE, mPodcastTitle);
            editor.putString(INTENT_KEY_PODCAST_DESCRIPTION, mPodcastDescription);
            editor.putString(INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME, mImageTransitionName);
            editor.putString(INTENT_KEY_PODCAST_IMAGE_URL, mPodcastImageUrl);
            editor.apply();
        }


        if (sIsDualPane) {
            mCollapsingToolbarLayout = findViewById(R.id.detail_collapsing_toolbar_layout);
            mCollapsingToolbarLayout.setTitle(mPodcastTitle);
            mToolbar = findViewById(R.id.detail_toolbar);
            mToolbar.setNavigationOnClickListener(v -> onBackPressed());
            mPodcastDetailImageView = findViewById(R.id.podcast_detail_image);

            if (mPodcastImageUrl != null
                    && !mPodcastImageUrl.isEmpty()) {
                Picasso.get()
                        .load(mPodcastImageUrl)
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
                Intent intent = createIntent();
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
                Intent intent = createIntent();
                podcastDetailFragment.setArguments(intent.getExtras());
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
    protected void onResume() {
        super.onResume();

        if (sReturning) {
            commitEpisodeDetailFragment();
            sReturning = false;
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
    public void onEpisodeSelected(Long id, String title) {
        Log.d(TAG, String.format("onEpisodeSelected - id:%d, title:%s", id, title));
        if (sEpisodeId != null) {
            sPreviousEpisodeId = sEpisodeId;
        }
        sEpisodeId = id;
        sEpisodeTitle = title;
        commitEpisodeDetailFragment();
    }

    private void commitEpisodeDetailFragment() {
        EpisodeDetailFragment episodeDetailFragment = new EpisodeDetailFragment();
        episodeDetailFragment.setOnPodcastEpisodeBackSelectedListener(this);
        Intent intent = createIntent();
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

    private Intent createIntent() {
        Intent intent = new Intent();
        intent.putExtra(INTENT_KEY_PODCAST_ID, mPodcastId);
        intent.putExtra(INTENT_KEY_PODCAST_TITLE, mPodcastTitle);
        intent.putExtra(INTENT_KEY_PODCAST_DESCRIPTION, mPodcastDescription);
        intent.putExtra(INTENT_KEY_EPISODE_ID, sEpisodeId);
        intent.putExtra(INTENT_KEY_PREVIOUS_EPISODE_ID, sPreviousEpisodeId);
        intent.putExtra(INTENT_KEY_IS_DUAL_PANE, sIsDualPane);
        intent.putExtra(INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME, mImageTransitionName);
        intent.putExtra(INTENT_KEY_PODCAST_IMAGE_URL, mPodcastImageUrl);

        if (!sReturning) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.format("%s_%s", mPodcastTitle, sEpisodeTitle));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.podkis_shared_prefs), Context.MODE_PRIVATE).edit();
        if (sEpisodeId != null) {
            editor.putLong(INTENT_KEY_EPISODE_ID, sEpisodeId);
        }
        if (sPreviousEpisodeId != null) {
            editor.putLong(INTENT_KEY_PREVIOUS_EPISODE_ID, sPreviousEpisodeId);

        }
        editor.apply();

        return intent;
    }
}
