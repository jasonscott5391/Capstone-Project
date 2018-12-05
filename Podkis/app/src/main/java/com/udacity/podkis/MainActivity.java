package com.udacity.podkis;

import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.udacity.podkis.repository.PodkisRepository;
import com.udacity.podkis.viewmodel.PodcastListViewModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements PodcastAdapter.PodcastClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    protected static final String INTENT_KEY_PODCAST_ID = "podcast_id";
    protected static final String INTENT_KEY_PODCAST_TITLE = "podcast_title";
    protected static final String INTENT_KEY_PODCAST_DESCRIPTION = "podcast_description";
    protected static final String INTENT_KEY_PODCAST_IMAGE_URL = "podcast_image_url";
    protected static final String INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME = "podcast_image_transition_name";
    private static final String LAYOUT_CURRENT_POSITION = "layout_current_position";

    private static int sCurrentPosition = 0;

    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PodcastAdapter mPodcastAdapter;
    private PodcastListViewModel mPodcastListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.podcast_column_count));

        mRecyclerView = findViewById(R.id.podcast_recycler_view);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        mPodcastAdapter = new PodcastAdapter(this, this, new ArrayList<>());
        mRecyclerView.setAdapter(mPodcastAdapter);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        mPodcastListViewModel = ViewModelProviders.of(this).get(PodcastListViewModel.class);
        mPodcastListViewModel.getPodcastList().observe(MainActivity.this, podcastList -> {
            Log.d(TAG, "Updating Podcast List.");
            sCurrentPosition = 0;
            mPodcastAdapter.swapPodcasts(podcastList);
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe to refresh Podcasts.");
            PodkisRepository.updatePodcasts(this);
        });

        mRecyclerView.smoothScrollToPosition(sCurrentPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int currentPosition = mGridLayoutManager.findFirstVisibleItemPosition();
        sCurrentPosition = currentPosition != -1 ? currentPosition : 0;
        outState.putInt(LAYOUT_CURRENT_POSITION, sCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        sCurrentPosition = savedInstanceState.getInt(LAYOUT_CURRENT_POSITION);
    }

    @Override
    public void onClickPodcast(Long id, String title, String description, String imageUrl, ImageView sharedImageView) {
        Log.d(TAG, String.format("onClickPodcast - id:%d", id));

        Intent episodeDetailIntent = new Intent(MainActivity.this, PodcastDetailActivity.class);
        episodeDetailIntent.putExtra(INTENT_KEY_PODCAST_ID, id);
        episodeDetailIntent.putExtra(INTENT_KEY_PODCAST_TITLE, title);
        episodeDetailIntent.putExtra(INTENT_KEY_PODCAST_DESCRIPTION, description);
        episodeDetailIntent.putExtra(INTENT_KEY_PODCAST_IMAGE_URL, imageUrl);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            episodeDetailIntent.putExtra(INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(sharedImageView));
            startActivity(episodeDetailIntent,
                    ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,
                            sharedImageView,
                            ViewCompat.getTransitionName(sharedImageView)).toBundle());
        } else {
            startActivity(episodeDetailIntent);
        }
    }
}
