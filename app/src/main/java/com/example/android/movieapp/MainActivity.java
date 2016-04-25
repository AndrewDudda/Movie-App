package com.example.android.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.android.movieapp.data.Movie;

public class MainActivity extends AppCompatActivity implements Callback {

    boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout panelTwo = (FrameLayout) findViewById(R.id.movie_detail_container);
        mTwoPane = (panelTwo == null)? false : true;

            MovieFragment movieFragment = new MovieFragment();
            movieFragment.setMovie(this);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_movie, movieFragment)
                    .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setSelectedMovie(Movie movie) {

        if(mTwoPane){
            // case Two Pane
            DetailFragment detailFragment = new DetailFragment();
            Bundle extra = new Bundle();
            extra.putSerializable("Movie", movie);
            detailFragment.setArguments(extra);
            getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container, detailFragment).commit();
        }
        else{
            // case One Pane
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("Movie", movie);
            this.startActivity(intent);
        }
    }
}
