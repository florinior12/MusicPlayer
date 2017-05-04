package com.example.iorda.musicplayer.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.example.iorda.musicplayer.R;
import com.example.iorda.musicplayer.activities.MainActivity;
import com.example.iorda.musicplayer.adapters.SongListAdapter;
import com.example.iorda.musicplayer.heper.Song;

import java.util.ArrayList;

/**
 * Created by iorda on 4/17/2017.
 */

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private SongListAdapter adapter;

    private Song playSong = null;
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;

    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public void onCreate() {
        super.onCreate();   //create service
        songPosn = 0;   //initialize position
        player = new MediaPlayer(); //create media player

        initMusicPlayer();  //initialise media player
    }

    public void onDestroy() {
        stopForeground(true);
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);    //play music when locked
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);   //MUISC player, not other type of sounds

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public void playSong() {

        Log.v("!!!!!!!playSong method", songPosn + " pos");

       /* if (playSong != null) { //here playSong is the previous song - if it's null we reset it to default color
            ((TextView)playSong.getSongView().findViewById(R.id.txt_listitem_songname)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.songTitle));
            ((TextView)playSong.getSongView().findViewById(R.id.txt_listitem_artistname)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.songArtist));
        }*/
        //reset the player at each song play
        player.reset();
        //get the song to be played from the list
        playSong = songs.get(songPosn);
        //get the title of the song
        songTitle = playSong.getmSongName();
        //get the id of the current song
        long currSong = playSong.getSongID();
        //set uri of the file to be played
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong
        );

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);

        }

        //((TextView)playSong.getSongView().findViewById(R.id.txt_listitem_songname)).setTextColor(Color.WHITE);
        //((TextView)playSong.getSongView().findViewById(R.id.txt_listitem_artistname)).setTextColor(Color.WHITE);

        Log.v("!!!!!!!playSong method", songPosn + " pos");
        player.prepareAsync();  //prepare the media player for playback

    }

    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();

        //Create a notification for the playing music
        //First create a class which takes us back to the main activity when clicking the notificatio
        Intent notifIntent = new Intent(this, MainActivity.class);
        notifIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Build the notification
        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.default_clipart)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);

        Notification not = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            not = builder.build();
        }

        startForeground(NOTIFY_ID, not);
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public int getSongPosition() {
        return songPosn;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }


    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.start();
    }

    public void playPrev() {    //previous song
        songPosn--;

        //if the positon is negative, we play the last song
        if (songPosn < 0) songPosn = songs.size() - 1;

        playSong();
    }

    public void setSongListAdapter(SongListAdapter adapter) {
        this.adapter = adapter;
    }

    public void playNext() {
        songPosn++;

        if (songPosn >= songs.size())
            songPosn = 0;   //if the position is larger than size of the songs, we go to the first song

        playSong();
    }

}
