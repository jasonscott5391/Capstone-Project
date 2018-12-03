package com.udacity.podkis.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.provider.BaseColumns;

import static com.udacity.podkis.entity.Podcast.TABLE_NAME_PODCAST;

@Entity(tableName = TABLE_NAME_PODCAST)
public class Podcast {

    public static final String TABLE_NAME_PODCAST = "podcast";
    public static final String COLUMN_PODCAST_ID = BaseColumns._ID;
    static final String COLUMN_PODCAST_TITLE = "title";
    static final String COLUMN_PODCAST_DESCRIPTION = "description";
    static final String COLUMN_PODCAST_AUTHOR = "author";
    public static final String COLUMN_PODCAST_IMAGE_URL = "image_url";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_PODCAST_ID)
    public Long id;

    @ColumnInfo(name = COLUMN_PODCAST_TITLE)
    public String title;

    @ColumnInfo(name = COLUMN_PODCAST_DESCRIPTION)
    public String description;

    @ColumnInfo(name = COLUMN_PODCAST_AUTHOR)
    public String author;

    @ColumnInfo(name = COLUMN_PODCAST_IMAGE_URL)
    public String imageUrl;

}
