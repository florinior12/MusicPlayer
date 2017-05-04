package com.example.iorda.musicplayer.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import com.example.iorda.musicplayer.services.MusicService;

import java.util.ArrayList;

public class MusicControl extends AppCompatActivity implements MediaController.MediaPlayerControl{

    private MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;
    private boolean paused = false;
    private TextView songTitle;
    private ArrayList<Song> songs;

    protected void onStart() {
        super.onStart();
        if (playIntent == null) {   //if there is no intent
            //create an intent between the activity_main activity and the service which plays music
            playIntent = new Intent(this, MusicService.class);
            //bindService is a method provided by android used to bind an intent to a service using a service connection
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
        //Log.v("----setSongTitle", musicService.getSongPosition() + "");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_controller);

        songs = (ArrayList<Song>)getIntent().getSerializableExtra("songs");

        Button nextButton = (Button) findViewById(R.id.nextButton);
        Button prevButton = (Button) findViewById(R.id.prevButton);
        Button pauseButton = (Button) findViewById(R.id.pause);

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
                if(!paused) {
                    pause();
                    paused = true;
                }
                else {
                    start();
                    paused = false;
                }
            }
        });


    }

    private void setSongTitle() {

        Song currentSong = songs.get(musicService.getSongPosition());
        songTitle = (TextView) findViewById(R.id.songTitle);
        songTitle.setText(currentSong.getmArtistName() + " - " + currentSong.getmSongName());
    }
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
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
        if(musicService != null && musicBound)
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
