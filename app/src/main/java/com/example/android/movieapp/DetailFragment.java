package com.example.android.movieapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
 * Created by Andrew on 4/20/2016.
 */
public class DetailFragment extends Fragment {

    Movie movie;

    boolean check;

    ArrayList trailerName;
    ArrayList trailerKey;
    ArrayList reviewArray;
    ArrayAdapter trailerAdapter;
    ArrayAdapter reviewAdapter;

    CheckBox checkBox;

    DatabaseController databaseController;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        trailerName = new ArrayList();
        trailerKey = new ArrayList();
        reviewArray = new ArrayList();

        movie = new Movie();

        databaseController = new DatabaseController(getActivity());
        databaseController.open();


        Intent intent = getActivity().getIntent();



        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        trailerAdapter = new ArrayAdapter<String>(getActivity(), R.layout.trailer_list_item, R.id.trailer_listitem_textview, trailerName);
        ListView trailerList = (ListView) rootView.findViewById(R.id.trailer_list_view);
        trailerList.setAdapter(trailerAdapter);
        trailerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + trailerKey.get(position)));
                startActivity(intent);
            }
        });

        reviewAdapter = new ArrayAdapter(getActivity(), R.layout.review_list_item, R.id.review_listitem_textview, reviewArray);
        ListView reviewList = (ListView) rootView.findViewById(R.id.review_list_view);
        reviewList.setAdapter(reviewAdapter);

        if (intent != null) {
            Movie movieOfIntent = (Movie) getArguments().getSerializable("Movie");
            //Movie movieOfIntent = (Movie)intent.getSerializableExtra("Movie");

            movie = movieOfIntent;

            String url = "http://image.tmdb.org/t/p/w185" + movie.getPoster_path();
            ((TextView) rootView.findViewById(R.id.title_text_view)).setText(movie.getTitle());
            Picasso.with(getActivity()).load(url).into(((ImageView) rootView.findViewById(R.id.poster_image_view)));
            ((TextView) rootView.findViewById(R.id.release_text_view)).setText(movie.getRelease_date());
            ((TextView) rootView.findViewById(R.id.rating_text_view)).setText(movie.getVote_average() + " / 10");
            ((TextView) rootView.findViewById(R.id.overview_text_view)).setText(movie.getOverview());
            checkBox = (CheckBox) rootView.findViewById(R.id.favorite_button);

            check = databaseController.isExist(movie.getId());
            checkBox.setChecked(check);

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = movie.getId();
                    if (check)
                        databaseController.deleteMovieById(id);
                    else
                        databaseController.insert(movie);
                }
            });

            updateMovie(movie.getId() + "");
        }

        return rootView;
    }


    private void updateMovie(String id) {
        FetchMovieTrailer trailerTask = new FetchMovieTrailer();
        FetchMovieReview reviewTask = new FetchMovieReview();
        trailerTask.execute(id);
        reviewTask.execute(id);
    }

    /** Fetch Trailers */
    public class FetchMovieTrailer extends AsyncTask<String, Void, String[][] > {

        private final String LOG_TAG = FetchMovieTrailer.class.getSimpleName();


        private String[][] getMovieDataFromJson(String movieJsonStr) throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_NAME = "name";
            final String OWM_KEY = "key";
            final String OWM_RESULT = "results";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULT);

            if(movieArray.length() == 0)
                return null;

            String[][] resultStrs = new String[movieArray.length()][2];

            for (int i = 0; i < movieArray.length(); i++) {

                String name;
                String key;

                // Get the JSON object
                JSONObject movieObject = movieArray.getJSONObject(i);

                name = movieObject.getString(OWM_NAME);
                key = movieObject.getString(OWM_KEY);


                resultStrs[i][0] = name;
                resultStrs[i][1] = key;

            }

            return resultStrs;

        }


        @Override
        protected String[][] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String id = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;


            try {

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String VIDEOS_PARAM = "videos";
                final String KEY_PARAM = "api_key";
                final String API_KEY = "a2c43ff043a570c696a61d8d37d7075f";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(id)
                        .appendPath(VIDEOS_PARAM)
                        .appendQueryParameter(KEY_PARAM, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());


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
        protected void onPostExecute(String[][] result) {

            trailerName.clear();
            trailerKey.clear();

            if (result != null) {
                for (int i = 0; i < result.length; i++) {
                    trailerName.add(result[i][0]);
                    trailerKey.add(result[i][1]);
                }
            }
            else{
                String res = "There is no trailer for this movie!";
                trailerName.add(res);
            }
            trailerAdapter.notifyDataSetChanged();
        }
    }

    /** Fetch Reviews */
    public class FetchMovieReview extends AsyncTask<String, Void, String[][] > {

        private final String LOG_TAG = FetchMovieReview.class.getSimpleName();


        private String[][] getMovieDataFromJson(String movieJsonStr) throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_AUTHOR = "author";
            final String OWM_CONTENT = "content";
            final String OWM_RESULT = "results";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULT);

            if(movieArray.length() == 0)
                return null;

            String[][] resultStrs = new String[movieArray.length()][2];

            for (int i = 0; i < movieArray.length(); i++) {

                String author;
                String content;

                // Get the JSON object
                JSONObject movieObject = movieArray.getJSONObject(i);

                author = movieObject.getString(OWM_AUTHOR);
                content = movieObject.getString(OWM_CONTENT);


                resultStrs[i][0] = author;
                resultStrs[i][1] = content;

            }

            return resultStrs;

        }


        @Override
        protected String[][] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String id = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;


            try {

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String REVIEWS_PARAM = "reviews";
                final String KEY_PARAM = "api_key";
                final String API_KEY = "a2c43ff043a570c696a61d8d37d7075f";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(id)
                        .appendPath(REVIEWS_PARAM)
                        .appendQueryParameter(KEY_PARAM, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

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
        protected void onPostExecute(String[][] result) {

            reviewArray.clear();

            if (result != null) {
                for (int i = 0; i < result.length; i++) {
                    reviewArray.add(result[i][1] + "\nBy " + result[i][0] + "\n\n");
                }
            }
            else{
                String res = "There is no review for this movie!";
                reviewArray.add(res);
            }
            reviewAdapter.notifyDataSetChanged();
        }
    }
}

