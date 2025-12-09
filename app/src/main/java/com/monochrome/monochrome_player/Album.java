package com.monochrome.monochrome_player;

public class Album {
    private String name;
    private String artist;
    private int songCount;

    public Album(String name, String artist, int songCount) {
        this.name = name;
        this.artist = artist;
        this.songCount = songCount;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public int getSongCount() {
        return songCount;
    }
}
