package com.tonalan.xkcdreader;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


public class Viewer extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks  {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_xkcd);
                break;
            case 2:
                mTitle = getString(R.string.title_whatif);
                break;
            case 3:
                mTitle = getString(R.string.title_blog);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.viewer, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public class DataThread implements Runnable {
            private URL url;
            volatile JSONObject data = null;

            public DataThread(String protoURL) {
                try {
                    url = new URL(protoURL);
                } catch(MalformedURLException e) { Log.e("XKCD Reader", "Malformed URL", e); }
            }

            @Override
            public void run() {
                try {
                    data = new JSONObject((new BufferedReader(new InputStreamReader((InputStream)url.getContent()))).readLine());
                } catch (IOException e) {
                    Log.e("XKCD Reader", "Error while fetching data", e);
                } catch(JSONException e) {
                    Log.e("XKCD Reader", "Error while parsing JSON", e);
                } //Popup with error
            }
        }

        public class ImageThread implements Runnable {
            private URL url;
            volatile Drawable image = null;

            public ImageThread(String imgURL) {
                try {
                    url = new URL(imgURL);
                } catch(MalformedURLException e) { Log.e("XKCD Reader", "Malformed URL", e); }
            }

            @Override
            public void run() {
                try {
                    image = new BitmapDrawable(BitmapFactory.decodeStream(url.openStream()));
                } catch(IOException e) { Log.e("XKCD Reader", "Error while fetching image", e); }
            }
        }
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_viewer, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Viewer) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
            String protoURL = "http://xkcdapi.heroku.com/api";
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

            DataThread dataThread = new DataThread(protoURL);
            Thread run = new Thread(dataThread);
            run.start();
            while(run.isAlive()) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) { Log.e("XKCD Reader", "Thread interrupted", e); }
            }

            JSONObject data = dataThread.data;

            Integer index = null;
            String[] date = null;
            try {
                index = data.has("num") ? data.getInt("num") : null;
                if(getArguments().getInt(ARG_SECTION_NUMBER) == 3)
                    date = new String[] {
                            data.getString("day"),
                            data.getString("month"),
                            data.getString("year")
                    };
            } catch(JSONException e) { Log.e("XKCD Reader", "Error while reading data", e); }

            protoURL += "/" + (date == null ? index :
                    date[0] + "/" + date[1] + "/" + date[2]);

            dataThread = new DataThread(protoURL);
            run = new Thread(dataThread);
            run.start();
            while(run.isAlive()) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) { Log.e("XKCD Reader", "Thread interrupted", e); }
            }

            getContent(dataThread.data);
        }

        private void getContent(JSONObject data) {
            String title = null,
                question = null,
               attribute = null,
                     alt = null;

            String[] content = null,
                      layout = null;

            Drawable[] images = null;

            try {
                int value = getArguments().getInt(ARG_SECTION_NUMBER);
                if(value == 1)
                    alt = data.getString("alt");
                else if(value == 2 || value == 3) {
                    if(value == 2) {
                        question = data.getString("question");
                        attribute = data.getString("attribute");
                    }
                    content = data.getString("content").split("|");
                    layout = data.getString("layout").split("|");
                }

                title = data.getString("title");

                String[] imgURL = new String[] { data.getString("img") };
                imgURL = imgURL[0].contains("|") ? imgURL[0].split("|") : imgURL;
                images = getImages(imgURL);
            } catch(JSONException e) { Log.e("XKCD Reader", "Error while parsing JSON", e); }

            TextView textView = (TextView)getActivity().findViewById(R.id.xkcd_text);
            textView.append(title);
        }

        private Drawable getImage(String img) {
            ImageThread imageThread = new ImageThread(img);
            Thread run = new Thread(imageThread);
            run.start();
            while(run.isAlive()) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) { Log.e("XKCD Reader", "Thread interrupted", e); }
            }

            return imageThread.image;
        }

        private Drawable[] getImages(String[] imgs) {
            Drawable[] images = new Drawable[imgs.length];
            for(int i = 0; i < imgs.length; i++)
                images[i] = getImage(imgs[i]);
            return images;
        }
    }
}
