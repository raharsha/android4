/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.popularmovies.app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.popularmovies.app.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private ShareActionProvider mShareActionProvider;
    private String mForecast;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.COLUMN_DATE,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_RUNTIME,
            MovieContract.MovieEntry.COLUMN_VIDEOS,
            MovieContract.MovieEntry.COLUMN_REVIEWS,
            MovieContract.MovieEntry.COLUMN_IS_FAVORITE
    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_MOVIE_DATE = 0;
    public static final int COL_MOVIE_DETAIL_ID = 1;
    public static final int COL_MOVIE_DETAIL_ORIGINAL_TITLE = 2;
    public static final int COL_MOVIE_DETAIL_POSTER_PATH = 3;
    public static final int COL_MOVIE_DETAIL_OVERVIEW = 4;
    public static final int COL_MOVIE_DETAIL_VOTE_AVERAGE = 5;
    public static final int COL_MOVIE_DETAIL_RELEASE_DATE = 6;
    public static final int COL_MOVIE_DETAIL_RUNTIME = 7;
    public static final int COL_MOVIE_DETAIL_VIDEOS = 8;
    public static final int COL_MOVIE_DETAIL_REVIEWS = 9;
    public static final int COL_MOVIE_DETAIL_IS_FAVORITE = 10;

    private static final String sMovieSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE + " = ? ";


    private ImageView mThumbnail;
    private TextView mYear;
    private TextView mTitle;
    private TextView mRating;
    private TextView mRuntime;
    private TextView mOverview;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    private ListView mTrailors;
    private ListView mReviews;
    private CheckBox mFav;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mThumbnail = (ImageView) rootView.findViewById(R.id.imageView);
        mTitle = (TextView) rootView.findViewById(R.id.textView);
        mYear = (TextView) rootView.findViewById(R.id.textView2);
        mRating = (TextView) rootView.findViewById(R.id.tvRating);
        mRuntime = (TextView) rootView.findViewById(R.id.tvLength);
        mOverview = (TextView) rootView.findViewById(R.id.tvOverview);
        mTrailors = (ListView) rootView.findViewById(R.id.lvTrailors);
        mReviews = (ListView) rootView.findViewById(R.id.lvReviews);
        mFav = (CheckBox) rootView.findViewById(R.id.favorite_button);
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    ContentValues weatherValues = new ContentValues();
                    weatherValues.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, isChecked ? 1 : 0);
                    CharSequence titleText1 = mTitle.getText();
                    if (titleText1 != null) {
                        String titleText = titleText1.toString();
                        String[] selectionArgs = new String[]{titleText};

                        compoundButton.getContext().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,
                                weatherValues, sMovieSelection, selectionArgs);
                    }

                }
        };
        mFav.setOnCheckedChangeListener(listener);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
//        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onSortbyChanged(String newSortby) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
//            long date = MovieContract.MovieEntry.getDateFromUri(uri);
//            Uri updatedUri = MovieContract.WeatherEntry.buildWeatherLocationWithDate(newSortby, date);
//            mUri = updatedUri;
//            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read weather condition ID from cursor
            int movieId = data.getInt(COL_MOVIE_DETAIL_ID);

            // Use weather art image
//            mThumbnail.setImageResource(Utility.getArtResourceForWeatherCondition(movieId));

            String url = null;
            url = "http://image.tmdb.org/t/p/w185" + data.getString(COL_MOVIE_DETAIL_POSTER_PATH);
            Picasso.with(getActivity()).load(url).into(mThumbnail);

//
//            // Read date from cursor and update views for day of week and date
            long date = data.getLong(COL_MOVIE_DETAIL_RELEASE_DATE);
            String year = Utility.getYear(getActivity(), date);
            mYear.setText(year);

            String title = data.getString(COL_MOVIE_DETAIL_ORIGINAL_TITLE);
            mTitle.setText(title);
//
//            // Read rating from cursor and update view
            String rating = data.getString(COL_MOVIE_DETAIL_VOTE_AVERAGE) + "/10";
            String length = data.getString(COL_MOVIE_DETAIL_RUNTIME) + "min";
            mRating.setText(rating);
            mRuntime.setText(length);

            String overview = data.getString(COL_MOVIE_DETAIL_OVERVIEW);
            mOverview.setText(overview);
            String[] trailors = data.getString(COL_MOVIE_DETAIL_VIDEOS).split(",");
            String reviewsStr = data.getString(COL_MOVIE_DETAIL_REVIEWS);
            if (reviewsStr != null) {
                String[] reviews = reviewsStr.split("REVIEW_SEPARATOR");
                ReviewsAdapter reviewsAdapter = new ReviewsAdapter(getActivity(),
                        android.R.layout.simple_list_item_1,reviews);
                mReviews.setAdapter(reviewsAdapter);
            }
            TrailorAdapter adapter = new TrailorAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, trailors);
            mTrailors.setAdapter(adapter);

            if (data.getInt(COL_MOVIE_DETAIL_IS_FAVORITE) == 1) {
                mFav.setChecked(true);
            }

//
//            // For accessibility, add a content rating to the icon field
//            mThumbnail.setContentDescription(rating);
//
//            // Read high temperature from cursor and update view
//            boolean isMetric = Utility.isMetric(getActivity());
//
//            double high = data.getDouble(COL_WEATHER_MAX_TEMP);
//            String highString = Utility.formatTemperature(getActivity(), high);
//            mOverview.setText(highString);
//
//            // Read low temperature from cursor and update view
//            double low = data.getDouble(COL_WEATHER_MIN_TEMP);
//            String lowString = Utility.formatTemperature(getActivity(), low);
//            mLowTempView.setText(lowString);
//
//            // Read humidity from cursor and update view
//            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
//            mHumidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
//
//            // Read wind speed and direction from cursor and update view
//            float windSpeedStr = data.getFloat(COL_WEATHER_WIND_SPEED);
//            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
//            mWindView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));
//
//            // Read pressure from cursor and update view
//            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
//            mPressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
//
//            // We still need this for the share intent
//            mForecast = String.format("%s - %s - %s/%s", dateText, rating, high, low);
//
//            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
//            if (mShareActionProvider != null) {
//                mShareActionProvider.setShareIntent(createShareForecastIntent());
//            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}