package com.udacity.podkis.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.udacity.podkis.entity.Episode;
import com.udacity.podkis.entity.Podcast;

@Database(entities = {Podcast.class, Episode.class}, version = 1, exportSchema = false)
@TypeConverters(PodkisTypeConverters.class)
public abstract class PodkisDatabase extends RoomDatabase {

    public abstract PodkisDao podkisDao();

    private static final String PODKIS_DATABASE_NAME = "podkis_db";

    private static PodkisDatabase sPodkisDatabase;

    public static synchronized PodkisDatabase getInstance(Context context) {

        if (sPodkisDatabase == null) {
            sPodkisDatabase = Room.databaseBuilder(context.getApplicationContext(),
                    PodkisDatabase.class,
                    PODKIS_DATABASE_NAME)
                    .build();
        }

        return sPodkisDatabase;

    }
}
