package com.example.top10downloader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: starting Asynctask");
        DownloadData downloadData = new DownloadData();

        // App needs permission to access the internet - added uses-permission tag in AndroidManifest.xml file
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
        Log.d(TAG, "onCreate: done");
    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        
        private static final String TAG = "DownloadData";

        // Takes returned value from doInBackground() as param. Runs on UI thread.
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);
        }

        // Runs in a background thread as async task
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                // Create URL from string url path
                URL url = new URL(urlPath);

                // Create HTTP connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // get response code returned from connection request
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code was " + response);

                // Shortened form of code below
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                /*// Get input stream from connection
                InputStream inputStream = connection.getInputStream();

                // Create input stream reader
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                // Read new input stream with buffered reader
                BufferedReader reader = new BufferedReader(inputStreamReader);*/

                // Read characters from buffered stream
                int charsRead;

                // Read 500 chars at a time from stream
                char[] inputBuffer = new char[500];

                // Keep looping until the end of the input stream is reached
                while(true) {

                    // Buffered reader's read() method returns numbers of chars read
                    charsRead = reader.read(inputBuffer);

                    if (charsRead < 0) {
                        break;
                    }
                    // If chars have been read, append these chars to the xmlResult
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close();
                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data: " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception. Needs permission? " + e.getMessage());
                //e.printStackTrace();
            }
            return null;
        }
    }
}
