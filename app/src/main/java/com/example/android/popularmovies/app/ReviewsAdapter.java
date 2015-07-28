package com.example.android.popularmovies.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by haswath on 7/24/15.
 */
public class ReviewsAdapter extends ArrayAdapter<String> {
    String [] items;
    public ReviewsAdapter(Context context, int resource, String[] items) {
        super(context, resource, items);
        this.items = items;
    }

    @Override
    public TextView getView(final int position, View convertView, ViewGroup parent) {
        //super.getView(position, convertView, parent);
        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.review, null);
        }

        TextView view = (TextView) convertView.findViewById(R.id.tvReview);
        view.setText(items[position]);
        return view;
    }

}
