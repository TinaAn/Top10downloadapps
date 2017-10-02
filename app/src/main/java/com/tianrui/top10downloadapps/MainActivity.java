package com.tianrui.top10downloadapps;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listApps;
    private String urlFeed = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int limitFeed = 10;
    //below strings are used to compare with the last url, if same, no need to update
    private String feedCachedUrl = "INVALIDATED";
    public static final String STATE_URL = "urlFeed";
    public static final String STATE_LIMIT = "limitFeed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listApps = (ListView) findViewById(R.id.xmlListViewer);

        if(savedInstanceState != null){
            urlFeed = savedInstanceState.getString(STATE_URL);
            limitFeed = savedInstanceState.getInt(STATE_LIMIT);
        }
        downloadUrl(String.format(urlFeed,limitFeed));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        if(limitFeed == 10) {
            menu.findItem(R.id.mnu10).setChecked(true);
        } else {
            menu.findItem(R.id.mnu25).setChecked(true);
        }

        return true;
    }


    //decide with menu is selected --> decide urlFeed and limitFeed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId(); //get id from the item

        switch (id) {
            case R.id.mnuFree:
                urlFeed = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.mnuSongs:
                urlFeed = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.mnuPaid:
                urlFeed = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.mnu10:
            case R.id.mnu25:
                if(!item.isChecked()){ //this case is really special, because only two choices, so when is not checked, must be another one
                    item.setChecked(true);
                    limitFeed = 35 - limitFeed;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " set item limit to " + limitFeed);
                }else{
                    Log.d(TAG, "onOptionsItemSelected: "+ item.getTitle() + " feed limit unchanged");
                }
                break;
            case R.id.mnuRefresh:
                feedCachedUrl = "INVALIDATED";
                break;
            default: //when use menu, must add default case as this
                return super.onOptionsItemSelected(item);
        }

        downloadUrl(String.format(urlFeed,limitFeed));
        return true;

    }

    //save the state, when screen rotates, can still retrieve the current states.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL,urlFeed);
        outState.putInt(STATE_LIMIT,limitFeed);
        super.onSaveInstanceState(outState);
    }


    private void downloadUrl(String urlFeed) {
        if(!urlFeed.equalsIgnoreCase(feedCachedUrl)){
            Log.d(TAG, "downloadUrl: starting Asynctask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(urlFeed);
            feedCachedUrl = urlFeed;
            Log.d(TAG, "downloadUrl: done");
        }else{
            Log.d(TAG, "downloadUrl: Url unchanged");
        }

    }

    //open a new thread to perform the download task
    //won't affect the main thread
    //Parameters,progress,result
    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData"; //debug info

        @Override
        protected void onPostExecute(String s) {
            //super.onPostExecute(s);
            // Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplication parseApplication = new ParseApplication();
            parseApplication.parse(s);

//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(
//                    MainActivity.this,R.layout.list_item,parseApplication.getApplications());
//            listApps.setAdapter(arrayAdapter);
            FeedAdapter feedAdapter = new FeedAdapter(
                    MainActivity.this, R.layout.list_apps, parseApplication.getApplications());
            listApps.setAdapter(feedAdapter);

        }

        //The download data thread do in background, get the first string from DownloadData
        @Override
        protected String doInBackground(String... params) {
            // Log.d(TAG, "doInBackground: starts with " + params[0]);

            String rssFeed = downLoadXML(params[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            }

            return rssFeed;
        }

        //download the data from url path
        private String downLoadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //int response = urlConnection.getResponseCode();
                // Log.d(TAG, "downLoadXML: The response code is " + response);
//                InputStream inputStream = urlConnection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())); //get the input stream, get InputStreamReader as input variable

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer); //the method returns a character as an integer. If the end of the stream has been reached the method returns -1.
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                //remember to close the reader
                reader.close();

                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downLoadXML: Invalid URL " + e.getMessage());  //must put url exception before IOException, url exception is a subclass of IO Exception
            } catch (IOException e) {
                Log.e(TAG, "downLoadXML: IO Exception reading data " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downLoadXML: Security Exception. Needs permission? " + e.getMessage());
                // e.printStackTrace(); //track the error
            }

            return null;
        }
    }
}
