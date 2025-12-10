package com.monochrome.monochrome_player;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.HapticFeedbackConstants;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.activity.OnBackPressedCallback;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigationrail.NavigationRailView;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final List<Song> songs = new ArrayList<>();
    private final List<Artist> artists = new ArrayList<>();
    private final List<Album> albums = new ArrayList<>();
    private SongAdapter adapter;
    private GenericListAdapter artistAdapter;
    private GenericListAdapter albumAdapter;
    private RecyclerView recyclerView;
    private RecyclerView artistsRecyclerView;
    private RecyclerView albumsRecyclerView;
    private RecyclerView playlistsRecyclerView;
    private View playlistDetailView;
    private TextView playlistDetailTitle;
    private ImageButton playlistDetailBack;
    private ImageButton playlistDetailAddButton;
    private RecyclerView playlistSongsRecyclerView;
    private TextView emptyView;
    private TextView artistsEmptyView;
    private TextView albumsEmptyView;
    private com.google.android.material.floatingactionbutton.FloatingActionButton buttonCreatePlaylist;
    private PlaylistAdapter playlistAdapter;
    private java.util.List<Playlist> playlists = new java.util.ArrayList<>();
    private Playlist currentPlaylistViewing = null;
    private ImageButton playlistAddInTracks;
    // Playback queue: when non-null playback is limited to these song indices (indices into `songs`)
    private java.util.List<Integer> playbackQueue = null;
    private int playbackQueuePos = -1;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> wallpaperPickerLauncher;
    private ActivityResultLauncher<Uri> folderPickerLauncher;
    private View tracksContainer;
    private View nowPlayingContainer;
    private View artistsContainer;
    private View playlistsContainer;
    private View albumsContainer;
    private View settingsContainer;
    private NavigationRailView navigationRailView;
    private ViewGroup alphabetPopupContainer;
    private String currentArtistFilter = null;
    private String currentAlbumFilter = null;
    private SettingsManager settingsManager;
    private ThemeColors currentTheme;
    private com.google.android.material.button.MaterialButton themeAndroidBtn;
    private com.google.android.material.button.MaterialButton themeMaterialYouBtn;
    private com.google.android.material.button.MaterialButton themeMonochromeBtn;
    private com.google.android.material.button.MaterialButton themeBozkurtBtn;
    private com.google.android.material.button.MaterialButton themeMechaBtn;
    private com.google.android.material.button.MaterialButton chooseWallpaperBtn;
    private com.google.android.material.button.MaterialButton clearWallpaperBtn;
    private com.google.android.material.button.MaterialButton sortAlphabeticalBtn;
    private com.google.android.material.button.MaterialButton sortDateAddedBtn;
    private CheckBox includeDownloadsCheckbox;
    private CheckBox includeDocumentsCheckbox;
    private CheckBox includeMusicCheckbox;
    private com.google.android.material.button.MaterialButton buttonAddFolder;
    private LinearLayout folderListContainerView;

    // Player UI - Mini Player
    private View playerSheet;
    private View playerHeader;
    private TextView playerTitleView;
    private TextView playerArtistView;
    private TextView playerAlbumView;
    private TextView collapsedTitleView;
    private ShapeableImageView artworkView;
    private ImageButton shuffleButton;
    private ImageButton backButton;
    private ImageButton playPauseButton;
    private ImageButton forwardButton;
    private ImageButton repeatButton;
    
    private BottomSheetBehavior<View> playerSheetBehavior;
    private ObjectAnimator artworkAnimator;
    private int collapsedPeekHeight = 0;

    // Player UI - Full Player
    private View fullPlayerSheet;
    private TextView fullPlayerTitleView;
    private TextView fullPlayerArtistView;
    private TextView fullPlayerAlbumView;
    private ShapeableImageView fullArtworkView;
    private ImageButton fullShuffleButton;
    private ImageButton fullBackButton;
    private ImageButton fullPlayPauseButton;
    private ImageButton fullForwardButton;
    private ImageButton fullRepeatButton;
    private ImageButton fullPlayerClose;
    private ImageButton fullAddToPlaylistButton;
    private ImageButton fullFavoriteButton;
    private ObjectAnimator fullArtworkAnimator;
    
    private ir.mahozad.multiplatform.wavyslider.WavySlider miniSeekBar;
    private ir.mahozad.multiplatform.wavyslider.WavySlider fullSeekBar;
    
    private TextView aboutSectionHeader;
    private TextView aboutAppName;
    private TextView aboutVersion;
    private TextView aboutDescription;
    private boolean isAboutExpanded = false;
    
    // Background views for theming
    private View mainContentView;
    private View playerSheetCard;

    // Playback
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = -1;
    private boolean isRepeatEnabled = false;
    private boolean isShuffleEnabled = false;
    private final Random random = new Random();

    // Double-tap detection
    private final Handler clickHandler = new Handler(Looper.getMainLooper());
    private boolean backClickPending = false;
    private boolean forwardClickPending = false;
    private static final long DOUBLE_TAP_WINDOW_MS = 350L;
    private final Runnable backSingleAction = () -> {
        backClickPending = false;
        seekBy(-10_000);
    };
    private final Runnable forwardSingleAction = () -> {
        forwardClickPending = false;
        seekBy(10_000);
    };

    private final Handler seekBarUpdateHandler = new Handler(Looper.getMainLooper());
    private final Runnable seekBarUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPos = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                if (duration > 0) {
                    float progress = (float) currentPos / duration;
                    if (miniSeekBar != null) miniSeekBar.setValue(progress);
                    if (fullSeekBar != null) fullSeekBar.setValue(progress);
                }
            }
            seekBarUpdateHandler.postDelayed(this, 100);
        }
    };

    // Notification / media session
    private static final String ACTION_TOGGLE = "com.monochrome.monochrome_player.ACTION_TOGGLE";
    private static final String ACTION_NEXT = "com.monochrome.monochrome_player.ACTION_NEXT";
    private static final String ACTION_PREV = "com.monochrome.monochrome_player.ACTION_PREV";
    private static final String ACTION_CLOSE = "com.monochrome.monochrome_player.ACTION_CLOSE";
    private static final String CHANNEL_ID = "player_channel";
    private static final int NOTIFICATION_ID = 42;
    private MediaSessionCompat mediaSession;
    private NotificationManagerCompat notificationManager;
    private BroadcastReceiver notificationActionReceiver;

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Apply theme before setting content view
        settingsManager = new SettingsManager(this);
        applyThemeColors();
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navigationRailView = findViewById(R.id.navigation_rail);
        tracksContainer = findViewById(R.id.tracks_container);
        nowPlayingContainer = findViewById(R.id.now_playing_container);
        artistsContainer = findViewById(R.id.artists_container);
        playlistsContainer = findViewById(R.id.playlists_container);
        albumsContainer = findViewById(R.id.albums_container);
        settingsContainer = findViewById(R.id.settings_container);
        alphabetPopupContainer = findViewById(R.id.alphabet_popup_container);
        
        // Settings buttons
        themeAndroidBtn = findViewById(R.id.theme_android);
        themeMaterialYouBtn = findViewById(R.id.theme_material_you);
        themeMonochromeBtn = findViewById(R.id.theme_monochrome);
        themeBozkurtBtn = findViewById(R.id.theme_bozkurt);
        themeMechaBtn = findViewById(R.id.theme_mecha);
        chooseWallpaperBtn = findViewById(R.id.choose_wallpaper);
        clearWallpaperBtn = findViewById(R.id.clear_wallpaper);
        sortAlphabeticalBtn = findViewById(R.id.sort_alphabetical);
        sortDateAddedBtn = findViewById(R.id.sort_date_added);
        
        aboutSectionHeader = findViewById(R.id.about_section_header);
        aboutAppName = findViewById(R.id.about_app_name);
        aboutVersion = findViewById(R.id.about_version);
        aboutDescription = findViewById(R.id.about_description);
        
        if (aboutSectionHeader != null) {
            aboutSectionHeader.setOnClickListener(v -> toggleAboutSection());
        }
        
        mainContentView = findViewById(R.id.tracks_container);
        playerSheetCard = findViewById(R.id.player_sheet);
        
        setupSettingsButtons();

        // Initialize RecyclerViews
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);

        artistsRecyclerView = findViewById(R.id.artists_recycler_view);
        artistsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        artistsRecyclerView.setNestedScrollingEnabled(false);

        albumsRecyclerView = findViewById(R.id.albums_recycler_view);
        albumsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        albumsRecyclerView.setNestedScrollingEnabled(false);

        // Playlists UI
        playlistsRecyclerView = findViewById(R.id.playlists_recycler_view);
        if (playlistsRecyclerView != null) {
            playlistsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            playlistsRecyclerView.setNestedScrollingEnabled(false);
        }
        playlistSongsRecyclerView = findViewById(R.id.playlist_songs_recycler);
        if (playlistSongsRecyclerView != null) {
            playlistSongsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            playlistSongsRecyclerView.setNestedScrollingEnabled(false);
        }
        buttonCreatePlaylist = findViewById(R.id.button_create_playlist);
        playlistAddInTracks = findViewById(R.id.playlist_add_in_tracks);
        playlistDetailView = findViewById(R.id.playlist_detail);
        playlistDetailTitle = findViewById(R.id.playlist_detail_title);
        playlistDetailBack = findViewById(R.id.playlist_detail_back);
        playlistDetailAddButton = findViewById(R.id.playlist_detail_add);

        emptyView = findViewById(R.id.empty_view);
        artistsEmptyView = findViewById(R.id.artists_empty_view);
        albumsEmptyView = findViewById(R.id.albums_empty_view);

        // Load persisted playlists
        loadPlaylists();
        if (buttonCreatePlaylist != null) {
            buttonCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog());
        }
        if (playlistAddInTracks != null) {
            playlistAddInTracks.setOnClickListener(v -> {
                if (currentPlaylistViewing != null) {
                    showAddToPlaylistDialogForPlaylist(currentPlaylistViewing);
                } else {
                    // fallback: add current playing song to chosen playlist
                    showAddToPlaylistDialogForCurrentSong();
                }
            });
        }
        if (playlistDetailBack != null) {
            playlistDetailBack.setOnClickListener(v -> showPlaylistListView());
        }
        if (playlistDetailAddButton != null) {
            playlistDetailAddButton.setOnClickListener(v -> {
                if (currentPlaylistViewing != null) showAddToPlaylistDialogForPlaylist(currentPlaylistViewing);
            });
        }
        
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        loadSongs();
                        loadArtists();
                        loadAlbums();
                    } else {
                        showEmptyState(R.string.empty_state_no_permission);
                        Toast.makeText(this, "Permission denied. Cannot load songs.", Toast.LENGTH_SHORT).show();
                    }
                });

        wallpaperPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                settingsManager.setWallpaperUriForTheme(settingsManager.getTheme(), uri.toString());
            }
            applyThemeDynamically();
            updateSettingsButtonStates();
        });

        // Folder picker for user-selected folders (Storage Access Framework)
        folderPickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
            if (uri != null) {
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                try {
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);
                } catch (SecurityException se) {
                    // ignore if cannot take persistable permission
                }
                settingsManager.addIncludedFolder(uri.toString());
                refreshFolderList();
                loadSongs();
                loadArtists();
                loadAlbums();
            }
        });

        // Folder settings UI bindings
        includeDownloadsCheckbox = findViewById(R.id.include_downloads_checkbox);
        includeDocumentsCheckbox = findViewById(R.id.include_documents_checkbox);
        includeMusicCheckbox = findViewById(R.id.include_music_checkbox);
        buttonAddFolder = findViewById(R.id.button_add_folder);
        folderListContainerView = findViewById(R.id.folder_list_container);

        includeDownloadsCheckbox.setChecked(settingsManager.isIncludeDownloads());
        includeDocumentsCheckbox.setChecked(settingsManager.isIncludeDocuments());
        includeMusicCheckbox.setChecked(settingsManager.isIncludeMusic());

        includeDownloadsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setIncludeDownloads(isChecked);
            loadSongs();
        });

        includeDocumentsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setIncludeDocuments(isChecked);
            loadSongs();
        });

        includeMusicCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsManager.setIncludeMusic(isChecked);
            loadSongs();
        });

        buttonAddFolder.setOnClickListener(v -> folderPickerLauncher.launch(null));

        refreshFolderList();

        



        setupMediaSessionAndNotifications();
        setupPlayerSheet();
        setupBackHandling();

        navigationRailView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_now_playing) {
                showNowPlaying();
                return true;
            } else if (itemId == R.id.nav_tracks) {
                showSection(tracksContainer);
                return true;
            } else if (itemId == R.id.nav_artists) {
                showSection(artistsContainer);
                return true;
            } else if (itemId == R.id.nav_playlists) {
                showSection(playlistsContainer);
                return true;
            } else if (itemId == R.id.nav_albums) {
                showSection(albumsContainer);
                return true;
            } else if (itemId == R.id.nav_settings) {
                showSection(settingsContainer);
                return true;
            }
            return false;
        });
        navigationRailView.setSelectedItemId(R.id.nav_tracks);
        showSection(tracksContainer);

        checkPermissionsAndLoadSongs();
    }

    private void checkPermissionsAndLoadSongs() {
        if (hasReadMediaPermission()) {
            loadSongs();
            loadArtists();
            loadAlbums();
        } else {
            String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                    Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
            showEmptyState(R.string.empty_state_no_permission);
            requestPermissionLauncher.launch(permission);
        }
    }

    private boolean hasReadMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void loadSongs() {
        songs.clear();
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.IS_MUSIC
        };
        // Restrict to common user folders: Download, Documents, Music (external storage)
        String externalRoot = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        java.util.List<String> patterns = new java.util.ArrayList<>();
        if (settingsManager.isIncludeDownloads()) {
            patterns.add(externalRoot + "/Download/%");
        }
        if (settingsManager.isIncludeDocuments()) {
            patterns.add(externalRoot + "/Documents/%");
        }
        if (settingsManager.isIncludeMusic()) {
            patterns.add(externalRoot + "/Music/%");
        }

        String selection;
        String[] selectionArgs;
        if (patterns.isEmpty()) {
            // If none of the defaults are enabled, query only by IS_MUSIC
            selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            selectionArgs = new String[]{};
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(MediaStore.Audio.Media.IS_MUSIC).append(" != 0 AND (");
            for (int i = 0; i < patterns.size(); i++) {
                if (i > 0) sb.append(" OR ");
                sb.append(MediaStore.Audio.Media.DATA).append(" LIKE ?");
            }
            sb.append(")");
            selection = sb.toString();
            selectionArgs = patterns.toArray(new String[0]);
        }

        String sortOrder = settingsManager.getSortMode().equals(SettingsManager.SORT_DATE_ADDED)
            ? MediaStore.Audio.Media.DATE_ADDED + " DESC"
            : MediaStore.Audio.Media.TITLE + " ASC";

        

        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);
            if (cursor != null) {
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);
                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleIndex);
                    String artist = cursor.getString(artistIndex);
                    String album = cursor.getString(albumIndex);
                    String path = cursor.getString(dataIndex);
                    long dateAdded = cursor.getLong(dateIndex);
                    songs.add(new Song(title, artist, album, path, dateAdded));
                }
            }
        } catch (SecurityException se) {
            Toast.makeText(this, "Permission not granted to read media", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        List<ListItem> listItems = buildListWithHeaders();
        adapter = new SongAdapter(listItems);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((song, position) -> openSong(position));
        adapter.setOnHeaderClickListener(this::showAlphabetPopup);
        
        if (songs.isEmpty()) {
            showEmptyState(R.string.empty_state_no_music);
        } else {
            hideEmptyState();
        }
        playerSheet.post(this::ensurePlayerSheetVisible);
    }

    private void refreshFolderList() {
        if (folderListContainerView == null) return;
        folderListContainerView.removeAllViews();
        java.util.Set<String> included = settingsManager.getIncludedFolders();
        if (included == null || included.isEmpty()) {
            TextView hint = new TextView(this);
            hint.setText("No custom folders added.");
            folderListContainerView.addView(hint);
            return;
        }

        for (String uriString : included) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView tv = new TextView(this);
            String title = uriString;
            try {
                Uri u = Uri.parse(uriString);
                DocumentFile doc = DocumentFile.fromTreeUri(this, u);
                if (doc != null && doc.getName() != null) title = doc.getName();
            } catch (Exception ignored) {}
            tv.setText(title);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            com.google.android.material.button.MaterialButton remove = new com.google.android.material.button.MaterialButton(this);
            remove.setText("Remove");
            remove.setOnClickListener(v -> {
                settingsManager.removeIncludedFolder(uriString);
                refreshFolderList();
                loadSongs();
            });

            row.addView(tv);
            row.addView(remove);
            folderListContainerView.addView(row);
        }
    }

    private List<ListItem> buildListWithHeaders() {
        List<ListItem> items = new ArrayList<>();
        if (songs.isEmpty()) {
            return items;
        }
        
        boolean isSortedByDate = settingsManager.getSortMode().equals(SettingsManager.SORT_DATE_ADDED);
        
        if (isSortedByDate) {
            // Group by month/year
            String currentMonthYear = null;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault());
            
            for (int i = 0; i < songs.size(); i++) {
                Song song = songs.get(i);
                String monthYear = sdf.format(new java.util.Date(song.getDateAdded() * 1000L));
                
                if (!monthYear.equals(currentMonthYear)) {
                    currentMonthYear = monthYear;
                    items.add(ListItem.createHeader(currentMonthYear));
                }
                items.add(ListItem.createSong(song, i));
            }
        } else {
            // Group by first letter (A-Z)
            String currentLetter = null;
            for (int i = 0; i < songs.size(); i++) {
                Song song = songs.get(i);
                String title = song.getTitle();
                if (title != null && !title.isEmpty()) {
                    String firstLetter = title.substring(0, 1).toUpperCase();
                    if (Character.isLetter(firstLetter.charAt(0))) {
                        if (!firstLetter.equals(currentLetter)) {
                            currentLetter = firstLetter;
                            items.add(ListItem.createHeader(currentLetter));
                        }
                    }
                }
                items.add(ListItem.createSong(song, i));
            }
        }
        return items;
    }

    private void showAlphabetPopup() {
        if (alphabetPopupContainer == null || adapter == null) return;
        
        java.util.Set<String> availableLetters = new java.util.HashSet<>();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ListItem item = adapter.getItemAtPosition(i);
            if (item.getType() == ListItem.TYPE_HEADER) {
                availableLetters.add(item.getHeader());
            }
        }
        
        if (availableLetters.isEmpty()) return;
        
        alphabetPopupContainer.removeAllViews();
        
        View popupView = getLayoutInflater().inflate(R.layout.alphabet_popup, alphabetPopupContainer, false);
        androidx.gridlayout.widget.GridLayout grid = popupView.findViewById(R.id.alphabet_grid);
        
        java.util.List<String> sortedLetters = new java.util.ArrayList<>(availableLetters);
        java.util.Collections.sort(sortedLetters);
        
        for (String letter : sortedLetters) {
            TextView letterView = new TextView(this);
            letterView.setText(letter);
            letterView.setGravity(Gravity.CENTER);
            letterView.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
            letterView.setTextSize(18f);
            letterView.setTextColor(currentTheme != null ? currentTheme.accentColor : Color.WHITE);
            letterView.setTypeface(null, android.graphics.Typeface.BOLD);
            letterView.setClickable(true);
            letterView.setFocusable(true);
            letterView.setBackground(ContextCompat.getDrawable(this, android.R.drawable.list_selector_background));
            
            letterView.setOnClickListener(v -> {
                scrollToLetter(letter);
                hideAlphabetPopup();
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            });
            
            grid.addView(letterView);
        }
        
        alphabetPopupContainer.addView(popupView);
        alphabetPopupContainer.setVisibility(View.VISIBLE);
        alphabetPopupContainer.setAlpha(0f);
        alphabetPopupContainer.animate().alpha(1f).setDuration(200).start();
        
        alphabetPopupContainer.setOnClickListener(v -> hideAlphabetPopup());
    }
    
    private void hideAlphabetPopup() {
        if (alphabetPopupContainer == null) return;
        alphabetPopupContainer.animate().alpha(0f).setDuration(150)
                .withEndAction(() -> {
                    alphabetPopupContainer.setVisibility(View.GONE);
                    alphabetPopupContainer.removeAllViews();
                }).start();
    }

    private void scrollToLetter(String letter) {
        if (recyclerView == null || adapter == null || adapter.getItemCount() == 0) return;
        
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ListItem item = adapter.getItemAtPosition(i);
            if (item.getType() == ListItem.TYPE_HEADER && letter.equals(item.getHeader())) {
                recyclerView.scrollToPosition(i);
                return;
            }
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        clickHandler.removeCallbacks(backSingleAction);
        clickHandler.removeCallbacks(forwardSingleAction);
        if (notificationActionReceiver != null) {
            unregisterReceiver(notificationActionReceiver);
            notificationActionReceiver = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void setupPlayerSheet() {
        playerSheet = findViewById(R.id.player_sheet);
        playerHeader = findViewById(R.id.player_header);
        playerTitleView = findViewById(R.id.player_title);
        playerArtistView = findViewById(R.id.player_artist);
        playerAlbumView = findViewById(R.id.player_album);
        collapsedTitleView = findViewById(R.id.player_collapsed_title);
        artworkView = findViewById(R.id.artwork_view);
        shuffleButton = findViewById(R.id.button_shuffle);
        backButton = findViewById(R.id.button_back);
        playPauseButton = findViewById(R.id.button_play_pause);
        forwardButton = findViewById(R.id.button_forward);
        repeatButton = findViewById(R.id.button_repeat);
        miniSeekBar = findViewById(R.id.mini_wavy_seekbar);

        if (miniSeekBar != null) {
            miniSeekBar.setOnValueChangeListener((oldValue, newValue) -> {
                if (mediaPlayer != null) {
                    int duration = mediaPlayer.getDuration();
                    if (duration > 0) {
                        mediaPlayer.seekTo((int)(newValue * duration));
                    }
                }
                return null;
            });
            miniSeekBar.setWaveAnimating(false);
        }

        playerSheetBehavior = BottomSheetBehavior.from(playerSheet);
        playerSheetBehavior.setHideable(false);
        playerSheetBehavior.setSkipCollapsed(false);
        playerSheetBehavior.setSaveFlags(BottomSheetBehavior.SAVE_NONE);
        playerSheetBehavior.setDraggable(true);
        playerSheetBehavior.setPeekHeight(dpToPx(88), false);
        playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        
        playerSheet.post(() -> {
            updateCollapsedPeekHeight();
            if (collapsedPeekHeight > 0) {
                playerSheetBehavior.setPeekHeight(collapsedPeekHeight, false);
            }
        });
        playerSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    playerSheetBehavior.setSkipCollapsed(true);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    playerSheetBehavior.setSkipCollapsed(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        playerSheet.setVisibility(View.VISIBLE);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) playerSheet.getLayoutParams();
        lp.gravity = Gravity.BOTTOM;
        playerSheet.setLayoutParams(lp);

        playerSheet.setOnClickListener(v -> togglePlayerSheet());
        playerHeader.setOnClickListener(v -> togglePlayerSheet());
        playerTitleView.setVisibility(View.VISIBLE);
        artworkView.setOnClickListener(v -> {
            // Don't navigate if already on Now Playing
            if (nowPlayingContainer != null && nowPlayingContainer.getVisibility() == View.VISIBLE) {
                return;
            }
            navigationRailView.setSelectedItemId(R.id.nav_now_playing);
            showNowPlaying();
        });
        playerArtistView.setOnClickListener(v -> openArtistView());
        playerAlbumView.setOnClickListener(v -> openAlbumsView());

        shuffleButton.setOnClickListener(v -> toggleShuffle());
        backButton.setOnClickListener(v -> handleBackPress());
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        forwardButton.setOnClickListener(v -> handleForwardPress());
        repeatButton.setOnClickListener(v -> toggleRepeat());
        updateControlTint(shuffleButton, false);
        updateControlTint(repeatButton, false);

        setupFullPlayer();
    }

    private void setupFullPlayer() {
        fullPlayerSheet = findViewById(R.id.full_player_sheet);
        fullPlayerTitleView = findViewById(R.id.full_player_title);
        fullPlayerArtistView = findViewById(R.id.full_player_artist);
        fullSeekBar = findViewById(R.id.full_wavy_seekbar);

        if (fullSeekBar != null) {
            fullSeekBar.setOnValueChangeListener((oldValue, newValue) -> {
                if (mediaPlayer != null) {
                    int duration = mediaPlayer.getDuration();
                    if (duration > 0) {
                        mediaPlayer.seekTo((int)(newValue * duration));
                    }
                }
                return null;
            });
        }
        if (fullSeekBar != null) {
            fullSeekBar.setWaveAnimating(false);
        }
        fullPlayerAlbumView = findViewById(R.id.full_player_album);
        fullArtworkView = findViewById(R.id.full_artwork_view);
        fullShuffleButton = findViewById(R.id.full_button_shuffle);
        fullBackButton = findViewById(R.id.full_button_back);
        fullPlayPauseButton = findViewById(R.id.full_button_play_pause);
        fullForwardButton = findViewById(R.id.full_button_forward);
        fullRepeatButton = findViewById(R.id.full_button_repeat);
        fullAddToPlaylistButton = findViewById(R.id.full_button_add_to_playlist);
        fullFavoriteButton = findViewById(R.id.full_button_favorite);
        fullPlayerClose = findViewById(R.id.full_player_close);

        fullPlayerArtistView.setOnClickListener(v -> openArtistView());
        fullPlayerAlbumView.setOnClickListener(v -> openAlbumsView());
        fullPlayerClose.setOnClickListener(v -> {
            navigationRailView.setSelectedItemId(R.id.nav_tracks);
            showSection(tracksContainer);
        });
        fullShuffleButton.setOnClickListener(v -> toggleShuffle());
        fullBackButton.setOnClickListener(v -> handleBackPress());
        fullPlayPauseButton.setOnClickListener(v -> togglePlayPause());
        fullForwardButton.setOnClickListener(v -> handleForwardPress());
        if (fullAddToPlaylistButton != null) {
            fullAddToPlaylistButton.setOnClickListener(v -> {
                if (currentSongIndex >= 0 && currentSongIndex < songs.size()) {
                    Song song = songs.get(currentSongIndex);
                    showSimplePlaylistPicker(p -> {
                        if (p.containsSong(song.getPath())) {
                            Toast.makeText(this, "Already in playlist", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        p.addSong(song.getPath());
                        settingsManager.updatePlaylists(playlists);
                        // visual-only feedback handled elsewhere; no toast here
                    });
                }
            });
        }
        if (fullFavoriteButton != null) {
            fullFavoriteButton.setOnClickListener(v -> {
                if (currentSongIndex < 0 || currentSongIndex >= songs.size()) return;
                Playlist fav = getFavoritesPlaylist();
                String path = songs.get(currentSongIndex).getPath();
                if (fav.containsSong(path)) {
                    fav.removeSong(path);
                } else {
                    fav.addSong(path);
                }
                settingsManager.updatePlaylists(playlists);
                updateFavoriteButtonState();
            });
        }
        fullRepeatButton.setOnClickListener(v -> toggleRepeat());

        updateControlTint(fullShuffleButton, false);
        updateControlTint(fullRepeatButton, false);
    }

    private void setupSettingsButtons() {
        updateSettingsButtonStates();
        
        themeAndroidBtn.setOnClickListener(v -> {
            settingsManager.setTheme(SettingsManager.THEME_ANDROID);
            applyThemeDynamically();
            updateSettingsButtonStates();
        });
        
        themeMaterialYouBtn.setOnClickListener(v -> {
            settingsManager.setTheme(SettingsManager.THEME_MATERIAL_YOU);
            applyThemeDynamically();
            updateSettingsButtonStates();
        });
        
        themeMonochromeBtn.setOnClickListener(v -> {
            settingsManager.setTheme(SettingsManager.THEME_MONOCHROME);
            applyThemeDynamically();
            updateSettingsButtonStates();
        });

        themeBozkurtBtn.setOnClickListener(v -> {
            settingsManager.setTheme(SettingsManager.THEME_BOZKURT);
            applyThemeDynamically();
            updateSettingsButtonStates();
        });
        
        themeMechaBtn.setOnClickListener(v -> {
            settingsManager.setTheme(SettingsManager.THEME_MECHA);
            applyThemeDynamically();
            updateSettingsButtonStates();
        });
        
        chooseWallpaperBtn.setOnClickListener(v -> wallpaperPickerLauncher.launch("image/*"));

        clearWallpaperBtn.setOnClickListener(v -> {
            String current = settingsManager.getTheme();
            settingsManager.setWallpaperUriForTheme(current, null);
            applyThemeDynamically();
            updateSettingsButtonStates();
        });
        
        sortAlphabeticalBtn.setOnClickListener(v -> {
            settingsManager.setSortMode(SettingsManager.SORT_ALPHABETICAL);
            reloadSongs();
            updateSettingsButtonStates();
        });
        
        sortDateAddedBtn.setOnClickListener(v -> {
            settingsManager.setSortMode(SettingsManager.SORT_DATE_ADDED);
            reloadSongs();
            updateSettingsButtonStates();
        });
    }
    
    private void updateSettingsButtonStates() {
        String currentThemeStr = settingsManager.getTheme();
        
        // Style active/inactive theme buttons
        styleSettingsButton(themeAndroidBtn, currentThemeStr.equals(SettingsManager.THEME_ANDROID));
        styleSettingsButton(themeMaterialYouBtn, currentThemeStr.equals(SettingsManager.THEME_MATERIAL_YOU));
        styleSettingsButton(themeMonochromeBtn, currentThemeStr.equals(SettingsManager.THEME_MONOCHROME));
        styleSettingsButton(themeBozkurtBtn, currentThemeStr.equals(SettingsManager.THEME_BOZKURT));
        styleSettingsButton(themeMechaBtn, currentThemeStr.equals(SettingsManager.THEME_MECHA));
        
        String currentSort = settingsManager.getSortMode();
        styleSettingsButton(sortAlphabeticalBtn, currentSort.equals(SettingsManager.SORT_ALPHABETICAL));
        styleSettingsButton(sortDateAddedBtn, currentSort.equals(SettingsManager.SORT_DATE_ADDED));
        // Enable/disable clear wallpaper button depending on whether a wallpaper exists for current theme
        if (clearWallpaperBtn != null) {
            String wallpaperForCurrent = settingsManager.getWallpaperUriForTheme(currentThemeStr);
            clearWallpaperBtn.setEnabled(wallpaperForCurrent != null);
        }
    }
    
    private void styleSettingsButton(com.google.android.material.button.MaterialButton button, boolean isActive) {
        if (button == null || currentTheme == null) return;
        
        if (isActive) {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentTheme.accentColor));
            button.setTextColor(currentTheme.onSurfaceColor);
            button.setAlpha(1.0f);
            button.setStrokeWidth(0);
        } else {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentTheme.surfaceColor));
            button.setTextColor(currentTheme.onSurfaceVariantColor);
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(currentTheme.onSurfaceVariantColor));
            button.setStrokeWidth(2);
            button.setAlpha(0.8f);
        }
    }
    
    private void applyThemeDynamically() {
        currentTheme = ThemeColors.getThemeColors(this, settingsManager.getTheme());
        
        // Fade out slightly before applying theme
        View decorView = getWindow().getDecorView();
        decorView.animate()
                .alpha(0.85f)
                .setDuration(100)
                .withEndAction(() -> {
                    // Apply theme changes
                    android.view.Window window = getWindow();
                    window.setStatusBarColor(currentTheme.backgroundColor);
                    window.setNavigationBarColor(currentTheme.backgroundColor);
                    
                    // Update navigation rail
                    if (navigationRailView != null) {
                        navigationRailView.setItemActiveIndicatorColor(android.content.res.ColorStateList.valueOf(currentTheme.accentColor));
                        navigationRailView.setItemIconTintList(android.content.res.ColorStateList.valueOf(currentTheme.onSurfaceVariantColor));
                        navigationRailView.setItemTextColor(android.content.res.ColorStateList.valueOf(currentTheme.onSurfaceVariantColor));
                    }
                    
                    // Update player sheet
                    applyThemeToPlayer();
                    
                    // Reload artwork to apply theme-specific changes
                    if (currentSongIndex >= 0 && currentSongIndex < songs.size()) {
                        loadArtwork(songs.get(currentSongIndex));
                    }
                    
                    // Update all buttons in settings
                    updateSettingsButtonStates();
                    
                    if (adapter != null) adapter.setTheme(currentTheme);
                    if (artistAdapter != null) artistAdapter.setTheme(currentTheme);
                    if (albumAdapter != null) albumAdapter.setTheme(currentTheme);
                    
                    // Fade back in
                    decorView.animate()
                            .alpha(1.0f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }
    
    private void applyThemeToPlayer() {
        if (currentTheme == null) return;
        
        // Update backgrounds
        if (mainContentView != null) {
            String currentThemeKey = settingsManager.getTheme();
            String wallpaperUri = settingsManager.getWallpaperUriForTheme(currentThemeKey);
            Drawable d = null;
            if (wallpaperUri != null) {
                d = loadWallpaperDrawable(wallpaperUri);
            }
            // If none set and current theme is BOZKURT, try to load bundled/asset default
                if (d == null && SettingsManager.THEME_BOZKURT.equals(currentThemeKey)) {
                    // Try assets/wallpapers/... (place the file at app/src/main/assets/wallpapers/<name>)
                    d = loadWallpaperDrawableFromAsset("wallpapers/29-295219_arka-plan-wallpaper-2761364703.jpg");
                }
                // If still null and Mecha theme, try Mecha asset
                if (d == null && SettingsManager.THEME_MECHA.equals(currentThemeKey)) {
                    d = loadWallpaperDrawableFromAsset("wallpapers/NGEwallpaper.jpg");
                }
            if (d != null) {
                mainContentView.setBackground(d);
            } else {
                mainContentView.setBackgroundColor(currentTheme.backgroundColor);
            }
        }
        if (playerSheetCard != null) playerSheetCard.setBackgroundColor(currentTheme.surfaceColor);
        if (fullPlayerSheet != null) fullPlayerSheet.setBackgroundColor(currentTheme.surfaceColor);
        
        // Update player controls tint
        updateControlTint(shuffleButton, isShuffleEnabled);
        updateControlTint(repeatButton, isRepeatEnabled);
        updateControlTint(fullShuffleButton, isShuffleEnabled);
        updateControlTint(fullRepeatButton, isRepeatEnabled);
        
        // Update mini player text colors
        if (playerTitleView != null) playerTitleView.setTextColor(currentTheme.onSurfaceColor);
        if (collapsedTitleView != null) collapsedTitleView.setTextColor(currentTheme.onSurfaceColor);
        if (playerArtistView != null) playerArtistView.setTextColor(currentTheme.accentColor);
        if (playerAlbumView != null) playerAlbumView.setTextColor(currentTheme.accentColor);
        
        // Update full player text colors
        if (fullPlayerTitleView != null) fullPlayerTitleView.setTextColor(currentTheme.onSurfaceColor);
        if (fullPlayerArtistView != null) fullPlayerArtistView.setTextColor(currentTheme.accentColor);
        if (fullPlayerAlbumView != null) fullPlayerAlbumView.setTextColor(currentTheme.accentColor);
        
        // Update control button tints
        applyControlButtonTint(playPauseButton);
        applyControlButtonTint(backButton);
        applyControlButtonTint(forwardButton);
        applyControlButtonTint(fullPlayPauseButton);
        applyControlButtonTint(fullBackButton);
        applyControlButtonTint(fullForwardButton);
        applyControlButtonTint(fullPlayerClose);
        
        if (miniSeekBar != null) {
            miniSeekBar.setWaveColor(currentTheme.accentColor);
            miniSeekBar.setActiveTrackColor(currentTheme.accentColor);
            miniSeekBar.setInactiveTrackColor(currentTheme.surfaceColor);
            miniSeekBar.setThumbColor(currentTheme.accentColor);
        }
        if (fullSeekBar != null) {
            fullSeekBar.setWaveColor(currentTheme.accentColor);
            fullSeekBar.setActiveTrackColor(currentTheme.accentColor);
            fullSeekBar.setInactiveTrackColor(currentTheme.surfaceColor);
            fullSeekBar.setThumbColor(currentTheme.accentColor);
        }
        
        if (aboutSectionHeader != null) aboutSectionHeader.setTextColor(currentTheme.accentColor);
    }

    private Drawable loadWallpaperDrawable(String uriStr) {
        if (uriStr == null) return null;
        try {
            Uri uri = Uri.parse(uriStr);
            ContentResolver resolver = getContentResolver();

            // Determine target size (use view size if available, otherwise screen size)
            int targetW = getResources().getDisplayMetrics().widthPixels;
            int targetH = getResources().getDisplayMetrics().heightPixels;
            if (mainContentView != null && mainContentView.getWidth() > 0 && mainContentView.getHeight() > 0) {
                targetW = mainContentView.getWidth();
                targetH = mainContentView.getHeight();
            }

            // First decode bounds to compute a sample size
            BitmapFactory.Options boundsOpts = new BitmapFactory.Options();
            boundsOpts.inJustDecodeBounds = true;
            try (java.io.InputStream is1 = resolver.openInputStream(uri)) {
                if (is1 == null) return null;
                BitmapFactory.decodeStream(is1, null, boundsOpts);
            }

            int srcW = boundsOpts.outWidth;
            int srcH = boundsOpts.outHeight;
            if (srcW <= 0 || srcH <= 0) return null;

            int inSampleSize = 1;
            while ((srcW / inSampleSize) > targetW * 2 || (srcH / inSampleSize) > targetH * 2) {
                inSampleSize *= 2;
            }

            BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
            decodeOpts.inSampleSize = inSampleSize;
            decodeOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;

            try (java.io.InputStream is2 = resolver.openInputStream(uri)) {
                if (is2 == null) return null;
                Bitmap bmp = BitmapFactory.decodeStream(is2, null, decodeOpts);
                if (bmp == null) return null;

                // Scale up to cover target while preserving aspect ratio (centerCrop)
                float scale = Math.max((float) targetW / bmp.getWidth(), (float) targetH / bmp.getHeight());
                int scaledW = Math.max(1, Math.round(bmp.getWidth() * scale));
                int scaledH = Math.max(1, Math.round(bmp.getHeight() * scale));
                Bitmap scaled = Bitmap.createScaledBitmap(bmp, scaledW, scaledH, true);

                // Crop center to target size
                int x = Math.max(0, (scaledW - targetW) / 2);
                int y = Math.max(0, (scaledH - targetH) / 2);
                int cw = Math.min(targetW, scaled.getWidth() - x);
                int ch = Math.min(targetH, scaled.getHeight() - y);
                Bitmap cropped = Bitmap.createBitmap(scaled, x, y, cw, ch);

                if (cropped != scaled) scaled.recycle();
                if (bmp != cropped && !bmp.isRecycled()) bmp.recycle();

                return new BitmapDrawable(getResources(), cropped);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Drawable loadWallpaperDrawableFromAsset(String assetPath) {
        if (assetPath == null) return null;
        try {
            android.content.res.AssetManager am = getAssets();
            try (java.io.InputStream is = am.open(assetPath)) {
                if (is == null) return null;

                int targetW = getResources().getDisplayMetrics().widthPixels;
                int targetH = getResources().getDisplayMetrics().heightPixels;
                if (mainContentView != null && mainContentView.getWidth() > 0 && mainContentView.getHeight() > 0) {
                    targetW = mainContentView.getWidth();
                    targetH = mainContentView.getHeight();
                }

                BitmapFactory.Options boundsOpts = new BitmapFactory.Options();
                boundsOpts.inJustDecodeBounds = true;
                // Need to reopen stream to read bounds, so read into a byte array first
                byte[] data = readAllBytesFromStream(is);
                if (data == null) return null;
                BitmapFactory.decodeByteArray(data, 0, data.length, boundsOpts);

                int srcW = boundsOpts.outWidth;
                int srcH = boundsOpts.outHeight;
                if (srcW <= 0 || srcH <= 0) return null;

                int inSampleSize = 1;
                while ((srcW / inSampleSize) > targetW * 2 || (srcH / inSampleSize) > targetH * 2) {
                    inSampleSize *= 2;
                }

                BitmapFactory.Options decodeOpts = new BitmapFactory.Options();
                decodeOpts.inSampleSize = inSampleSize;
                decodeOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;

                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, decodeOpts);
                if (bmp == null) return null;

                float scale = Math.max((float) targetW / bmp.getWidth(), (float) targetH / bmp.getHeight());
                int scaledW = Math.max(1, Math.round(bmp.getWidth() * scale));
                int scaledH = Math.max(1, Math.round(bmp.getHeight() * scale));
                Bitmap scaled = Bitmap.createScaledBitmap(bmp, scaledW, scaledH, true);

                int x = Math.max(0, (scaledW - targetW) / 2);
                int y = Math.max(0, (scaledH - targetH) / 2);
                int cw = Math.min(targetW, scaled.getWidth() - x);
                int ch = Math.min(targetH, scaled.getHeight() - y);
                Bitmap cropped = Bitmap.createBitmap(scaled, x, y, cw, ch);

                if (cropped != scaled) scaled.recycle();
                if (bmp != cropped && !bmp.isRecycled()) bmp.recycle();

                return new BitmapDrawable(getResources(), cropped);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] readAllBytesFromStream(java.io.InputStream is) {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) {
                baos.write(buf, 0, r);
            }
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private Drawable makeCircularDrawable(Drawable src) {
        if (src == null) return null;
        try {
            Bitmap bmp;
            if (src instanceof BitmapDrawable) {
                bmp = ((BitmapDrawable) src).getBitmap();
            } else {
                int w = Math.max(src.getIntrinsicWidth(), 1);
                int h = Math.max(src.getIntrinsicHeight(), 1);
                Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                src.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                src.draw(canvas);
                bmp = bitmap;
            }

            int size = Math.min(bmp.getWidth(), bmp.getHeight());
            int x = (bmp.getWidth() - size) / 2;
            int y = (bmp.getHeight() - size) / 2;
            Bitmap squared = Bitmap.createBitmap(bmp, x, y, size, size);

            Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            BitmapShader shader = new BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            if (squared != bmp && !squared.isRecycled()) squared.recycle();

            return new BitmapDrawable(getResources(), output);
        } catch (Exception e) {
            return src;
        }
    }

    

    
    private void applyControlButtonTint(ImageButton button) {
        if (button != null && currentTheme != null) {
            button.setColorFilter(currentTheme.onSurfaceColor);
        }
    }
    
    private void applyThemeColors() {
        currentTheme = ThemeColors.getThemeColors(this, settingsManager.getTheme());
        applyThemeDynamically();
    }
    
    private void reloadSongs() {
        if (hasReadMediaPermission()) {
            loadSongs();
        }
    }

    private void syncFullPlayerState() {
        if (currentSongIndex < 0 || currentSongIndex >= songs.size()) return;
        Song song = songs.get(currentSongIndex);
        
        fullPlayerTitleView.setText(song.getTitle());
        fullPlayerArtistView.setText(song.getArtist() == null || song.getArtist().isEmpty() 
                ? getString(R.string.player_artist_unknown) : song.getArtist());
        fullPlayerAlbumView.setText(song.getAlbum() == null || song.getAlbum().isEmpty() 
                ? getString(R.string.player_album_unknown) : song.getAlbum());
        
        // Sync artwork
        if (artworkView.getDrawable() != null) {
            fullArtworkView.setImageDrawable(artworkView.getDrawable());
        }
        
        // Sync play/pause state
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            fullPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            fullPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
        
        // Sync shuffle/repeat states
        updateControlTint(fullShuffleButton, isShuffleEnabled);
        updateControlTint(fullRepeatButton, isRepeatEnabled);
    }

    private void openArtistView() {
        if (currentSongIndex < 0 || currentSongIndex >= songs.size()) return;
        String artist = songs.get(currentSongIndex).getArtist();
        if (artist == null || artist.isEmpty()) {
            Toast.makeText(this, "Unknown artist", Toast.LENGTH_SHORT).show();
            return;
        }
        
        currentArtistFilter = artist;
        navigationRailView.setSelectedItemId(R.id.nav_artists);
        showSection(artistsContainer);
    }

    private void openAlbumsView() {
        if (currentSongIndex < 0 || currentSongIndex >= songs.size()) return;
        String album = songs.get(currentSongIndex).getAlbum();
        if (album == null || album.isEmpty()) {
            Toast.makeText(this, "Unknown album", Toast.LENGTH_SHORT).show();
            return;
        }
        
        currentAlbumFilter = album;
        navigationRailView.setSelectedItemId(R.id.nav_albums);
        showSection(albumsContainer);
    }

    private void updateCollapsedPeekHeight() {
        if (playerSheet == null) return;
        int headerHeight = playerHeader != null && playerHeader.getHeight() > 0
                ? playerHeader.getHeight()
                : dpToPx(60);
        int paddingTop = playerSheet.getPaddingTop();
        collapsedPeekHeight = Math.max(dpToPx(88), paddingTop + headerHeight);
    }

    private void showMiniPlayerCollapsed() {
        if (playerSheet == null || playerSheetBehavior == null) return;
        if (collapsedPeekHeight <= 0) {
            updateCollapsedPeekHeight();
        }
        int peekHeight = collapsedPeekHeight > 0 ? collapsedPeekHeight : dpToPx(88);
        playerSheetBehavior.setPeekHeight(peekHeight, false);
        playerSheetBehavior.setHideable(false);
        playerSheetBehavior.setDraggable(true);
        if (playerSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        playerSheet.setVisibility(View.VISIBLE);
    }

    private void forceShowPlayerSheet() {
        showMiniPlayerCollapsed();
    }

    private void openSong(int index) {
        if (index < 0 || index >= songs.size()) return;
        startPlayback(index);
    }

    private void ensurePlayerSheetVisible() {
        showMiniPlayerCollapsed();
    }

    private void scheduleSheetGuard() {
        
    }

    // Core playback implementation (keeps behavior identical); callers should use wrapper methods
    private void startPlaybackCore(int index) {
        if (index < 0 || index >= songs.size()) return;
        clickHandler.removeCallbacks(backSingleAction);
        clickHandler.removeCallbacks(forwardSingleAction);
        backClickPending = false;
        forwardClickPending = false;
        currentSongIndex = index;
        Song song = songs.get(index);

        collapsedTitleView.setText(song.getTitle());
        playerTitleView.setText(song.getTitle());
        String artist = song.getArtist();
        playerArtistView.setText(artist == null || artist.isEmpty() ? getString(R.string.player_artist_unknown) : artist);
        String album = song.getAlbum();
        playerAlbumView.setText(album == null || album.isEmpty() ? getString(R.string.player_album_unknown) : album);
        loadArtwork(song);
        
        ensurePlayerSheetVisible();

        updateFavoriteButtonState();

        releasePlayer();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                updatePlayPauseIcon(true);
                updateWaveAnimationState(true);
                startArtworkSpin();
                updateNotification(true);
                seekBarUpdateHandler.post(seekBarUpdateRunnable);
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (isRepeatEnabled) {
                    mp.seekTo(0);
                    mp.start();
                    updateWaveAnimationState(true);
                    startArtworkSpin();
                    updateNotification(true);
                } else {
                    updateWaveAnimationState(false);
                    playNextTrack();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to play song", Toast.LENGTH_SHORT).show();
            updatePlayPauseIcon(false);
            updateWaveAnimationState(false);
            updateNotification(false);
        }
    }

    // Public wrapper: start playback in global (tracks) mode — clears any playlist-scoped queue
    private void startPlayback(int index) {
        playbackQueue = null;
        playbackQueuePos = -1;
        startPlaybackCore(index);
    }

    // Public wrapper: start playback with a specified queue (indices into `songs`).
    private void startPlaybackWithQueue(int index, java.util.List<Integer> queue) {
        if (queue == null || queue.isEmpty()) {
            startPlayback(index);
            return;
        }
        playbackQueue = new java.util.ArrayList<>(queue);
        playbackQueuePos = playbackQueue.indexOf(index);
        if (playbackQueuePos < 0) playbackQueuePos = 0;
        startPlaybackCore(index);
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updatePlayPauseIcon(false);
            updateWaveAnimationState(false);
            pauseArtworkSpin();
            seekBarUpdateHandler.removeCallbacks(seekBarUpdateRunnable);
            updateNotification(false);
        } else {
            mediaPlayer.start();
            updatePlayPauseIcon(true);
            updateWaveAnimationState(true);
            startArtworkSpin();
            seekBarUpdateHandler.post(seekBarUpdateRunnable);
            updateNotification(true);
        }
    }

    private void toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled;
        updateControlTint(shuffleButton, isShuffleEnabled);
        updateControlTint(fullShuffleButton, isShuffleEnabled);
        if (isShuffleEnabled) {
            playRandomTrack();
        }
        updateNotification(mediaPlayer != null && mediaPlayer.isPlaying());
    }

    private void toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled;
        updateControlTint(repeatButton, isRepeatEnabled);
        updateControlTint(fullRepeatButton, isRepeatEnabled);
        updateNotification(mediaPlayer != null && mediaPlayer.isPlaying());
    }

    private void updateWaveAnimationState(boolean playing) {
        if (miniSeekBar != null) miniSeekBar.setWaveAnimating(playing);
        if (fullSeekBar != null) fullSeekBar.setWaveAnimating(playing);
    }

    private void handleBackPress() {
        if (mediaPlayer == null) return;
        if (backClickPending) {
            clickHandler.removeCallbacks(backSingleAction);
            backClickPending = false;
            playPreviousTrack();
        } else {
            backClickPending = true;
            clickHandler.postDelayed(backSingleAction, DOUBLE_TAP_WINDOW_MS);
        }
    }

    private void handleForwardPress() {
        if (mediaPlayer == null) return;
        if (forwardClickPending) {
            clickHandler.removeCallbacks(forwardSingleAction);
            forwardClickPending = false;
            playNextTrack();
        } else {
            forwardClickPending = true;
            clickHandler.postDelayed(forwardSingleAction, DOUBLE_TAP_WINDOW_MS);
        }
    }

    private void playNextTrack() {
        if (songs.isEmpty()) return;
        int nextIndex = -1;
        if (playbackQueue != null && !playbackQueue.isEmpty()) {
            if (isShuffleEnabled) {
                // pick random from queue different from current queue pos
                int cand = random.nextInt(playbackQueue.size());
                if (cand == playbackQueuePos) cand = (cand + 1) % playbackQueue.size();
                playbackQueuePos = cand;
            } else {
                playbackQueuePos = (playbackQueuePos + 1) % playbackQueue.size();
            }
            nextIndex = playbackQueue.get(playbackQueuePos);
        } else {
            if (isShuffleEnabled) {
                nextIndex = randomIndexDifferentFrom(currentSongIndex);
            } else {
                nextIndex = (currentSongIndex + 1) % songs.size();
            }
        }
        // start depending on queue presence (startPlaybackWithQueue will keep queue unchanged)
        if (playbackQueue != null && !playbackQueue.isEmpty()) {
            startPlaybackCore(nextIndex);
        } else {
            startPlayback(nextIndex);
        }
        updateNotification(mediaPlayer != null && mediaPlayer.isPlaying());
    }

    private void playPreviousTrack() {
        if (songs.isEmpty()) return;
        int prevIndex = -1;
        if (playbackQueue != null && !playbackQueue.isEmpty()) {
            if (isShuffleEnabled) {
                int cand = random.nextInt(playbackQueue.size());
                if (cand == playbackQueuePos) cand = (cand + 1) % playbackQueue.size();
                playbackQueuePos = cand;
            } else {
                playbackQueuePos = playbackQueuePos - 1;
                if (playbackQueuePos < 0) playbackQueuePos = playbackQueue.size() - 1;
            }
            prevIndex = playbackQueue.get(playbackQueuePos);
        } else {
            if (isShuffleEnabled) {
                prevIndex = randomIndexDifferentFrom(currentSongIndex);
            } else {
                prevIndex = currentSongIndex - 1;
                if (prevIndex < 0) {
                    prevIndex = songs.size() - 1;
                }
            }
        }
        if (playbackQueue != null && !playbackQueue.isEmpty()) {
            startPlaybackCore(prevIndex);
        } else {
            startPlayback(prevIndex);
        }
        updateNotification(mediaPlayer != null && mediaPlayer.isPlaying());
    }

    private void playRandomTrack() {
        if (songs.isEmpty()) return;
        startPlayback(randomIndexDifferentFrom(currentSongIndex));
    }

    private int randomIndexDifferentFrom(int exclude) {
        if (songs.size() <= 1) return 0;
        int candidate = random.nextInt(songs.size());
        if (candidate == exclude) {
            candidate = (candidate + 1) % songs.size();
        }
        return candidate;
    }

    private void seekBy(int deltaMs) {
        if (mediaPlayer == null) return;
        int duration = mediaPlayer.getDuration();
        if (duration <= 0) return;
        int target = mediaPlayer.getCurrentPosition() + deltaMs;
        target = Math.max(0, Math.min(duration, target));
        mediaPlayer.seekTo(target);
    }

    

    private void updatePlayPauseIcon(boolean isPlaying) {
        if (playPauseButton == null) return;
        playPauseButton.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        if (fullPlayPauseButton != null) {
            fullPlayPauseButton.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        }
    }

    private void updateControlTint(ImageButton button, boolean active) {
        if (button == null) return;
        int color = ContextCompat.getColor(this, active ? R.color.player_accent : R.color.player_control);
        button.setColorFilter(color);
    }

    private void loadArtists() {
        artists.clear();
        java.util.Map<String, Integer> artistCount = new java.util.HashMap<>();
        
        for (Song song : songs) {
            String artist = song.getArtist();
            if (artist == null || artist.isEmpty()) {
                artist = getString(R.string.player_artist_unknown);
            }
            artistCount.put(artist, artistCount.getOrDefault(artist, 0) + 1);
        }
        
        for (java.util.Map.Entry<String, Integer> entry : artistCount.entrySet()) {
            artists.add(new Artist(entry.getKey(), entry.getValue()));
        }
        
        artists.sort((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
        
        List<ListItem> listItems = buildArtistListWithHeaders();
        artistAdapter = new GenericListAdapter(listItems);
        artistsRecyclerView.setAdapter(artistAdapter);
        artistAdapter.setOnItemClickListener(item -> {
            if (item.getType() == ListItem.TYPE_HEADER) {
                showAlphabetPopupForArtists();
            } else if (item.getType() == ListItem.TYPE_ARTIST) {
                String artistName = item.getArtist().getName();
                showSongsForArtist(artistName);
            }
        });
    }

    private List<ListItem> buildArtistListWithHeaders() {
        List<ListItem> items = new ArrayList<>();
        if (artists.isEmpty()) return items;
        
        String currentLetter = null;
        for (int i = 0; i < artists.size(); i++) {
            Artist artist = artists.get(i);
            String name = artist.getName();
            if (name != null && !name.isEmpty()) {
                String firstLetter = name.substring(0, 1).toUpperCase();
                if (Character.isLetter(firstLetter.charAt(0))) {
                    if (!firstLetter.equals(currentLetter)) {
                        currentLetter = firstLetter;
                        items.add(ListItem.createHeader(currentLetter));
                    }
                }
            }
            items.add(ListItem.createArtist(artist, i));
        }
        return items;
    }

    private void showAlphabetPopupForArtists() {
        // Similar to songs alphabet popup but for artists
        showGenericAlphabetPopup(artistAdapter, artistsRecyclerView);
    }

    private void showSongsForArtist(String artistName) {
        if (artistName == null || artistsRecyclerView == null) return;
        currentArtistFilter = artistName;

        List<ListItem> items = new ArrayList<>();
        final java.util.List<Integer> queueIndices = new java.util.ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            String a = s.getArtist();
            if (a == null || a.isEmpty()) a = getString(R.string.player_artist_unknown);
            if (artistName.equals(a)) {
                items.add(ListItem.createSong(s, i));
                queueIndices.add(i);
            }
        }

        SongAdapter sa = new SongAdapter(items);
        sa.setOnItemClickListener((song, pos) -> {
            if (pos >= 0 && pos < queueIndices.size()) {
                int globalIndex = queueIndices.get(pos);
                startPlaybackWithQueue(globalIndex, queueIndices);
            }
        });
        sa.setOnItemLongClickListener((song, pos) -> {
            // allow removal or other actions in future
        });

        artistsRecyclerView.setAdapter(sa);
        navigationRailView.setSelectedItemId(R.id.nav_artists);
        showSection(artistsContainer);
    }

    private void restoreArtistList() {
        loadArtists();
    }

    private void loadAlbums() {
        albums.clear();
        java.util.Map<String, java.util.Map.Entry<String, Integer>> albumInfo = new java.util.HashMap<>();
        
        for (Song song : songs) {
            String album = song.getAlbum();
            if (album == null || album.isEmpty()) {
                album = getString(R.string.player_album_unknown);
            }
            String artist = song.getArtist();
            if (artist == null || artist.isEmpty()) {
                artist = getString(R.string.player_artist_unknown);
            }
            
            if (albumInfo.containsKey(album)) {
                java.util.Map.Entry<String, Integer> entry = albumInfo.get(album);
                albumInfo.put(album, new java.util.AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue() + 1));
            } else {
                albumInfo.put(album, new java.util.AbstractMap.SimpleEntry<>(artist, 1));
            }
        }
        
        for (java.util.Map.Entry<String, java.util.Map.Entry<String, Integer>> entry : albumInfo.entrySet()) {
            albums.add(new Album(entry.getKey(), entry.getValue().getKey(), entry.getValue().getValue()));
        }
        
        albums.sort((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()));
        
        List<ListItem> listItems = buildAlbumListWithHeaders();
        albumAdapter = new GenericListAdapter(listItems);
        albumsRecyclerView.setAdapter(albumAdapter);
        albumAdapter.setOnItemClickListener(item -> {
            if (item.getType() == ListItem.TYPE_HEADER) {
                showAlphabetPopupForAlbums();
            } else if (item.getType() == ListItem.TYPE_ALBUM) {
                String albumName = item.getAlbum().getName();
                showSongsForAlbum(albumName);
            }
        });
    }

    private List<ListItem> buildAlbumListWithHeaders() {
        List<ListItem> items = new ArrayList<>();
        if (albums.isEmpty()) return items;
        
        String currentLetter = null;
        for (int i = 0; i < albums.size(); i++) {
            Album album = albums.get(i);
            String name = album.getName();
            if (name != null && !name.isEmpty()) {
                String firstLetter = name.substring(0, 1).toUpperCase();
                if (Character.isLetter(firstLetter.charAt(0))) {
                    if (!firstLetter.equals(currentLetter)) {
                        currentLetter = firstLetter;
                        items.add(ListItem.createHeader(currentLetter));
                    }
                }
            }
            items.add(ListItem.createAlbum(album, i));
        }
        return items;
    }

    private void showAlphabetPopupForAlbums() {
        showGenericAlphabetPopup(albumAdapter, albumsRecyclerView);
    }

    private void showSongsForAlbum(String albumName) {
        if (albumName == null || albumsRecyclerView == null) return;
        currentAlbumFilter = albumName;

        List<ListItem> items = new ArrayList<>();
        final java.util.List<Integer> queueIndices = new java.util.ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            String a = s.getAlbum();
            if (a == null || a.isEmpty()) a = getString(R.string.player_album_unknown);
            if (albumName.equals(a)) {
                items.add(ListItem.createSong(s, i));
                queueIndices.add(i);
            }
        }

        SongAdapter sa = new SongAdapter(items);
        sa.setOnItemClickListener((song, pos) -> {
            // pos is position within this items list; translate to global index via queueIndices
            if (pos >= 0 && pos < queueIndices.size()) {
                int globalIndex = queueIndices.get(pos);
                startPlaybackWithQueue(globalIndex, queueIndices);
            }
        });
        sa.setOnItemLongClickListener((song, pos) -> {
            // allow default long-press (no-op) or future features
        });

        albumsRecyclerView.setAdapter(sa);
        // ensure we're in Albums section
        navigationRailView.setSelectedItemId(R.id.nav_albums);
        showSection(albumsContainer);
    }

    private void restoreAlbumList() {
        loadAlbums();
    }

    private void showGenericAlphabetPopup(GenericListAdapter adapter, RecyclerView targetRecyclerView) {
        if (alphabetPopupContainer == null || adapter == null) return;
        
        java.util.Set<String> availableLetters = new java.util.HashSet<>();
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ListItem item = adapter.getItemAtPosition(i);
            if (item.getType() == ListItem.TYPE_HEADER) {
                availableLetters.add(item.getHeader());
            }
        }
        
        if (availableLetters.isEmpty()) return;
        
        alphabetPopupContainer.removeAllViews();
        View popupView = getLayoutInflater().inflate(R.layout.alphabet_popup, alphabetPopupContainer, false);
        androidx.gridlayout.widget.GridLayout grid = popupView.findViewById(R.id.alphabet_grid);
        
        java.util.List<String> sortedLetters = new java.util.ArrayList<>(availableLetters);
        java.util.Collections.sort(sortedLetters);
        
        for (String letter : sortedLetters) {
            TextView letterView = new TextView(this);
            letterView.setText(letter);
            letterView.setGravity(Gravity.CENTER);
            letterView.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
            letterView.setTextSize(18f);
            letterView.setTextColor(currentTheme != null ? currentTheme.accentColor : Color.WHITE);
            letterView.setTypeface(null, android.graphics.Typeface.BOLD);
            letterView.setClickable(true);
            letterView.setFocusable(true);
            letterView.setBackground(ContextCompat.getDrawable(this, android.R.drawable.list_selector_background));
            
            letterView.setOnClickListener(v -> {
                scrollToLetterInAdapter(letter, adapter, targetRecyclerView);
                hideAlphabetPopup();
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            });
            
            grid.addView(letterView);
        }
        
        alphabetPopupContainer.addView(popupView);
        alphabetPopupContainer.setVisibility(View.VISIBLE);
        alphabetPopupContainer.setAlpha(0f);
        alphabetPopupContainer.animate().alpha(1f).setDuration(200).start();
        
        alphabetPopupContainer.setOnClickListener(v -> hideAlphabetPopup());
    }

    private void scrollToLetterInAdapter(String letter, GenericListAdapter adapter, RecyclerView recyclerView) {
        if (recyclerView == null || adapter == null || adapter.getItemCount() == 0) return;
        
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ListItem item = adapter.getItemAtPosition(i);
            if (item.getType() == ListItem.TYPE_HEADER && letter.equals(item.getHeader())) {
                recyclerView.scrollToPosition(i);
                return;
            }
        }
    }

    private void setupMediaSessionAndNotifications() {
        notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Now Playing",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Playback controls");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        mediaSession = new MediaSessionCompat(this, "MonochromePlayerSession");
        mediaSession.setActive(true);

        notificationActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                String action = intent.getAction();
                if (ACTION_TOGGLE.equals(action)) {
                    togglePlayPause();
                } else if (ACTION_NEXT.equals(action)) {
                    playNextTrack();
                } else if (ACTION_PREV.equals(action)) {
                    playPreviousTrack();
                } else if (ACTION_CLOSE.equals(action)) {
                    stopPlaybackAndClear();
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_TOGGLE);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PREV);
        filter.addAction(ACTION_CLOSE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationActionReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(notificationActionReceiver, filter);
        }
    }

    private void setupBackHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If a playlist detail is open, close it first
                if (currentPlaylistViewing != null) {
                    showPlaylistListView();
                    return;
                }
                // If currently viewing an album detail, restore album list
                if (currentAlbumFilter != null) {
                    currentAlbumFilter = null;
                    restoreAlbumList();
                    return;
                }
                // If currently viewing an artist detail, restore artist list
                if (currentArtistFilter != null) {
                    currentArtistFilter = null;
                    restoreArtistList();
                    return;
                }
                // Otherwise, if a different navigation tab is selected, go back to Tracks
                if (navigationRailView != null && navigationRailView.getSelectedItemId() != R.id.nav_tracks) {
                    navigationRailView.setSelectedItemId(R.id.nav_tracks);
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private PendingIntent buildPendingIntent(String action, int requestCode) {
        Intent intent = new Intent(action);
        intent.setPackage(getPackageName());
        return PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void updateNotification(boolean isPlaying) {
        if (songs.isEmpty() || currentSongIndex < 0 || currentSongIndex >= songs.size()) {
            notificationManager.cancel(NOTIFICATION_ID);
            return;
        }
        Song song = songs.get(currentSongIndex);

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Action prevAction = new NotificationCompat.Action(
                android.R.drawable.ic_media_previous, "Prev", buildPendingIntent(ACTION_PREV, 1));
        NotificationCompat.Action toggleAction = new NotificationCompat.Action(
                isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                isPlaying ? "Pause" : "Play",
                buildPendingIntent(ACTION_TOGGLE, 2));
        NotificationCompat.Action nextAction = new NotificationCompat.Action(
                android.R.drawable.ic_media_next, "Next", buildPendingIntent(ACTION_NEXT, 3));
        NotificationCompat.Action closeAction = new NotificationCompat.Action(
                android.R.drawable.ic_menu_close_clear_cancel, "Stop", buildPendingIntent(ACTION_CLOSE, 4));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(song.getTitle())
                .setContentText(song.getArtist() == null ? "" : song.getArtist())
                .setColor(ContextCompat.getColor(this, R.color.player_accent))
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying)
                .addAction(prevAction)
                .addAction(toggleAction)
                .addAction(nextAction)
                .addAction(closeAction)
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2, 3));

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void loadArtwork(Song song) {
        if (artworkView == null) return;
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(song.getPath());
            byte[] data = retriever.getEmbeddedPicture();
            if (data != null) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        } catch (Exception ignored) {
            // ignore broken metadata
        } finally {
            try {
                retriever.release();
            } catch (IOException ignored) {
                // Safe to ignore release failures
            }
        }
        if (bitmap != null) {
            artworkView.setImageBitmap(bitmap);
            if (fullArtworkView != null) {
                fullArtworkView.setImageBitmap(bitmap);
            }
        } else {
            // Default fallback is the Android icon. For Mecha theme use custom spinner asset (circular)
            Drawable spinner = null;
            try {
                String currentTheme = settingsManager != null ? settingsManager.getTheme() : null;
                if (SettingsManager.THEME_MECHA.equals(currentTheme)) {
                    spinner = loadWallpaperDrawableFromAsset("wallpapers/NGEspinning.png");
                    if (spinner != null) spinner = makeCircularDrawable(spinner);
                }
            } catch (Exception ignored) {
            }
            if (spinner != null) {
                artworkView.setImageDrawable(spinner);
                if (fullArtworkView != null) fullArtworkView.setImageDrawable(spinner);
            } else {
                artworkView.setImageResource(R.mipmap.ic_launcher);
                if (fullArtworkView != null) {
                    fullArtworkView.setImageResource(R.mipmap.ic_launcher);
                }
            }
        }
        
        // Sync full player if it's open
        if (fullPlayerSheet != null && fullPlayerSheet.getVisibility() == View.VISIBLE) {
            syncFullPlayerState();
            updateFavoriteButtonState();
        }
    }

    private void startArtworkSpin() {
        if (artworkView == null) return;
        if (artworkAnimator == null) {
            artworkAnimator = ObjectAnimator.ofFloat(artworkView, View.ROTATION, 0f, 360f);
            artworkAnimator.setDuration(12000);
            artworkAnimator.setInterpolator(new LinearInterpolator());
            artworkAnimator.setRepeatCount(ValueAnimator.INFINITE);
        }
        if (artworkAnimator.isPaused()) {
            artworkAnimator.resume();
        } else if (!artworkAnimator.isRunning()) {
            artworkAnimator.start();
        }
        
        // Also spin full player artwork
        if (fullArtworkView != null) {
            if (fullArtworkAnimator == null) {
                fullArtworkAnimator = ObjectAnimator.ofFloat(fullArtworkView, View.ROTATION, 0f, 360f);
                fullArtworkAnimator.setDuration(12000);
                fullArtworkAnimator.setInterpolator(new LinearInterpolator());
                fullArtworkAnimator.setRepeatCount(ValueAnimator.INFINITE);
            }
            if (fullArtworkAnimator.isPaused()) {
                fullArtworkAnimator.resume();
            } else if (!fullArtworkAnimator.isRunning()) {
                fullArtworkAnimator.start();
            }
        }
    }

    private void pauseArtworkSpin() {
        if (artworkAnimator != null && artworkAnimator.isRunning()) {
            artworkAnimator.pause();
        }
        if (fullArtworkAnimator != null && fullArtworkAnimator.isRunning()) {
            fullArtworkAnimator.pause();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        seekBarUpdateHandler.removeCallbacks(seekBarUpdateRunnable);
        updateWaveAnimationState(false);
        if (artworkAnimator != null) {
            artworkAnimator.cancel();
        }
        updateNotification(false);
    }

    private void stopPlaybackAndClear() {
        currentSongIndex = -1;
        releasePlayer();
        collapsedTitleView.setText(getString(R.string.player_not_playing));
        playerTitleView.setText(getString(R.string.player_not_playing));
        playerArtistView.setText("");
        updatePlayPauseIcon(false);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private void expandPlayer() {
        if (playerSheetBehavior == null) return;
        playerSheetBehavior.setHideable(false);
        playerSheetBehavior.setSkipCollapsed(true);
        playerSheetBehavior.setDraggable(true);
        playerSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void togglePlayerSheet() {
        if (playerSheetBehavior == null) return;
        if (playerSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            collapsePlayer();
        } else {
            expandPlayer();
        }
    }

    private void collapsePlayer() {
        if (playerSheetBehavior == null) return;
        playerSheetBehavior.setSkipCollapsed(false);
        playerSheetBehavior.setDraggable(true);
        playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void toggleAboutSection() {
        isAboutExpanded = !isAboutExpanded;
        if (aboutAppName != null) aboutAppName.setVisibility(isAboutExpanded ? View.VISIBLE : View.GONE);
        if (aboutVersion != null) aboutVersion.setVisibility(isAboutExpanded ? View.VISIBLE : View.GONE);
        if (aboutDescription != null) aboutDescription.setVisibility(isAboutExpanded ? View.VISIBLE : View.GONE);
    }

    private void showSection(View targetSection) {
        setSectionVisibility(nowPlayingContainer, false);
        setSectionVisibility(tracksContainer, targetSection == tracksContainer);
        setSectionVisibility(artistsContainer, targetSection == artistsContainer);
        setSectionVisibility(playlistsContainer, targetSection == playlistsContainer);
        setSectionVisibility(albumsContainer, targetSection == albumsContainer);
        setSectionVisibility(settingsContainer, targetSection == settingsContainer);

        if (playerSheet != null && currentSongIndex >= 0) {
            playerSheet.setVisibility(View.VISIBLE);
        }

        if (targetSection == tracksContainer) {
            // Always restore the full songs listing when viewing Tracks
            restoreFullSongList();
            if (songs.isEmpty()) {
                showEmptyState(hasReadMediaPermission() ? R.string.empty_state_no_music : R.string.empty_state_no_permission);
            } else {
                hideEmptyState();
            }
        } else {
            hideEmptyState();
        }
    }
    
    private void showNowPlaying() {
        setSectionVisibility(nowPlayingContainer, true);
        setSectionVisibility(tracksContainer, false);
        setSectionVisibility(artistsContainer, false);
        setSectionVisibility(playlistsContainer, false);
        setSectionVisibility(albumsContainer, false);
        setSectionVisibility(settingsContainer, false);
        hideEmptyState();
        
        if (playerSheet != null) {
            playerSheet.setVisibility(View.GONE);
        }
        
        if (fullPlayerSheet != null) {
            fullPlayerSheet.setVisibility(View.VISIBLE);
            syncFullPlayerState();
        }
    }

    private void setSectionVisibility(View section, boolean visible) {
        if (section != null) {
            if (visible) {
                section.setAlpha(0f);
                section.setVisibility(View.VISIBLE);
                section.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            } else {
                section.animate()
                        .alpha(0f)
                        .setDuration(150)
                        .setInterpolator(new android.view.animation.AccelerateInterpolator())
                        .withEndAction(() -> section.setVisibility(View.GONE))
                        .start();
            }
        }
    }

    // --- Playlist helpers ---
    private void loadPlaylists() {
        playlists.clear();
        if (settingsManager != null) {
            playlists.addAll(settingsManager.getPlaylists());
        }
        // Ensure Favorites exists
        boolean hasFav = false;
        for (Playlist p : playlists) if ("Favorites".equalsIgnoreCase(p.getName())) hasFav = true;
        if (!hasFav) {
            Playlist fav = new Playlist("Favorites");
            playlists.add(0, fav);
            if (settingsManager != null) settingsManager.updatePlaylists(playlists);
        }

        if (playlistsRecyclerView != null) {
            playlistAdapter = new PlaylistAdapter(this, playlists, (p, pos) -> showPlaylistInPlaylistSection(p));
            playlistAdapter.setOnItemLongClickListener((p, pos) -> {
                if ("Favorites".equalsIgnoreCase(p.getName())) {
                    Toast.makeText(this, "Cannot delete Favorites", Toast.LENGTH_SHORT).show();
                    return;
                }
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Delete playlist")
                        .setMessage("Delete playlist '" + p.getName() + "'?")
                        .setPositiveButton("Delete", (d, which) -> {
                            playlists.remove(pos);
                            settingsManager.updatePlaylists(playlists);
                            playlistAdapter.notifyDataSetChanged();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
            playlistsRecyclerView.setAdapter(playlistAdapter);
        }
    }

    private void showCreatePlaylistDialog() {
        androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(this);
        b.setTitle("Create playlist");
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Playlist name");
        b.setView(input);
        b.setPositiveButton("Create", (dlg, which) -> {
            String name = input.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            Playlist p = new Playlist(name);
            // Offer song selection immediately after creation
            playlists.add(p);
            settingsManager.updatePlaylists(playlists);
            if (playlistAdapter != null) playlistAdapter.notifyDataSetChanged();
            // Show multi-select to add songs
            showAddToPlaylistDialogForPlaylist(p);
        });
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    private void showAddToPlaylistDialogForCurrentSong() {
        if (currentSongIndex < 0 || currentSongIndex >= songs.size()) {
            Toast.makeText(this, "No song playing", Toast.LENGTH_SHORT).show();
            return;
        }
        Song song = songs.get(currentSongIndex);
        showSimplePlaylistPicker((p) -> {
            if (p.containsSong(song.getPath())) {
                Toast.makeText(this, "Already in playlist", Toast.LENGTH_SHORT).show();
                return;
            }
            p.addSong(song.getPath());
            settingsManager.updatePlaylists(playlists);
            Toast.makeText(this, "Added to " + p.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void showAddToPlaylistDialogForPlaylist(Playlist target) {
        // Show a multi-select list of all songs to add to the target playlist
        if (target == null) return;
        String[] titles = new String[songs.size()];
        boolean[] checked = new boolean[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            titles[i] = songs.get(i).getTitle();
            checked[i] = target.containsSong(songs.get(i).getPath());
        }
        androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(this);
        b.setTitle("Add songs to " + target.getName());
        b.setMultiChoiceItems(titles, checked, (dialog, which, isChecked) -> {
            // nothing here, we'll read on positive
        });
        b.setPositiveButton("Add", (d, w) -> {
            androidx.appcompat.app.AlertDialog ad = (androidx.appcompat.app.AlertDialog) d;
            android.widget.ListView lv = ad.getListView();
            for (int i = 0; i < lv.getCount(); i++) {
                if (lv.isItemChecked(i)) {
                    String path = songs.get(i).getPath();
                    if (!target.containsSong(path)) target.addSong(path);
                }
            }
            settingsManager.updatePlaylists(playlists);
            Toast.makeText(this, "Updated " + target.getName(), Toast.LENGTH_SHORT).show();
            // If currently viewing this playlist, refresh UI
            if (currentPlaylistViewing != null && currentPlaylistViewing == target) showSongsForPlaylist(target);
        });
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    private void showPlaylistInPlaylistSection(Playlist p) {
        if (p == null || playlistDetailView == null) return;
        currentPlaylistViewing = p;
        playlistDetailTitle.setText(p.getName());

        // Build items from playlist paths
        java.util.List<ListItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < p.getSongPaths().size(); i++) {
            String path = p.getSongPaths().get(i);
            int idx = findSongIndexByPath(path);
            if (idx >= 0) items.add(ListItem.createSong(songs.get(idx), idx));
        }

        if (playlistSongsRecyclerView != null) {
            // Build queue indices for this playlist
            final java.util.List<Integer> queueIndices = new java.util.ArrayList<>();
            for (int i = 0; i < p.getSongPaths().size(); i++) {
                String path = p.getSongPaths().get(i);
                int idx = findSongIndexByPath(path);
                if (idx >= 0) queueIndices.add(idx);
            }

            SongAdapter sa = new SongAdapter(items);
            sa.setOnItemClickListener((song, pos) -> startPlaybackWithQueue(pos, queueIndices));
            sa.setOnItemLongClickListener((song, pos) -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Remove from playlist")
                        .setMessage("Remove '" + song.getTitle() + "' from " + p.getName() + "?")
                        .setPositiveButton("Remove", (d, w) -> {
                            p.removeSong(song.getPath());
                            settingsManager.updatePlaylists(playlists);
                            showPlaylistInPlaylistSection(p);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
            playlistSongsRecyclerView.setAdapter(sa);
        }

        // Show detail view, hide list
        if (playlistsRecyclerView != null) playlistsRecyclerView.setVisibility(View.GONE);
        if (buttonCreatePlaylist != null) buttonCreatePlaylist.setVisibility(View.GONE);
        playlistDetailView.setVisibility(View.VISIBLE);
        // Ensure Playlists tab is visible
        navigationRailView.setSelectedItemId(R.id.nav_playlists);
        showSection(playlistsContainer);
    }

    private void showPlaylistListView() {
        currentPlaylistViewing = null;
        if (playlistDetailView != null) playlistDetailView.setVisibility(View.GONE);
        if (playlistsRecyclerView != null) playlistsRecyclerView.setVisibility(View.VISIBLE);
        if (buttonCreatePlaylist != null) buttonCreatePlaylist.setVisibility(View.VISIBLE);
    }

    private void showSimplePlaylistPicker(java.util.function.Consumer<Playlist> onPicked) {
        if (playlists.isEmpty()) {
            Toast.makeText(this, "No playlists", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[playlists.size()];
        for (int i = 0; i < playlists.size(); i++) names[i] = playlists.get(i).getName();
        androidx.appcompat.app.AlertDialog.Builder b = new androidx.appcompat.app.AlertDialog.Builder(this);
        b.setTitle("Choose playlist");
        b.setItems(names, (dialog, which) -> {
            Playlist p = playlists.get(which);
            if (onPicked != null) onPicked.accept(p);
        });
        b.setNegativeButton("Cancel", null);
        b.show();
    }

    private void showSongsForPlaylist(Playlist p) {
        if (p == null) return;
        currentPlaylistViewing = p;
        // Build ListItem list
        List<ListItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < p.getSongPaths().size(); i++) {
            String path = p.getSongPaths().get(i);
            int idx = findSongIndexByPath(path);
            if (idx >= 0 && idx < songs.size()) {
                items.add(ListItem.createSong(songs.get(idx), idx));
            }
        }
        adapter = new SongAdapter(items);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((song, pos) -> startPlayback(pos));
        adapter.setOnItemLongClickListener((song, pos) -> {
            // remove from playlist
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Remove from playlist")
                    .setMessage("Remove '" + song.getTitle() + "' from " + p.getName() + "?")
                    .setPositiveButton("Remove", (d, w) -> {
                        p.removeSong(song.getPath());
                        settingsManager.updatePlaylists(playlists);
                        showSongsForPlaylist(p);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        // Show add button in tracks header
        if (playlistAddInTracks != null) playlistAddInTracks.setVisibility(View.VISIBLE);
    }

    private void restoreFullSongList() {
        currentPlaylistViewing = null;
        List<ListItem> listItems = buildListWithHeaders();
        adapter = new SongAdapter(listItems);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((song, position) -> openSong(position));
        adapter.setOnHeaderClickListener(this::showAlphabetPopup);
        adapter.setOnItemLongClickListener((song, position) -> {});
        if (playlistAddInTracks != null) playlistAddInTracks.setVisibility(View.GONE);
    }

    private int findSongIndexByPath(String path) {
        if (path == null) return -1;
        for (int i = 0; i < songs.size(); i++) {
            if (path.equals(songs.get(i).getPath())) return i;
        }
        return -1;
    }

    private Playlist getFavoritesPlaylist() {
        for (Playlist p : playlists) {
            if ("Favorites".equalsIgnoreCase(p.getName())) return p;
        }
        // create if missing
        Playlist fav = new Playlist("Favorites");
        playlists.add(0, fav);
        if (settingsManager != null) settingsManager.updatePlaylists(playlists);
        if (playlistAdapter != null) playlistAdapter.notifyDataSetChanged();
        return fav;
    }

    private void updateFavoriteButtonState() {
        if (fullFavoriteButton == null) return;
        if (currentSongIndex < 0 || currentSongIndex >= songs.size()) {
            fullFavoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
            return;
        }
        Playlist fav = getFavoritesPlaylist();
        String path = songs.get(currentSongIndex).getPath();
        if (fav.containsSong(path)) {
            fullFavoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
            try {
                android.content.res.ColorStateList tint = android.content.res.ColorStateList.valueOf(android.graphics.Color.YELLOW);
                fullFavoriteButton.setImageTintList(tint);
            } catch (Exception ignored) {}
        } else {
            fullFavoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
            try {
                if (currentTheme != null) {
                    fullFavoriteButton.setImageTintList(android.content.res.ColorStateList.valueOf(currentTheme.onSurfaceColor));
                } else {
                    fullFavoriteButton.setImageTintList(null);
                }
            } catch (Exception ignored) {}
        }
    }

    private void showEmptyState(@StringRes int messageRes) {
        if (emptyView != null) {
            emptyView.setText(messageRes);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void hideEmptyState() {
        if (emptyView != null) {
            emptyView.setVisibility(View.GONE);
        }
    }
}
