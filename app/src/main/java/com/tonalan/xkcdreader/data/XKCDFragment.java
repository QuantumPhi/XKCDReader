package com.tonalan.xkcdreader.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class XKCDFragment extends DataFragment {

    @Override
    protected void parseContent(JSONObject data) {
        try {
            String[] tempAlt = new String[]{data.getString("alt")};
            alt = tempAlt[0].contains("|") ? tempAlt[0].split("|") : tempAlt;

            title = data.getString("title");

            String[] imgURL = new String[]{data.getString("img")};
            imgURL = imgURL[0].contains("|") ? imgURL[0].split("|") : imgURL;
            images = getImages(imgURL);
        } catch (JSONException e) { Log.e("XKCD Reader", "Error while parsing JSON", e); }
    }
}
