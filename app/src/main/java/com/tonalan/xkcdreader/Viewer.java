package com.tonalan.xkcdreader;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class Viewer extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

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

        public class ImageTask extends AsyncTask<URL, Void, Drawable[]> {
            @Override
            protected Drawable[] doInBackground(URL... imgsrc) {
                Drawable[] images = new Drawable[imgsrc.length];
                try {
                    for (int i = 0; i < images.length; i++)
                        images[i] = new BitmapDrawable(BitmapFactory.decodeStream(imgsrc[i].openStream()));
                } catch (IOException e) { Log.e("XKCD Reader", "Error while opening stream", e); }

                return images;
            }

            @Override
            protected void onPostExecute(Drawable[] images) {
                FrameLayout frameLayout = (FrameLayout)getActivity().findViewById(R.id.container);
                ImageView imageView = null;
                if(layout != null) {
                    TextView textView = new TextView(getActivity().getApplicationContext());
                    int imgIndex = 0;

                    for(int i = 0; i < layout.length; i++) {
                        if (layout[i].equals("p"))
                            textView.append(content[i] + "\n");
                        else if (layout[i].equals("img")) {
                            (imageView = new ImageView(getActivity().getApplicationContext())).setImageDrawable(images[imgIndex]);
                            imgIndex++;

                            frameLayout.addView(textView);
                            frameLayout.addView(imageView);
                        }
                    }
                } else {
                    (imageView = new ImageView(getActivity().getApplicationContext())).setImageDrawable(images[0]);
                    frameLayout.addView(imageView);
                }
            }
        }

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private Integer index;

        private String title = null,
                    question = null,
                   attribute = null,
                         alt = null;

        private String[] content = null,
                          layout = null,
                            date = null;

        private Drawable[] images = null;

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

            String protoURL = genURL();

            try {
               new DataTask().execute(new URL(protoURL));
            } catch(MalformedURLException e) { Log.e("XKCD Reader", "Malformed URL", e); }
        }

        private String genURL(int number) {
            String protoURL = "http://xkcdapi.heroku.com/api";
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

            return protoURL;
        }

        private void parseContent(JSONObject data) {
            try {
                int value = getArguments().getInt(ARG_SECTION_NUMBER);
                if (value == 1)
                    alt = data.getString("alt");
                else if (value == 2 || value == 3) {
                    if (value == 2) {
                        question = data.getString("question");
                        attribute = data.getString("attribute");
                    }
                    content = data.getString("content").split("|");
                    layout = data.getString("layout").split("|");
                }

                title = data.getString("title");

                String[] imgURL = new String[]{data.getString("img")};
                imgURL = imgURL[0].contains("|") ? imgURL[0].split("|") : imgURL;
                images = getImages(imgURL);
            } catch (JSONException e) { Log.e("XKCD Reader", "Error while parsing JSON", e); }
        }

        private Drawable[] getImages(String[] imgs) {
            Drawable[] images = new Drawable[imgs.length];
            for(int i = 0; i < imgs.length; i++) {
                try {
                    images[i] = (new ImageTask().execute(new URL(imgs[i]))).get()[0];
                } catch(MalformedURLException e) { Log.e("XKCD Reader", "Malformed URL", e); }
                  catch (InterruptedException e) { Log.e("XKCD Reader", "Thread interrupted", e); }
                  catch (ExecutionException e) { Log.e("XKCD Reader", "Error executing thread", e); }

            }
            return images;
        }
    }
}
