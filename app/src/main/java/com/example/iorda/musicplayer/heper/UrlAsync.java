package com.example.iorda.musicplayer.heper;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by iorda on 5/5/2017.
 */

public class UrlAsync extends AsyncTask<String, Void, String> {

    private Exception exception;
    private String inputLine = "";

    public String getContent() {
        return inputLine;
    }

    protected String doInBackground(String... url) {
        try {
            Log.v("---ASYNC---", "It's doing !" + url[0]);

            URL musix = new URL(url[0]);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            musix.openStream()));

            inputLine = in.readLine();
            in.close();
            Log.v("---async", inputLine);
            return inputLine;
        } catch (Exception e) {
            this.exception = e;

            return null;
        }
    }


}
