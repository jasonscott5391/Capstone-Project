package com.udacity.podkis;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.udacity.podkis.viewmodel.EpisodeListViewModel;
import com.udacity.podkis.viewmodel.EpisodeListViewModelFactory;

import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_DESCRIPTION;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_ID;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_IMAGE_URL;
import static com.udacity.podkis.MainActivity.INTENT_KEY_PODCAST_TITLE;


public class PodcastDetailFragment extends Fragment implements EpisodeAdapter.EpisodeClickHandler {

    protected static final String TAG = PodcastDetailFragment.class.getSimpleName();
    protected static final String INTENT_KEY_EPISODE_ID = "episode_id";
    protected static final String INTENT_KEY_IS_DUAL_PANE = "is_dual_pane";

    private static boolean sIsDualPane;

    private OnEpisodeSelectedListener mOnEpisodeSelectedListener;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private TextView mPodcastEpisodeDescription;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EpisodeAdapter mEpisodeAdapter;
    private ImageView mPodcastDetailImageView;
    private EpisodeListViewModel mEpisodeListViewModel;

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

        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = getActivity().getIntent().getExtras();
        }

        sIsDualPane = bundle.getBoolean(INTENT_KEY_IS_DUAL_PANE, false);
        mPodcastId = bundle.getLong(INTENT_KEY_PODCAST_ID, -1L);
        mPodcastTitle = bundle.getString(INTENT_KEY_PODCAST_TITLE, getString(R.string.app_name));
        mPodcastDescription = bundle.getString(INTENT_KEY_PODCAST_DESCRIPTION, "");

        if (!sIsDualPane) {
            mCollapsingToolbarLayout = view.findViewById(R.id.detail_collapsing_toolbar_layout);
            mCollapsingToolbarLayout.setTitle(mPodcastTitle);
            mToolbar = view.findViewById(R.id.detail_toolbar);
            mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
            mPodcastDetailImageView = view.findViewById(R.id.podcast_detail_image);
        }

        mPodcastEpisodeDescription = view.findViewById(R.id.podcast_episode_description);
        mRecyclerView = view.findViewById(R.id.episode_recycler_view);

        mLinearLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mEpisodeAdapter = new EpisodeAdapter(context, this);

        if (!sIsDualPane
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = bundle.getString(INTENT_KEY_PODCAST_IMAGE_TRANSITION_NAME, "");
            mPodcastDetailImageView.setTransitionName(imageTransitionName);
        }

        Spanned html;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            html = Html.fromHtml(mPodcastDescription, Html.FROM_HTML_MODE_COMPACT);
        } else {
            html = Html.fromHtml(mPodcastDescription);
        }
        mPodcastEpisodeDescription.setText(html);


        String imageUrl = bundle.getString(INTENT_KEY_PODCAST_IMAGE_URL);
        mEpisodeAdapter.setPodcastImageUrl(imageUrl);
        if (!sIsDualPane
                && imageUrl != null
                && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .noFade()
                    .placeholder(ContextCompat.getDrawable(context, R.drawable.web_hi_res_512_square))
                    .into(mPodcastDetailImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            FragmentActivity fragmentActivity = getActivity();
                            if (fragmentActivity != null) {
                                fragmentActivity.supportStartPostponedEnterTransition();
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            FragmentActivity fragmentActivity = getActivity();
                            if (fragmentActivity != null) {
                                fragmentActivity.supportStartPostponedEnterTransition();
                            }
                        }
                    });
        } else if (!sIsDualPane) {
            mPodcastDetailImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.ic_launcher_square));
        }

        mEpisodeListViewModel = ViewModelProviders.of(this, new EpisodeListViewModelFactory(context, mPodcastId, getResources().getInteger(R.integer.default_page_size))).get(EpisodeListViewModel.class);
        mEpisodeListViewModel.getEpisodeList().observe(this, episodeList -> {
            Log.d(TAG, "Updating Episode List.");
            mEpisodeAdapter.submitList(episodeList);
        });
        mRecyclerView.setAdapter(mEpisodeAdapter);

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
        Log.d(TAG, String.format("onClickEpisode - id:%d ,mOnEpisodeSelectedListener:%s", id, mOnEpisodeSelectedListener));
        if (mOnEpisodeSelectedListener != null) {
            mOnEpisodeSelectedListener.onEpisodeSelected(id);
        }
    }

    public interface OnEpisodeSelectedListener {
        void onEpisodeSelected(Long id);
    }

    public void setOnEpisodeSelectedListener(OnEpisodeSelectedListener onEpisodeSelectedListener) {
        this.mOnEpisodeSelectedListener = onEpisodeSelectedListener;
    }
}
