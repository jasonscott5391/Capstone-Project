package com.udacity.podkis.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import java.util.Date;

import static com.udacity.podkis.entity.Episode.TABLE_NAME_EPISODE;
import static com.udacity.podkis.entity.Podcast.COLUMN_PODCAST_ID;

@Entity(tableName = TABLE_NAME_EPISODE)
public class Episode {

    public static final String TABLE_NAME_EPISODE = "episode";
    static final String COLUMN_EPISODE_ID = BaseColumns._ID;
    public static final String COLUMN_EPISODE_PODCAST_ID = "podcast_id";
    static final String COLUMN_EPISODE_TITLE = "title";
    static final String COLUMN_EPISODE_DESCRIPTION = "description";
    static final String COLUMN_EPISODE_NUMBER = "number";
    static final String COLUMN_EPISODE_SEASON_NUMBER = "season_number";
    static final String COLUMN_PUBLISHED_DATE = "published_date";
    static final String COLUMN_IMAGE_URL = "image_url";
    static final String COLUMN_EPISODE_DURATION = "duration";
    static final String COLUMN_EPISODE_URL = "url";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_EPISODE_ID)
    public Long id;

    @ForeignKey(entity = Podcast.class,
            parentColumns = COLUMN_PODCAST_ID,
            childColumns = COLUMN_EPISODE_PODCAST_ID)
    @ColumnInfo(index = true, name = COLUMN_EPISODE_PODCAST_ID)
    public Long podcastId;

    @ColumnInfo(name = COLUMN_EPISODE_TITLE)
    public String title;

    @ColumnInfo(name = COLUMN_EPISODE_DESCRIPTION)
    public String description;

    @ColumnInfo(name = COLUMN_EPISODE_NUMBER)
    public Integer episodeNumber;

    @ColumnInfo(name = COLUMN_EPISODE_SEASON_NUMBER)
    public Integer seasonNumber;

    @ColumnInfo(name = COLUMN_PUBLISHED_DATE)
    public Date publishedDate;

    @ColumnInfo(name = COLUMN_IMAGE_URL)
    public String imageUrl;

    @ColumnInfo(name = COLUMN_EPISODE_DURATION)
    public String duration;

    @ColumnInfo(name = COLUMN_EPISODE_URL)
    public String url;

    public String podcastImageUrl;
}
