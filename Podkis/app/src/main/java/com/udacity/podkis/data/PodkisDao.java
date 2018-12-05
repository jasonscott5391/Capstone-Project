package com.udacity.podkis.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.google.android.exoplayer2.upstream.DataSource;
import com.udacity.podkis.entity.Podcast;
import com.udacity.podkis.entity.Episode;

import java.util.List;

@Dao
public interface PodkisDao {

    @Query("SELECT COUNT(*) FROM " + Podcast.TABLE_NAME_PODCAST)
    int getPodcastCount();

    @Query("SELECT COUNT(*) FROM " + Episode.TABLE_NAME_EPISODE
            + " WHERE " + Episode.COLUMN_EPISODE_PODCAST_ID
            + " = :podcastId")
    int getEpisodeCount(long podcastId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPodcast(Podcast podcast);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertEpisodes(List<Episode> episodeList);

    @Query("SELECT * FROM " + Podcast.TABLE_NAME_PODCAST)
    List<Podcast> getPodcasts();

    @Query("SELECT * FROM " + Episode.TABLE_NAME_EPISODE
            + " WHERE " + Episode.COLUMN_EPISODE_PODCAST_ID
            + " = :podcastId")
    List<Episode> getPodcastEpisodes(long podcastId);

    @Query("SELECT * FROM " + Episode.TABLE_NAME_EPISODE
            + " WHERE " + Episode.COLUMN_EPISODE_ID
            + " = :episodeId")
    Episode getPodcastEpisode(long episodeId);
}
