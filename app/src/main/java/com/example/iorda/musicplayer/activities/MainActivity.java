package com.example.iorda.musicplayer.activities;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;

import com.example.iorda.musicplayer.R;
import com.example.iorda.musicplayer.adapters.SongListAdapter;
import com.example.iorda.musicplayer.heper.Song;
import com.example.iorda.musicplayer.services.MusicService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;



public class MainActivity extends AppCompatActivity  {

    private ArrayList<Song> songsList;
    private SongListAdapter mAdapterListFile;
    private String[] STAR = {"*"};
    private ListView mListSongs;
    private Intent intent;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false, playbackPaused = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this,MusicControl.class);
        init();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    //when the activity_main activity starts, it establishes a connection to the music service using an intent
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {   //if there is no intent
            //create an intent between the activity_main activity and the service which plays music
            playIntent = new Intent(this, MusicService.class);
            //bindService is a method provided by android used to bind an intent to a service using a service connection
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    protected void onDestroy() {
        if(musicBound) unbindService(musicConnection);
        stopService(playIntent);
        musicService = null;
        super.onDestroy();

    }

    protected void onPause() {
        super.onPause();
        paused = true;
    }

    protected void onResume() {
        super.onResume();
        if(paused) {
            //setController();
            paused = false;
        }
    }

    protected void onStop() {
       // controller.hide();
        super.onStop();
    }

    //menu buttons
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_end:
                stopService(playIntent);
                musicService = null;
                System.exit(0);
                break;
            case R.id.action_shuffle:

                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //connect to music service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get the service into our variable from the binder
            musicService = binder.getService();

            //pass the list of songs to the service
            musicService.setList(songsList);

            //the music is now bound to the service
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void init() {
        getActionBar();
        mListSongs = (ListView) findViewById(R.id.lvSongs);


        songsList = listAllSongs();
        Collections.sort(songsList, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getmSongName().compareTo(o2.getmSongName());
            }
        });


        mAdapterListFile = new SongListAdapter(this,songsList);
        mListSongs.setAdapter(mAdapterListFile);
        mListSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Integer.parseInt(view.getTag().toString()) != musicService.getSongPosition()) { //if the current soong is different than the song i want to play
                    mListSongs.setItemChecked(position, true);
                    mAdapterListFile.notifyDataSetChanged();
                    musicService.setSong(Integer.parseInt(view.getTag().toString()));
                    musicService.playSong();
                    //controller.show();
                    //setController();

                    if (playbackPaused) {
                        //setController();
                        playbackPaused = false;
                    }
                    view.setActivated(true);
                }
                intent.putExtra("songs",songsList);
                startActivity(intent);

            }
        });
        mListSongs.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mAdapterListFile.setSongList(songsList);

        //setController();


    }

    private ArrayList<Song> listAllSongs() {
        Cursor cursor;
        songsList = new ArrayList<>();

        Uri allSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " !=0";
        if (sdIsPresent()) {
            cursor = getContentResolver().query(allSongsUri, STAR, selection, null, null);
            if (cursor!=null) {
                if (cursor.moveToFirst()) {
                    do {
                        Song song = new Song();

                        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));


                        song.setmSongName(data);
                        String duration = getDuration(Long.parseLong(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));
                        song.setmDuration(duration);

                        song.setSongID(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        /*song.setSongUri(ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                cursor.getColumnIndex(MediaStore.Audio.Media._ID)));*/


                        song.setmFullPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        song.setmArtistName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));

                        songsList.add(song);

                    } while(cursor.moveToNext());

                }
                cursor.close();

                return songsList;
            }   else
            {
                Toast.makeText(this,"NO SONGS", Toast.LENGTH_SHORT);
            }

        }
        return null;
    }

    //gets the position of a chosen song and plays it
    /*public void songPicked(View view) {

        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();

        if(playbackPaused) {
            //setController();
            playbackPaused = false;
        }
        view.setActivated(true);
        controller.show();
        setController();


    }*/

    private static boolean sdIsPresent() {
        if (android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }

    private static String getDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be positive!");
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);

        millis -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(6);

        sb.append(minutes < 10 ? "0" + minutes : minutes);
        sb.append(":");
        sb.append(seconds < 10 ? "0" + seconds : seconds);

        return sb.toString();
    }



}
