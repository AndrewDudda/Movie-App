package com.example.android.movieapp.data;

import android.provider.BaseColumns;
import android.text.format.Time;


/**
 * Created by Andrew on 4/20/2016.
 */
public class MovieContract {
    public static long normalizeDate(long startDate) {

        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /*
        Inner class that defines the table contents of the favorite table
    *//*
    public static final class FavoriteEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorite";

        public static final String COLUMN_MOVIE_ID = "movie_id";


    }
*/

    /*
        Inner class that defines the contents of the movie table
    */
    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movieData";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_OVERVIEW = "overview";

        public static final String COLUMN_POSTER_PATH = "poster_path";

        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        public static final String COLUMN_RELEASE_DATE = "release_date";

        public static final String COLUMN_MOVIE_ID = "movie_id";

    }
}
