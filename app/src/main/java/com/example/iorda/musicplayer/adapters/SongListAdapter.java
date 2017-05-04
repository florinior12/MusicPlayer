package com.example.iorda.musicplayer.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.iorda.musicplayer.R;
import com.example.iorda.musicplayer.heper.Song;

import java.util.ArrayList;

/**
 * Created by iorda on 4/15/2017.
 */

public class SongListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Song> songList;
    private int mSelectedItem = -1;

    public SongListAdapter(Context context, ArrayList<Song> list) {
        mContext = context;
        songList = list;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.song_list_item,null);
        }

//        if (position == mSelectedItem) {
//            ((TextView)convertView.findViewById(R.id.txt_listitem_songname)).setTextColor(ContextCompat.getColor(mContext, R.color.WHITE));
//            ((TextView)convertView.findViewById(R.id.txt_listitem_artistname)).setTextColor(ContextCompat.getColor(mContext, R.color.WHITE));
//
//        }

        ImageView mImgSong = (ImageView) convertView.findViewById(R.id.img_listitem_file);
        TextView mtxtSongName = (TextView) convertView.findViewById(R.id.txt_listitem_songname);
        TextView mtxtArtistName = (TextView) convertView.findViewById(R.id.txt_listitem_artistname);
        TextView mtxtDuration = (TextView) convertView.findViewById(R.id.txt_listitem_duration);

        mImgSong.setImageResource(R.drawable.default_clipart);
        mtxtSongName.setText(songList.get(position).getmSongName());
        mtxtArtistName.setText(songList.get(position).getmArtistName());
        mtxtDuration.setText(songList.get(position).getmDuration());
        convertView.setTag(position);

        //songList.get(position).setSongView(convertView);
        return convertView;
    }



    public void setSongList (ArrayList<Song> list) {
        songList = list;
        this.notifyDataSetChanged();
    }
}
