package com.tonalan.xkcdreader.data;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tonalan.xkcdreader.R;
import com.tonalan.xkcdreader.Viewer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public abstract class DataFragment extends Fragment {

    public class DataTask extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... datasrc) {
            HttpURLConnection connection;
            JSONObject data = null;
            try {
                connection = (HttpURLConnection)datasrc[0].openConnection();
                connection.setRequestMethod("GET");
                data = new JSONObject((new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine());
            } catch (IOException e) { Log.e("XKCD Reader", "Error opening connection", e); }
            catch (JSONException e) { Log.e("XKCD Reader", "Error parsing JSON", e); }

            return data;
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            String[] date = null;
            try {
                index = data.getInt("num");
                date = new String[]{
                        data.getString("day"),
                        data.getString("month"),
                        data.getString("year")
                };
            } catch (JSONException e) { Log.e("XKCD Reader", "Error while reading data", e); }

            parseContent(data);
        }
    }

    public class ImageTask extends AsyncTask<URL, Void, Bitmap[]> {
        @Override
        protected Bitmap[] doInBackground(URL... imgsrc) {
            Bitmap[] images = new Bitmap[imgsrc.length];
            try {
                for (int i = 0; i < images.length; i++)
                    images[i] = BitmapFactory.decodeStream(imgsrc[i].openStream());
            } catch (IOException e) { Log.e("XKCD Reader", "Error while opening stream", e); }

            return images;
        }

        @Override
        protected void onPostExecute(Bitmap[] images) {
            FrameLayout frameLayout = (FrameLayout)getActivity().findViewById(R.id.data);
            XKCDImageView imageView = null;
            if(layout != null) {
                TextView textView = new TextView(getActivity().getApplicationContext());
                int imgIndex = 0;

                for(int i = 0; i < layout.length; i++) {
                    if (layout[i].equals("p"))
                        textView.append(content[i] + "\n");
                    else if (layout[i].equals("img")) {
                        imageView = new XKCDImageView(getActivity().getApplicationContext(), images[imgIndex], alt[imgIndex]);
                        imgIndex++;

                        frameLayout.addView(textView);
                        frameLayout.addView(imageView);
                    }
                }
            } else {
                imageView = new XKCDImageView(getActivity().getApplicationContext(), images[0], alt[0]);
                frameLayout.addView(imageView);
            }
        }
    }

    protected static final String ARG_SECTION_NUMBER = "section_number";
    protected Integer index;

    protected String title = null,
            question = null,
            attribute = null;

    protected String[] content = null,
            layout = null,
            alt = null;

    protected Bitmap[] images = null;

    public static DataFragment newInstance(int sectionNumber) {
        DataFragment fragment = null;
        switch(sectionNumber) {
            case 1:
                fragment = new XKCDFragment();
                break;
            case 2:
                fragment = new WhatIfFragment();
                break;
            case 3:
                fragment = new BlogFragment();
                break;
        }
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_data, container, false);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((Viewer) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));

        String protoURL = genURL();

        try {
            new DataTask().execute(new URL(protoURL));
        } catch(MalformedURLException e) { Log.e("XKCD Reader", "Malformed URL", e); }
    }

    private String genURL(int number) {
        String protoURL = "http://xkcdapi.herokuapp.com/api";
        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
            case 1:
                protoURL += "/xkcd" + number;
                break;
            case 2:
                protoURL += "/whatif" + number;
                break;
            case 3:
                protoURL += "/blog" + number;
                break;
        }

        return protoURL;
    }

    private String genURL() {
        String protoURL = "http://xkcdapi.herokuapp.com/api";
        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
            case 1:
                protoURL += "/xkcd";
                break;
            case 2:
                protoURL += "/whatif";
                break;
            case 3:
                protoURL += "/blog";
                break;
        }

        return protoURL;
    }

    protected abstract void parseContent(JSONObject data);

    protected Bitmap[] getImages(String[] imgs) {
        Bitmap[] images = new Bitmap[imgs.length];
        URL[] imgsrc = new URL[imgs.length];
        try {
            for (int i = 0; i < imgs.length; i++)
                imgsrc[i] = new URL(imgs[i]);
        } catch(MalformedURLException e) { Log.e("XKCD Reader", "Malformed URL", e); }

        try {
            images = new ImageTask().execute(imgsrc).get();
        } catch (InterruptedException e) { Log.e("XKCD Reader", "Thread interrupted", e); }
          catch (ExecutionException e) { Log.e("XKCD Reader", "Error executing thread", e); }

        return images;
    }
}