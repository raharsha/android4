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
import android.widget.Toast;

/**
 * Created by haswath on 7/24/15.
 */
public class TrailorAdapter extends ArrayAdapter<String> {
    String [] items;
    public TrailorAdapter(Context context, int resource, String [] items) {
        super(context, resource, items);
        this.items = items;
    }

    @Override
    public ImageView getView(final int position, View convertView, ViewGroup parent) {
        //super.getView(position, convertView, parent);
        if (convertView == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.list_item_trailer, null);
        }

        ImageView view = (ImageView) convertView.findViewById(R.id.imageButton);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(view.getContext(), items[position],
//                        Toast.LENGTH_LONG).show();
                watchYoutubeVideo(items[position], view);
            }
        });
        return view;
    }

    public static void watchYoutubeVideo(String id, View view){
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            view.getContext().startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent=new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v="+id));
            view.getContext().startActivity(intent);
        }
    }
}
