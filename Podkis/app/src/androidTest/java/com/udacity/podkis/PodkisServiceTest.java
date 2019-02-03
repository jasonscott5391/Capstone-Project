package com.udacity.podkis;

import android.support.test.runner.AndroidJUnit4;

import com.udacity.podkis.service.EpisodeWrapper;
import com.udacity.podkis.service.PodcastWrapper;
import com.udacity.podkis.service.PodkisService;
import com.udacity.podkis.service.RssWrapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static junit.framework.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class PodkisServiceTest {

    private static final String BASE_RSS_URL = "https://rss.art19.com";
    private static final String[] PODCASTS = new String[]{
            "startalk-radio",
            "conan-obrien",
            "the-daily"
    };

    private static PodkisService sPodkisService;

    @Before
    public void initService() {
        sPodkisService = new Retrofit.Builder()
                .baseUrl(BASE_RSS_URL)
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(new Persister(new AnnotationStrategy())))
                .build()
                .create(PodkisService.class);
    }

    @Test
    public void testGetPodcast() throws IOException {
        for (String podcast : PODCASTS) {
            Response<RssWrapper> response = sPodkisService.getPodcastRss(podcast).execute();
            RssWrapper rssWrapper = response.body();
            assertNotNull(rssWrapper);
            PodcastWrapper podcastWrapper = rssWrapper.podcastWrapper;
            assertNotNull(podcastWrapper);
            assertNotNull(podcastWrapper.title);
            assertNotNull(podcastWrapper.description);
            assertNotNull(podcastWrapper.author);
            assertNotNull(podcastWrapper.image);
            assertNotNull(podcastWrapper.episodeWrapperList);
            for (EpisodeWrapper episodeWrapper : podcastWrapper.episodeWrapperList) {
                assertNotNull(episodeWrapper.title);
                assertNotNull(episodeWrapper.description);
                assertNotNull(episodeWrapper.duration);
                assertNotNull(episodeWrapper.publishedDate);
                assertNotNull(episodeWrapper.image);
                assertNotNull(episodeWrapper.image.imageUrl);
                assertNotNull(episodeWrapper.enclosure);
                assertNotNull(episodeWrapper.enclosure.url);
            }
        }
    }
}
