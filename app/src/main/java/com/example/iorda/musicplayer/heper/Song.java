package com.example.iorda.musicplayer.heper;

import android.net.Uri;
import android.view.View;

import java.io.Serializable;

/**
 * Created by iorda on 3/29/2017.
 */

public class Song implements Serializable {

    private String mSongName, mArtistName, mFullPath, mDuration;
    //private Uri songUri;
    private int songID;



    public Song(String mSongName, String mArtistName, String mDuration, String mFullPath, int songID, Uri songUri) {
        this.mArtistName = mArtistName;
        this.mDuration = mDuration;
        this.mSongName = mSongName;
        this.mFullPath = mFullPath;
        this.songID = songID;
        //this.songUri = songUri;

    }
    public Song() {

    }
    public String getmSongName() {
        return mSongName;
    }

    public void setmSongName(String mSongName) {
        this.mSongName = mSongName;
    }

    public String getmArtistName() {
        return mArtistName;
    }

    public void setmArtistName(String mArtistName) {
        this.mArtistName = mArtistName;
    }

    public String getmDuration() {
        return mDuration;
    }

    public void setmDuration(String mDuration) {
        this.mDuration = mDuration;
    }

    public String getmFullPath() {
        return mFullPath;
    }

    public void setmFullPath(String mFullPath) {
        this.mFullPath = mFullPath;
    }

    /*public Uri getSongUri() {
        return songUri;
    }

    public void setSongUri(Uri songUri) {
        this.songUri = songUri;
    }*/

    public int getSongID() {
        return songID;
    }

    public void setSongID(int songID) {
        this.songID = songID;
    }

}
