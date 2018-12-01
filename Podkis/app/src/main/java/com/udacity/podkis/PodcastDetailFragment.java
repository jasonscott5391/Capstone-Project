package com.udacity.podkis;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.udacity.podkis.entity.Episode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_DESCRIPTION;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_ID;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_IMAGE_URL;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_TITLE;


public class PodcastDetailFragment extends Fragment implements EpisodeAdapter.EpisodeClickHandler {

    protected static final String TAG = PodcastDetailFragment.class.getSimpleName();
    protected static final String INTENT_KEY_EPISODE_ID = "episode_id";

    private OnEpisodeSelectedListener mOnEpisodeSelectedListener;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private TextView mPodcastEpisodeDescription;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EpisodeAdapter mEpisodeAdapter;
    private ImageView mPodcastDetailImageView;

    private Long mPodcastId;
    private String mPodcastTitle;
    private String mPodcastDescription;

    public PodcastDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_podcast_detail, container, false);

        getActivity().supportPostponeEnterTransition();

        Context context = getContext();

        mCollapsingToolbarLayout = view.findViewById(R.id.detail_collapsing_toolbar_layout);
        mToolbar = view.findViewById(R.id.detail_toolbar);
        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        mPodcastEpisodeDescription = view.findViewById(R.id.podcast_episode_description);
        mPodcastDetailImageView = view.findViewById(R.id.podcast_detail_image);
        mRecyclerView = view.findViewById(R.id.episode_recycler_view);

        mLinearLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mEpisodeAdapter = new EpisodeAdapter(getContext(), this, new ArrayList<>());
        mRecyclerView.setAdapter(mEpisodeAdapter);

        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = getActivity().getIntent().getExtras();
        }

        mPodcastId = bundle.getLong(INTENT_KEY_PODCAST_ID, -1L);
        mPodcastTitle = bundle.getString(INTENT_KEY_PODCAST_TITLE, getString(R.string.app_name));
        mPodcastDescription = bundle.getString(INTENT_KEY_PODCAST_DESCRIPTION, "");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = bundle.getString(INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME, "");
            mPodcastDetailImageView.setTransitionName(imageTransitionName);
        }

        mCollapsingToolbarLayout.setTitle(mPodcastTitle);
        mPodcastEpisodeDescription.setText(mPodcastDescription);

        String imageUrl = bundle.getString(INTENT_KEY_PODCAST_IMAGE_URL);
        if (imageUrl != null
                && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).noFade().into(mPodcastDetailImageView, new Callback() {
                @Override
                public void onSuccess() {
                    getActivity().supportStartPostponedEnterTransition();
                }

                @Override
                public void onError(Exception e) {
                    getActivity().supportStartPostponedEnterTransition();
                }
            });
        } else {
            mPodcastDetailImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher_square));
        }

        List<Episode> episodeList = new ArrayList<>();
        String testImageUrl = "https://content.production.cdn.art19.com/images/8b/14/c0/af/8b14c0af-828c-4a64-9625-b164ace2fcae/67e5066ddc647cfbd4a4afd089e40d16d896c5ead04ee394d6e893ebca15b8250ba009c2dfde7ebcf5929ae62f9648bafc85cb4b32fd2e008e38d66587acc742.jpeg";
        for (int i = 0; i < 9; i++) {
            Episode episode = new Episode();
            episode.id = (long) i + 1;
            episode.title = getString(R.string.test_episode_title);
            episode.seasonNumber = Integer.valueOf(getString(R.string.test_episode_season_number).replace("Season ", ""));
            episode.episodeNumber = Integer.valueOf(getString(R.string.test_episode_number).replace("Episode ", ""));
            try {
                episode.publishedDate = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).parse(getString(R.string.test_episode_published_date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            episode.imageUrl = testImageUrl;

            episodeList.add(episode);
        }

        mEpisodeAdapter.swapEpisodes(episodeList);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClickEpisode(Long id) {
        Log.d(TAG, String.format("onClickEpisode - id:%d", id));
        mOnEpisodeSelectedListener.onEpisodeSelected(id);
    }

    public interface OnEpisodeSelectedListener {
        void onEpisodeSelected(Long id);
    }

    public void setOnEpisodeSelectedListener(OnEpisodeSelectedListener onEpisodeSelectedListener) {
        this.mOnEpisodeSelectedListener = onEpisodeSelectedListener;
    }
}
