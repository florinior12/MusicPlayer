package com.example.iorda.musicplayer.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.net.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;


public class MusicControl extends AppCompatActivity implements MediaController.MediaPlayerControl {
    private final String API_KEY = "89c6b2bc98f70c1741f0b107a06da1c4";

    private ImageButton pauseButton;
    private ImageButton nextButton, prevButton;


    private MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;
    private boolean paused = false;
    private TextView songTitle, artistName;
    private TextView lyricsView;
    private ImageView imageView;
    private ArrayList<Song> songs;


    private boolean playing;


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
        playing = (boolean) getIntent().getSerializableExtra("playing");


        nextButton = (ImageButton) findViewById(R.id.nextButton);
        prevButton = (ImageButton) findViewById(R.id.prevButton);
        pauseButton = (ImageButton) findViewById(R.id.pause);
        lyricsView = (TextView) findViewById(R.id.TEXT_STATUS_ID);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setPressed(true);
                playNext();

                pauseButton.setActivated(false);
                setSongTitle();
                paused = false;
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setPressed(true);
                pauseButton.setActivated(false);
                playPrev();
                setSongTitle();
                paused = false;
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!paused) {
                    v.setActivated(true);
                    pause();
                    paused = true;
                } else {
                    v.setActivated(false);
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
        songTitle.setText(currentSong.getmSongName());
        artistName = (TextView) findViewById(R.id.artistName);
        imageView = (ImageView) findViewById(R.id.imageView);
        artistName.setText(currentSong.getmArtistName());
        if (musicService.getLyrics() == null) {
            try {
                getResponse(currentSong.getmArtistName(), currentSong.getmSongName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            lyricsView.setText(musicService.getLyrics());
            imageView.setImageBitmap(musicService.getImage());

        }
        //Log.v("---setSongTitle", lyrics);

    }


    public void getResponse(String artist, String track) throws IOException {
        artist = deAccent(artist);
        track = deAccent(track);
        //get lyrics!
        artist = artist.replaceAll(" ", "-").toLowerCase().replaceAll("/", "");
        track = track.replaceAll(" ", "-").toLowerCase().replaceAll("/", "");


        //String myUrl = "https://api.musixmatch.com/ws/1.1/matcher.track.get?format=jsonp&callback=callback&q_artist=" + artist + "&q_track=" + track + "&apikey=" + API_KEY;
        String myUrl = "http://www.songlyrics.com/" + artist + "/" + track + "-lyrics/";
        Log.v("---getResponse", myUrl);

        LyricsGet getLyrics = new LyricsGet();
        getLyrics.execute(myUrl);

        //get album art!
        artist = artist.replaceAll("-", "%20");
        track = track.replaceAll("-", "%20");
        myUrl = "http://ws.audioscrobbler.com/2.0/?method=track.search&track=" + track + "&artist=" + artist + "&api_key=" + API_KEY + "&format=json";

        ArtGet getArt = new ArtGet();
        getArt.execute(myUrl);


    }

    private class ArtGet extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... url) {
            URL myurl = null;
            try {
                myurl = new URL(url[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String input = null;
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(
                                myurl.openStream()));
                input = in.readLine();
                //Log.v("---ASYNC Input", input);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (input != null) {


                Log.v("----ARTGET", input);
                JSONObject object = null;

                String art_url = null;

                try {
                    object = new JSONObject(input.substring(input.indexOf("{"), input.lastIndexOf("}") + 1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    JSONArray tracks = object.getJSONObject("results").getJSONObject("trackmatches").getJSONArray("track");
                    if (tracks.length() == 0) {
                        return null;
                    } else {
                        JSONArray images = ((JSONObject) tracks.get(0)).getJSONArray("image");
                        art_url = ((JSONObject) images.get(images.length() - 1)).getString("#text");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.v("----ARTGET", art_url);


                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(art_url).getContent());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bitmap;
            } else return null;

        }

        @Override
        protected void onPostExecute(Bitmap b) {
            if (b != null) {
                imageView.setImageBitmap(b);
                musicService.setImage(b);
            }
        }


    }

    private class LyricsGet extends AsyncTask<String, Void, List<String>> {

        private Exception exception;
        private String input = null;
        private final ProgressDialog dialog = new ProgressDialog(MusicControl.this);

        protected void onPreExecute() {
            this.dialog.setMessage("Processing...");
            this.dialog.show();
        }

        protected List<String> doInBackground(String... url) {

            List<String> lyrics = new ArrayList<String>();

            Document doc = null;
            try {
                doc = Jsoup.connect(url[0]).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (doc != null) {
                Element p = doc.select("p.songLyricsV14").get(0);
                for (Node e : p.childNodes()) {
                    if (e instanceof TextNode) {
                        lyrics.add(((TextNode) e).getWholeText());
                    }
                }
                //Log.v("---Async",lyrics.get(0) + lyrics.size());
                return lyrics;
            } else {
                lyrics.add("No lyrics found :(");
                return lyrics;
            }


        }

        @Override
        protected void onPostExecute(List<String> s) {
            String text = "";
            for (String string : s) {
                text += string;
            }

            musicService.setLyrics(text);
            lyricsView.setText(text);
            dialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if (musicBound) unbindService(musicConnection);
        super.onDestroy();

    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get the service into our variable from the binder
            musicService = binder.getService();
            Log.v("---Serv", playing + "");
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

    //get rid of diacritics
    public String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }


}
