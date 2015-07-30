package com.example.android.popularmovies.app;

/**
 * Created by haswath on 7/29/15.
 */
/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 ARNAUD FRUGIER
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.app.data.MovieContract;
import com.squareup.picasso.Picasso;

public class SimpleCursorRecyclerAdapter extends CursorRecyclerAdapter<SimpleViewHolder> {

    private final Context context;
    private int mLayout;

    public SimpleCursorRecyclerAdapter(Context context, int layout, Cursor c) {
        super(c);
        mLayout = layout;
        this.context = context;
    }

    @Override
    public SimpleViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(mLayout, parent, false);
        return new SimpleViewHolder(v, context);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, Cursor cursor) {
        holder.setCursor(cursor);
        String url = null;
        url = "http://image.tmdb.org/t/p/w185" + cursor.getString(MoviesFragment.COL_POSTER_PATH);
        Picasso.with(context).load(url).into(holder.iconView);
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        Cursor cursor = super.swapCursor(c);
        return cursor;
    }
}

class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    //public TextView[] views;
    public final ImageView iconView;
    private final Context context;
    private Cursor cursor;
    private long movieId;

    public SimpleViewHolder(View itemView, Context context)
    {
        super(itemView);
        iconView = (ImageView) itemView.findViewById(R.id.imageView1);
        this.context = context;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        int position = getLayoutPosition(); // gets item position
//        Cursor cursor = getcu;//(Cursor) view.getItemAtPosition(position);
        if (cursor != null) {
            ((MoviesFragment.Callback) view.getContext())
                    .onItemSelected(MovieContract.MovieEntry.buildMovieWithId(movieId));
        }
    }

    public void setCursor(Cursor cursor) {
        movieId = cursor.getLong(MoviesFragment.COL_MOVIE_ID);
        this.cursor = cursor;
    }
}