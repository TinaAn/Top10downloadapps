package com.tianrui.top10downloadapps;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * The bridge between ListView and source data(from parseApplication)
 * Created by tianrui on 2017-01-03.
 */

public class FeedAdapter extends ArrayAdapter {
    private static final String TAG = "FeedAdapter";
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private List<FeedEntry> applications;

    public FeedAdapter(Context context, int resource, List<FeedEntry> applications) {
        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.applications = applications;
    }

    @Override
    public int getCount() {
        return applications.size();
    }

    @NonNull
    @Override
    //This is not efficient, every time calls this method will create a new view,
    // will cost lots of views and use memory.
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View view = layoutInflater.inflate(layoutResource, parent, false);
//        TextView appName = (TextView) view.findViewById(R.id.appName);
//        TextView appArtist = (TextView) view.findViewById(R.id.appArtist);
//        TextView appSummary = (TextView) view.findViewById(R.id.appSummary);
//
//        FeedEntry currentApp = applications.get(position);
//
//        appName.setText(currentApp.getName());
//        appArtist.setText(currentApp.getArtist());
//        appSummary.setText(currentApp.getSummary());
//
//        return view;
//    }

    //can make it more efficient, since findViewById is not that efficient, can create a view holder
    // to hold all the variables
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, parent, false);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

//        TextView appName = (TextView) convertView.findViewById(appName);
//        TextView appArtist = (TextView) convertView.findViewById(R.id.appArtist);
//        TextView appSummary = (TextView) convertView.findViewById(R.id.appSummary);

        FeedEntry currentApp = applications.get(position);

        viewHolder.appName.setText(currentApp.getName());
        viewHolder.appArtist.setText(currentApp.getArtist());
        viewHolder.appSummary.setText(currentApp.getSummary());

        return convertView;
    }

    private class ViewHolder {

        final TextView appName;
        final TextView appArtist;
        final TextView appSummary;

        ViewHolder(View v) {
            this.appName = (TextView) v.findViewById(R.id.appName);
            this.appArtist = (TextView) v.findViewById(R.id.appArtist);
            this.appSummary = (TextView) v.findViewById(R.id.appSummary);

        }
    }
}
