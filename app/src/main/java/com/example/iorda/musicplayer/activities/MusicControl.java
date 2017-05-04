package com.example.iorda.musicplayer.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;

import com.example.iorda.musicplayer.R;
import com.example.iorda.musicplayer.heper.Song;
import com.example.iorda.musicplayer.heper.UrlAsync;
import com.example.iorda.musicplayer.services.MusicService;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.net.*;
import java.util.concurrent.ExecutionException;

public class MusicControl extends AppCompatActivity implements MediaController.MediaPlayerControl {

    private MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;
    private boolean paused = false;
    private TextView songTitle;
    private TextView lyrics;
    private ArrayList<Song> songs;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    protected void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        if (playIntent == null) {   //if there is no intent
            //create an intent between the activity_main activity and the service which plays music
            playIntent = new Intent(this, MusicService.class);
            //bindService is a method provided by android used to bind an intent to a service using a service connection
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        //Log.v("----setSongTitle", musicService.getSongPosition() + "");

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_controller);

        songs = (ArrayList<Song>) getIntent().getSerializableExtra("songs");

        Button nextButton = (Button) findViewById(R.id.nextButton);
        Button prevButton = (Button) findViewById(R.id.prevButton);
        Button pauseButton = (Button) findViewById(R.id.pause);
        lyrics = (TextView) findViewById(R.id.TEXT_STATUS_ID);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
                setSongTitle();
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
                setSongTitle();
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!paused) {
                    pause();
                    paused = true;
                } else {
                    start();
                    paused = false;
                }
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void setSongTitle() {

        Song currentSong = songs.get(musicService.getSongPosition());
        songTitle = (TextView) findViewById(R.id.songTitle);
        songTitle.setText(currentSong.getmArtistName() + " - " + currentSong.getmSongName());
        String lyrics = null;
        try {
            getResponse(currentSong.getmArtistName(), currentSong.getmSongName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.v("---setSongTitle", lyrics);

    }

    private void setLyrics(String s) {
        lyrics.setText(s);
    }



    public void getResponse(String artist, String track) throws IOException {
        final String API_KEY = "94315e4b34c8e989919c728d44ee76a8";
        artist = artist.replaceAll(" ", "%20").toLowerCase();
        track = track.replaceAll(" ", "%20").toLowerCase();

        String myUrl = "https://api.musixmatch.com/ws/1.1/matcher.track.get?format=jsonp&callback=callback&q_artist=" + artist + "&q_track=" + track + "&apikey=" + API_KEY;

        UrlAsync urlAsync = new UrlAsync();
        urlAsync.execute(myUrl);


    }

    private class UrlAsync extends AsyncTask<String, Void, String> {

        private Exception exception;
        private String input = null;
        private final ProgressDialog dialog = new ProgressDialog(MusicControl.this);

        protected void onPreExecute() {
            this.dialog.setMessage("Processing...");
            this.dialog.show();
        }
        protected String doInBackground(String... url) {
            try {

                Log.v("---ASYNC---", "It's doing !" + url[0]);

                URL musix = new URL(url[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                musix.openStream()));

                input = in.readLine();
                in.close();

                return input;
            } catch (Exception e) {
                this.exception = e;

                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            setLyrics(s);
            dialog.dismiss();
        }
    }



    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get the service into our variable from the binder
            musicService = binder.getService();
            setSongTitle();
            //the music is now bound to the service
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    //Below are the methods used to control the media player
    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        //playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPng())
            return musicService.getDur();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPng())
            return musicService.getPosn();
        else
            return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound)
            return musicService.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void playNext() {
        musicService.playNext();

    }

    private void playPrev() {
        musicService.playPrev();

        // controller.show(0);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("MusicControl Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    /*private void setController() {
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.lvSongs));
        controller.setEnabled(true);

    }

    public class MusicController extends MediaController {
        public MusicController(Context c) {
            super(c);
        }

        public void hide() {

        }
    }*/
}
