package com.udacity.podkis.data;

import android.arch.persistence.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PodkisTypeConverters {

    private static final String DATE_PATTERN = "EEE, dd MMM yyyy";
    private static DateFormat sDateFormat = new SimpleDateFormat(DATE_PATTERN);

    @TypeConverter
    public static Date dateFromString(String dateString) {
        try {
            return sDateFormat.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    @TypeConverter
    public static String dateToString(Date date) {
        return sDateFormat.format(date);
    }
}
