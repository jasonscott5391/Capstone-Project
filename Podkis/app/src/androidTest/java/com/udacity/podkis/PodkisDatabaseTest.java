package com.udacity.podkis;


import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.udacity.podkis.data.PodkisDatabase;
import com.udacity.podkis.entity.Episode;
import com.udacity.podkis.entity.Podcast;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class PodkisDatabaseTest {

    private static final int TEST_NUM_PODCASTS = 10;
    private static final int TEST_NUM_EPISODES = 10;

    private PodkisDatabase mPodkisDatabase;

    private List<Long> mPodcastIdList;

    @Before
    public void createDatabase() {
        mPodkisDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                PodkisDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void closeDatabase() {
        mPodkisDatabase.close();
    }

    @Test
    public void testInsertPodcastsAndEpisodes() {
        assertEquals(mPodkisDatabase.podkisDao().getPodcastCount(), 0);
        insertPodcastsAndEpisodes();
        assertEquals(mPodkisDatabase.podkisDao().getPodcastCount(), TEST_NUM_PODCASTS);
        for (Long podcastId : mPodcastIdList) {
            assertEquals(mPodkisDatabase.podkisDao().getEpisodeCount(podcastId), TEST_NUM_EPISODES);
        }
    }

    @Test
    public void testGetPodcasts() {
        insertPodcastsAndEpisodes();
        List<Podcast> podcastList = mPodkisDatabase.podkisDao().getPodcasts();
        assertNotNull(podcastList);
        assertEquals(podcastList.size(), TEST_NUM_PODCASTS);
    }

    @Test
    public void testGetPodcastEpisodes() {
        insertPodcastsAndEpisodes();
        for (Long podcastId : mPodcastIdList) {
            List<Episode> episodeList = mPodkisDatabase.podkisDao().getPodcastEpisodes(podcastId);
            assertNotNull(episodeList);
            assertEquals(episodeList.size(), TEST_NUM_EPISODES);
        }
    }

    private void insertPodcastsAndEpisodes() {
        mPodcastIdList = new ArrayList<>();
        for (int i = 0; i < TEST_NUM_PODCASTS; i++) {
            Podcast podcast = new Podcast();
            podcast.title = String.format("Test Title %d", i + 1);
            podcast.description = String.format("Test Description %d", i + 1);
            podcast.author = String.format("Test Author %d", i + 1);
            podcast.imageUrl = String.format("Test Image URL %d", i + 1);

            long podcastId = mPodkisDatabase.podkisDao().insertPodcast(podcast);
            mPodcastIdList.add(podcastId);
            List<Episode> episodeList = new ArrayList<>();
            for (int j = 0; j < TEST_NUM_EPISODES; j++) {
                Episode episode = new Episode();
                episode.podcastId = podcastId;
                episode.title = String.format("Test Title %d", j + 1);
                episode.description = String.format("Test Description %d", j + 1);
                episode.publishedDate = new Date(System.currentTimeMillis() + (j * 3600000));
                episode.seasonNumber = i + 1;
                episode.episodeNumber = j + 1;
                episode.imageUrl = String.format("Test Image URL %d", j + 1);
                episode.podcastImageUrl = String.format("Test Image URL %d", i + 1);
                episode.duration = String.format("Test Duration %d", j + 1);
                episode.url = String.format("Test URL %d", j + 1);

                episodeList.add(episode);
            }

            mPodkisDatabase.podkisDao().insertEpisodes(episodeList);
        }
    }

}
