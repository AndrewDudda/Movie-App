package com.example.android.movieapp;

// This fragment must be inherit from android.support.v4.app.Fragment, not android.app.Fragment.

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.android.movieapp.data.DatabaseController;
import com.example.android.movieapp.data.Movie;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Andrew on 3/21/2016.
 */
public class MovieFragment extends Fragment {

    private ArrayList<Movie> movies;

    private GridView gridview;

    private ImageAdapter imageAdapter;

    DatabaseController databaseController;

    Callback callback;

    Bundle bundle;


    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        bundle = savedInstanceState;
        super.onCreate(savedInstanceState);

        // This fragment has option menu.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(bundle == null)
            inflater.inflate(R.menu.moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        databaseController = new DatabaseController(getActivity());
        databaseController.open();

        gridview = (GridView) rootView.findViewById(R.id.gridview_movie);
        movies = new ArrayList<>();
        imageAdapter = new ImageAdapter(getActivity(), movies);
        gridview.setAdapter(imageAdapter);

        return rootView;
    }


    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<Movie> data;

        public ImageAdapter(Context c, ArrayList<Movie> d) {
            mContext = c;
            data = d;
        }

        public int getCount() {
            return data.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            ImageView imageView;


            if (convertView == null) {
                // if it's not recycled, initialize some attributes

                imageView = new ImageView(mContext);

                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);


            } else {
                imageView = (ImageView) convertView;
            }


            String url = "http://image.tmdb.org/t/p/w185" + movies.get(position).getPoster_path();
            Picasso.with(mContext).load(url).into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    callback.setSelectedMovie(data.get(position));
                }
            });

            return imageView;
        }

    }

    public void setMovie(Callback callbackMovie) {
        callback = callbackMovie;
    }

    private void updateMovie() {
        FetchMovieTask movieTask = new FetchMovieTask();

        SharedPreferences prefs_sort = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = prefs_sort.getString(getString(R.string.pref_sort_key), getString(R.string.pref_most_popular_value));

        if(sort.equals(getString(R.string.pref_favorite_value))){
            movies.clear();
            ArrayList<Movie> result = databaseController.getData();
            for(int i = 0; i < result.size(); i++)
                movies.add(result.get(i));
            imageAdapter.notifyDataSetChanged();
        }
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String pageNumber = prefs.getString(getString(R.string.pref_page_number_key), getString(R.string.pref_page_number_default));
            int page = Integer.parseInt(pageNumber);
            if (page < 1)
                pageNumber = "1";
            else if (page > 100)
                pageNumber = "100";
            movieTask.execute(pageNumber);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie> > {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();


        private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException {

            // names of the JSON objects that need to be extracted.
            final String OWM_TITLE = "original_title";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_OVERVIEW = "overview";
            final String OWM_VOTE_AVERAGE = "vote_average";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_ID = "id";
            final String OWM_RESULT = "results";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULT);


            ArrayList<Movie> resultStrs = new ArrayList<>();

            for(int i = 0; i < movieArray.length(); i++) {

                String title;
                String poster_path;
                String overview;
                String vote_average;
                String release_date;
                String id;


                // Get the JSON object
                JSONObject movieObject = movieArray.getJSONObject(i);

                title = movieObject.getString(OWM_TITLE);
                overview = movieObject.getString(OWM_OVERVIEW);
                poster_path = movieObject.getString(OWM_POSTER_PATH);
                vote_average = movieObject.getString(OWM_VOTE_AVERAGE);
                release_date = movieObject.getString(OWM_RELEASE_DATE);
                id = movieObject.getString(OWM_ID);

                Movie movie = new Movie();

                movie.setTitle(title);
                movie.setOverview(overview);
                movie.setPoster_path(poster_path);
                movie.setVote_average(Double.parseDouble(vote_average));
                movie.setRelease_date(release_date);
                movie.setId(Integer.parseInt(id));

                resultStrs.add(movie);
            }

            return resultStrs;

        }


        @Override
        protected ArrayList<Movie> doInBackground(String... params) {


            if (params.length == 0) {
                return null;
            }

            String pageNumber = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;


            try {

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String PAGE_PARAM = "page";
                final String KEY_PARAM = "api_key";
                final String SORTING_PARAM = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString(
                                getString(R.string.pref_sort_key),
                                getString(R.string.pref_most_popular_value)
                        );
                final String API_KEY = "#put your API_KEY";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(SORTING_PARAM)
                        .appendQueryParameter(PAGE_PARAM, pageNumber)
                        .appendQueryParameter(KEY_PARAM, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                //Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    movieJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    movieJsonStr = null;
                }
                movieJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Movie JSON String: " + movieJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);

                movieJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> result) {

            if (result != null) {
                movies.clear();
                for(int i = 0; i < result.size(); i++)
                    movies.add(result.get(i));

                imageAdapter.notifyDataSetChanged();

            }
        }
    }
}
