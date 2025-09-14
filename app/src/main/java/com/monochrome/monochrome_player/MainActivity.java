package com.monochrome.monochrome_player;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Song> songs;
    private SongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView and adapter
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        songs = new ArrayList<>();
        adapter = new SongAdapter(songs);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(song ->
                Toast.makeText(MainActivity.this, "Selected: " + song.getTitle(), Toast.LENGTH_SHORT).show());

        // Check media permissions and load songs accordingly
        checkPermissionsAndLoadSongs();
    }

    /**
     * Check whether the app has permission to read audio files. Request it if
     * necessary and load songs once granted.
     */
    private void checkPermissionsAndLoadSongs() {
        if (hasReadMediaPermission()) {
            loadSongs();
        } else {
            ActivityResultLauncher<String> requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                        if (isGranted) {
                            loadSongs();
                        } else {
                            Toast.makeText(this, "Permission denied. Cannot load songs.", Toast.LENGTH_SHORT).show();
                        }
                    });
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
            requestPermissionLauncher.launch(permission);
        }
    }

    /**
     * Determine if the appropriate media-read permission has already been granted.
     */
    private boolean hasReadMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Query the system for music files and populate the RecyclerView.
     */
    private void loadSongs() {
        songs.clear();
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.IS_MUSIC
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, projection, selection, null,
                    MediaStore.Audio.Media.TITLE + " ASC");
            if (cursor != null) {
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleIndex);
                    String artist = cursor.getString(artistIndex);
                    String path = cursor.getString(dataIndex);
                    songs.add(new Song(title, artist, path));
                }
            }
        } catch (SecurityException se) {
            Toast.makeText(this, "Permission not granted to read media", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        adapter.notifyDataSetChanged();
    }
}