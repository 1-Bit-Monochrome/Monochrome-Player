package com.monochrome.monochrome_player;

public class ListItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_SONG = 1;
    public static final int TYPE_ARTIST = 2;
    public static final int TYPE_ALBUM = 3;

    private int type;
    private String header;
    private Song song;
    private Artist artist;
    private Album album;
    private int position;

    public static ListItem createHeader(String letter) {
        ListItem item = new ListItem();
        item.type = TYPE_HEADER;
        item.header = letter;
        return item;
    }

    public static ListItem createSong(Song song, int position) {
        ListItem item = new ListItem();
        item.type = TYPE_SONG;
        item.song = song;
        item.position = position;
        return item;
    }

    public static ListItem createArtist(Artist artist, int position) {
        ListItem item = new ListItem();
        item.type = TYPE_ARTIST;
        item.artist = artist;
        item.position = position;
        return item;
    }

    public static ListItem createAlbum(Album album, int position) {
        ListItem item = new ListItem();
        item.type = TYPE_ALBUM;
        item.album = album;
        item.position = position;
        return item;
    }

    public int getType() {
        return type;
    }

    public String getHeader() {
        return header;
    }

    public Song getSong() {
        return song;
    }

    public Artist getArtist() {
        return artist;
    }

    public Album getAlbum() {
        return album;
    }

    public int getPosition() {
        return position;
    }
}
