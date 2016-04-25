package com.example.android.movieapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Andrew on 4/20/2016.
 */
public class DatabaseController {

    private static final String TABLE_NAME = "movieData";

    private static final String DATABASE_NAME = "movie.db";

    private static final String COLUMN_ID = "_id";

    private static final String COLUMN_TITLE = "title";

    private static final String COLUMN_OVERVIEW = "overview";

    private static final String COLUMN_POSTER_PATH = "poster_path";

    private static final String COLUMN_VOTE_AVERAGE = "vote_average";

    private static final String COLUMN_RELEASE_DATE = "release_date";

    private static final String COLUMN_MOVIE_ID = "movie_id";

    private final Context context;

    SQLiteDatabase sqLiteDatabase;
    MovieDbHelper movieDbHelper;

    public DatabaseController(Context context) {
        this.context = context;
    }

    public void open(){
        movieDbHelper = new MovieDbHelper(context);
        sqLiteDatabase = movieDbHelper.getWritableDatabase();
    }

    public void insert(Movie movie){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, movie.getTitle());
        contentValues.put(COLUMN_OVERVIEW, movie.getOverview());
        contentValues.put(COLUMN_POSTER_PATH, movie.getPoster_path());
        contentValues.put(COLUMN_VOTE_AVERAGE, movie.getVote_average());
        contentValues.put(COLUMN_RELEASE_DATE, movie.getRelease_date());
        contentValues.put(COLUMN_MOVIE_ID, movie.getId());

        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
    }

    public ArrayList<Movie> getData(){
        ArrayList<Movie> movies = new ArrayList<>();
        String[] columns = {COLUMN_TITLE, COLUMN_OVERVIEW, COLUMN_POSTER_PATH, COLUMN_VOTE_AVERAGE, COLUMN_RELEASE_DATE, COLUMN_MOVIE_ID};
        Cursor cursor = sqLiteDatabase.query(TABLE_NAME, columns, null, null, null, null, null);
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            Movie movie = new Movie();
            movie.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
            movie.setOverview(cursor.getString(cursor.getColumnIndex(COLUMN_OVERVIEW)));
            movie.setPoster_path(cursor.getString(cursor.getColumnIndex(COLUMN_POSTER_PATH)));
            movie.setVote_average(cursor.getDouble(cursor.getColumnIndex(COLUMN_VOTE_AVERAGE)));
            movie.setRelease_date(cursor.getString(cursor.getColumnIndex(COLUMN_RELEASE_DATE)));
            movie.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_MOVIE_ID)));

            movies.add(movie);
        }
        return movies;
    }

    public void deleteMovieById(int id){
        sqLiteDatabase.delete(TABLE_NAME, COLUMN_MOVIE_ID + "=" + id, null);
    }

    public boolean isExist(int id){
        boolean check = false;
        ArrayList<Movie> movies = new ArrayList<>();
        movies = getData();

        for(int i = 0; i < movies.size(); i++){
            if(movies.get(i).getId() == id){
                check = true;
                break;
            }
        }
        return check;
    }
}
