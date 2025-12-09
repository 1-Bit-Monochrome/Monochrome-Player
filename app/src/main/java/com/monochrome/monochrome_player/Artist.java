package com.monochrome.monochrome_player;

public class Artist {
    private String name;
    private int songCount;

    public Artist(String name, int songCount) {
        this.name = name;
        this.songCount = songCount;
    }

    public String getName() {
        return name;
    }

    public int getSongCount() {
        return songCount;
    }
}
