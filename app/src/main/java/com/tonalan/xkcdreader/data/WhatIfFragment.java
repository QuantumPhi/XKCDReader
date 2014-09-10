package com.tonalan.xkcdreader.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class WhatIfFragment extends DataFragment {

    @Override
    protected void parseContent(JSONObject data) {
        try {
            String[] tempAlt = new String[]{data.getString("img")};
            alt = tempAlt[0].contains("|") ? tempAlt[0].split("|") : tempAlt;

            question = data.getString("question");
            attribute = data.getString("attribute");
            content = data.getString("content").split("|");
            layout = data.getString("layout").split("|");

            title = data.getString("title");

            String[] imgURL = new String[]{data.getString("img")};
            images = getImages(imgURL[0].contains("|") ? imgURL[0].split("|") : imgURL);
        } catch (JSONException e) { Log.e("XKCD Reader", "Error while parsing JSON", e); }
    }
}
