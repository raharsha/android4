package com.example.android.popularmovies.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Created by haswath on 7/24/15.
 */
public class TrailorAdapter extends ArrayAdapter<String> {
    public TrailorAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public ImageView getView(int position, View convertView, ViewGroup parent) {
        //super.getView(position, convertView, parent);
        ImageView view = (ImageView) convertView.findViewById(R.id.imageButton);
        return view;
    }
}
