package com.udacity.podkis;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;

import com.udacity.podkis.entity.Podcast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PodcastAdapter.PodcastClickHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    protected static final String INTENT_KEY_PODCAST_ID = "podcast_id";
    protected static final String INTENT_KEY_PODCAST_TITLE = "podcast_title";
    protected static final String INTENT_KEY_PODCAST_DESCRIPTION = "podcast_description";
    protected static final String INTENT_KEY_PODCAST_IMAGE_URL = "podcast_image_url";
    protected static final String INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME = "podcast_image_transition_name";

    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private PodcastAdapter mPodcastAdapter;

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

        List<Podcast> podcastList = new ArrayList<>();
        String testTitle = getString(R.string.test_podcast_title);
        String testDescription = getString(R.string.test_podcast_description);
        String testImageUrl = "https://content.production.cdn.art19.com/images/07/9d/d2/a3/079dd2a3-e834-4f94-b143-7cda92ee6173/e01eb3c25bd67e61fe22b9ecf609ffa58438f7a079216d7b27f99bf478f3b3d5d6e59192efeb0fef574c20ebb7b49539231a1475d4ce6554780eace09dbd026b.jpeg";
        for (int i = 0; i < 12; i++) {
            Podcast podcast = new Podcast();
            podcast.id = (long) i + 1;
            podcast.imageUrl = testImageUrl;
            podcast.title = testTitle;
            podcast.description = testDescription;
            podcastList.add(podcast);
        }

        mPodcastAdapter.swapPodcasts(podcastList);
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
