package com.example.android.popularmovies.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Time;
import android.util.Log;

import com.example.android.popularmovies.app.MainActivity;
import com.example.android.popularmovies.app.R;
import com.example.android.popularmovies.app.Utility;
import com.example.android.popularmovies.app.data.MovieContract;
import com.uwetrottmann.tmdb.Tmdb;
import com.uwetrottmann.tmdb.entities.AppendToResponse;
import com.uwetrottmann.tmdb.entities.Movie;
import com.uwetrottmann.tmdb.entities.MovieResultsPage;
import com.uwetrottmann.tmdb.entities.Review;
import com.uwetrottmann.tmdb.entities.ReviewResultsPage;
import com.uwetrottmann.tmdb.entities.Videos;
import com.uwetrottmann.tmdb.enumerations.AppendToResponseItem;
import com.uwetrottmann.tmdb.services.MoviesService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Vector;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private final MoviesService movieService;


    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
//            MovieContract.WeatherEntry.COLUMN_WEATHER_ID,
//            MovieContract.WeatherEntry.COLUMN_MAX_TEMP,
//            MovieContract.WeatherEntry.COLUMN_MIN_TEMP,
//            MovieContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Tmdb tmdb = new Tmdb();
        tmdb.setApiKey("e95feea469573da8277773d047559d1a");
        movieService = tmdb.moviesService();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        String locationQuery = Utility.getPreferredLocation(getContext());

        MovieResultsPage movies = movieService.popular(1, "en");
        List<Movie> results = movies.results;
        Vector<ContentValues> cVVector = new Vector<ContentValues>(results.size());
        for (int i = 0; i < results.size(); i++) {
            ContentValues weatherValues = new ContentValues();
            Movie movie = results.get(i);
            weatherValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.id);
            List<Videos.Video> videos = movieService.videos(movie.id, "en").results;
            AppendToResponse atr = new AppendToResponse(AppendToResponseItem.VIDEOS);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Movie summary = movieService.summary(movie.id, "en", atr);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ReviewResultsPage reviews = movieService.reviews(movie.id, 1, "en");
            StringBuilder sb1 = new StringBuilder();
            for(Review result : reviews.results) {
                sb1.append(result.content);
                sb1.append("REVIEW_SEPARATOR");
            }
            StringBuilder sb = new StringBuilder();
            for (Videos.Video video : summary.videos.results) {
                sb.append(video.key);
                sb.append(",");
            }
            weatherValues.put(MovieContract.MovieEntry.COLUMN_DATE, System.currentTimeMillis());
            weatherValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movie.original_title);
            weatherValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.overview);
            weatherValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, movie.poster_path);
            weatherValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.release_date.getTime());
            weatherValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.vote_average);
            weatherValues.put(MovieContract.MovieEntry.COLUMN_RUNTIME, summary.runtime);
            weatherValues.put(MovieContract.MovieEntry.COLUMN_IS_CURRENT, 1);
            weatherValues.put(MovieContract.MovieEntry.COLUMN_IS_FAVORITE, 0);
            weatherValues.put(MovieContract.MovieEntry.COLUMN_VIDEOS, sb.toString());
            weatherValues.put(MovieContract.MovieEntry.COLUMN_REVIEWS, sb1.toString());


            cVVector.add(weatherValues);
        }
        int inserted = 0;
        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            // delete old data so we don't build up an endless history
            getContext().getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    null);
            getContext().getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, cvArray);


//                notifyWeather();
        }

        Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");



    }



    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
//        long locationId;
//
//        // First, check if the location with this city name exists in the db
//        Cursor locationCursor = getContext().getContentResolver().query(
//                MovieContract.LocationEntry.CONTENT_URI,
//                new String[]{MovieContract.LocationEntry._ID},
//                MovieContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
//                new String[]{locationSetting},
//                null);
//
//        if (locationCursor.moveToFirst()) {
//            int locationIdIndex = locationCursor.getColumnIndex(MovieContract.LocationEntry._ID);
//            locationId = locationCursor.getLong(locationIdIndex);
//        } else {
//            // Now that the content provider is set up, inserting rows of data is pretty simple.
//            // First create a ContentValues object to hold the data you want to insert.
//            ContentValues locationValues = new ContentValues();
//
//            // Then add the data, along with the corresponding name of the data type,
//            // so the content provider knows what kind of value is being inserted.
//            locationValues.put(MovieContract.LocationEntry.COLUMN_CITY_NAME, cityName);
//            locationValues.put(MovieContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
//            locationValues.put(MovieContract.LocationEntry.COLUMN_COORD_LAT, lat);
//            locationValues.put(MovieContract.LocationEntry.COLUMN_COORD_LONG, lon);
//
//            // Finally, insert location data into the database.
//            Uri insertedUri = getContext().getContentResolver().insert(
//                    MovieContract.LocationEntry.CONTENT_URI,
//                    locationValues
//            );
//
//            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
//            locationId = ContentUris.parseId(insertedUri);
//        }
//
//        locationCursor.close();
//        // Wait, that worked?  Yes!
//        return locationId;
        return 0L;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}