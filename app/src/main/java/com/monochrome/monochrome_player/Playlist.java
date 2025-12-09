package com.monochrome.monochrome_player;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;
    private List<String> songPaths;

    public Playlist(String name) {
        this.name = name;
        this.songPaths = new ArrayList<>();
    }

    public Playlist(String name, List<String> songPaths) {
        this.name = name;
        this.songPaths = new ArrayList<>(songPaths);
    }

    public String getName() { return name; }
    public List<String> getSongPaths() { return songPaths; }

    public void addSong(String path) {
        if (path == null) return;
        if (!songPaths.contains(path)) songPaths.add(path);
    }

    public void removeSong(String path) {
        songPaths.remove(path);
    }

    public boolean containsSong(String path) {
        return songPaths.contains(path);
    }

    public int size() { return songPaths.size(); }
}
