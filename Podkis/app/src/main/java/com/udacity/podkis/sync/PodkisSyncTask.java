package com.udacity.podkis.sync;

import android.util.Log;

import com.udacity.podkis.data.PodkisDao;
import com.udacity.podkis.entity.Episode;
import com.udacity.podkis.entity.Podcast;
import com.udacity.podkis.repository.PodkisRepository;
import com.udacity.podkis.service.EpisodeWrapper;
import com.udacity.podkis.service.ImageWrapper;
import com.udacity.podkis.service.PodcastWrapper;
import com.udacity.podkis.service.PodkisService;
import com.udacity.podkis.service.RssWrapper;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class PodkisSyncTask {
    private static final String TAG = PodkisSyncTask.class.getSimpleName();
    private static final String BASE_RSS_URL = "https://rss.art19.com";
    private static final String[] PODCASTS = new String[]{
            "startalk-radio"
    };

    private static final DateFormat sDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());

    private static PodkisService sPodkisService = new Retrofit.Builder()
            .baseUrl(BASE_RSS_URL)
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(new Persister(new AnnotationStrategy())))
            .build()
            .create(PodkisService.class);

    static synchronized void syncPodkis(PodkisDao podkisDao) {
        Log.d(TAG, String.format("syncPodkis - Preparing request PODCASTS: %s", Arrays.toString(PODCASTS)));

        if (podkisDao.getPodcastCount() > 0) {
            PodkisRepository.getPodcastList().postValue(podkisDao.getPodcasts());
            return;
        }

        try {
            for (String podcastString : PODCASTS) {

                Response<RssWrapper> response = sPodkisService.getPodcastRss(podcastString).execute();
                Log.d(TAG, String.format("syncPodkis - Podcast: %s, response.code:%s, response.body:%s, response.errorBody:%s",
                        podcastString,
                        response.code(),
                        response.body(),
                        response.errorBody()));

                RssWrapper rssWrapper = response.body();
                if (rssWrapper == null) {
                    throw new Exception("rssWrapper is null!");
                }

                PodcastWrapper podcastWrapper = rssWrapper.podcastWrapper;
                if (podcastWrapper == null) {
                    throw new Exception("podcastWrapper is null!");
                }

                // Transform to and Insert Podcast first.
                Podcast podcast = new Podcast();
                podcast.title = podcastWrapper.title;
                podcast.description = podcastWrapper.description;
                podcast.author = podcastWrapper.author;
                for (ImageWrapper imageWrapper : podcastWrapper.image) {
                    String imageUrl;
                    if (imageWrapper.imageUrl != null) {
                        imageUrl = imageWrapper.imageUrl;
                    } else {
                        imageUrl = imageWrapper.url;
                    }
                    if (imageUrl != null) {
                        podcast.imageUrl = imageUrl;
                        break;
                    }
                }

                long podcastId = podkisDao.insertPodcast(podcast);
                Log.d(TAG, String.format("syncPodkis - Podcast inserted with ID %d", podcastId));

                // Generate List of Episodes, set parent ID, and insert.
                List<Episode> episodeList = new ArrayList<>();
                for (EpisodeWrapper episodeWrapper : podcastWrapper.episodeWrapperList) {
                    Episode episode = new Episode();
                    episode.podcastId = podcastId;
                    episode.title = episodeWrapper.title.get(0);
                    episode.description = episodeWrapper.description;
                    episode.seasonNumber = episodeWrapper.seasonNumber;
                    episode.episodeNumber = episodeWrapper.episodeNumber;
                    episode.publishedDate = sDateFormat.parse(episodeWrapper.publishedDate);
                    episode.duration = episodeWrapper.duration;
                    episode.url = episodeWrapper.enclosure.url;
                    String episodeImageUrl;
                    if ((episodeImageUrl = episodeWrapper.image.url) == null) {
                        episodeImageUrl = episodeWrapper.image.imageUrl;
                    }
                    episode.imageUrl = episodeImageUrl;

                    episodeList.add(episode);
                    if (episodeList.size() == 30) {
                        break;
                    }
                }

                long[] episodeIds = podkisDao.insertEpisodes(episodeList);
                Log.d(TAG, String.format("syncPodkis - Number of Episodes inserted %d", episodeIds.length));
                Log.d(TAG, String.format("syncPodkis - List of Episodes inserted with IDs %s", Arrays.toString(episodeIds)));

            }

            PodkisRepository.getPodcastList().postValue(podkisDao.getPodcasts());

        } catch (Exception e) {
            Log.e(TAG, String.format("syncPodkis - exception:%s", e.getMessage()));
            PodkisRepository.getPodcastList().postValue(null);
        }

    }
}
