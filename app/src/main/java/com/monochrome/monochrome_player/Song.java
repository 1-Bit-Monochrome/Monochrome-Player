package com.monochrome.monochrome_player;

public class Song {
    private String title;
    private String artist;
    private String path;
    private String album;
    private long dateAdded;

    public Song(String title, String artist, String album, String path, long dateAdded) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.dateAdded = dateAdded;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getPath() { return path; }
    public long getDateAdded() { return dateAdded; }
}
