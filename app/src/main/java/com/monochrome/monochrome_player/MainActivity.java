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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.HapticFeedbackConstants;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private SongAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private View tracksContainer;
    private View playlistsContainer;
    private View albumsContainer;
    private View settingsContainer;
    private NavigationRailView navigationRailView;
    private View alphabetScrollerContainer;
    private ViewGroup alphabetScroller;
    private final char[] alphabetChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final Handler alphabetHideHandler = new Handler(Looper.getMainLooper());
    private final Runnable alphabetHideRunnable = this::hideAlphabetScroller;
    private final Handler sheetGuardHandler = new Handler(Looper.getMainLooper());
    private final Runnable sheetGuardRunnable = this::forceShowPlayerSheet;

    // Player UI
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
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navigationRailView = findViewById(R.id.navigation_rail);
        tracksContainer = findViewById(R.id.tracks_container);
        playlistsContainer = findViewById(R.id.playlists_container);
        albumsContainer = findViewById(R.id.albums_container);
        settingsContainer = findViewById(R.id.settings_container);
        alphabetScrollerContainer = findViewById(R.id.alphabet_scroller_container);
        alphabetScroller = findViewById(R.id.alphabet_scroller);
        setupAlphabetScroller();

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false); // keep sheet stable when scrolling list
        adapter = new SongAdapter(songs);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((song, position) -> openSong(position));

        emptyView = findViewById(R.id.empty_view);
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        loadSongs();
                        ensurePlayerSheetVisible();
                    } else {
                        showEmptyState(R.string.empty_state_no_permission);
                        Toast.makeText(this, "Permission denied. Cannot load songs.", Toast.LENGTH_SHORT).show();
                    }
                });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (tracksContainer.getVisibility() == View.VISIBLE && !songs.isEmpty()) {
                    showAlphabetScroller();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView rv, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scheduleAlphabetHide();
                } else if (!songs.isEmpty() && tracksContainer.getVisibility() == View.VISIBLE) {
                    showAlphabetScroller();
                }
            }
        });

        setupMediaSessionAndNotifications();
        setupPlayerSheet();
        setupBackHandling();

        navigationRailView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tracks) {
                showSection(tracksContainer);
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

        // Make sure mini-player is visible on start
        playerSheet.post(this::ensurePlayerSheetVisible);
    }

    private void checkPermissionsAndLoadSongs() {
        if (hasReadMediaPermission()) {
            loadSongs();
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
                int albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleIndex);
                    String artist = cursor.getString(artistIndex);
                    String album = cursor.getString(albumIndex);
                    String path = cursor.getString(dataIndex);
                    songs.add(new Song(title, artist, album, path));
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
        forceShowPlayerSheet();
        if (songs.isEmpty()) {
            showEmptyState(R.string.empty_state_no_music);
        } else {
            hideEmptyState();
        }
    }

    private void setupAlphabetScroller() {
        if (alphabetScroller == null) return;
        alphabetScroller.removeAllViews();
        for (char c : alphabetChars) {
            TextView letterView = new TextView(this);
            letterView.setText(String.valueOf(c));
            letterView.setGravity(Gravity.CENTER_HORIZONTAL);
            letterView.setPadding(0, 8, 0, 8);
            letterView.setTextSize(12f);
            letterView.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            final String target = String.valueOf(c);
            letterView.setOnClickListener(v -> scrollToLetter(target));
            alphabetScroller.addView(letterView);
        }
        alphabetScrollerContainer.setOnTouchListener((v, event) -> {
            if (alphabetScroller == null || alphabetChars.length == 0) return false;
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    handleAlphabetTouch(event.getY());
                    showAlphabetScroller();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    scheduleAlphabetHide();
                    return true;
                default:
                    return false;
            }
        });
    }

    private void scrollToLetter(String letter) {
        if (recyclerView == null || songs.isEmpty()) return;
        String target = letter.toUpperCase();
        for (int i = 0; i < songs.size(); i++) {
            String title = songs.get(i).getTitle();
            if (title != null && !title.isEmpty()) {
                String first = title.substring(0, 1).toUpperCase();
                if (first.compareTo(target) >= 0) {
                    recyclerView.scrollToPosition(i);
                    showAlphabetScroller();
                    scheduleAlphabetHide();
                    return;
                }
            }
        }
        recyclerView.scrollToPosition(songs.size() - 1);
        showAlphabetScroller();
        scheduleAlphabetHide();
    }

    private void handleAlphabetTouch(float touchY) {
        if (alphabetScrollerContainer == null || alphabetChars.length == 0) return;
        int containerHeight = alphabetScrollerContainer.getHeight();
        if (containerHeight <= 0) return;
        float clampedY = Math.max(0, Math.min(touchY, containerHeight));
        float perItem = (float) containerHeight / alphabetChars.length;
        int index = (int) (clampedY / perItem);
        index = Math.max(0, Math.min(alphabetChars.length - 1, index));
        scrollToLetter(String.valueOf(alphabetChars[index]));
    }

    private void showAlphabetScroller() {
        alphabetHideHandler.removeCallbacks(alphabetHideRunnable);
        if (alphabetScrollerContainer != null && tracksContainer.getVisibility() == View.VISIBLE && !songs.isEmpty()) {
            alphabetScrollerContainer.setVisibility(View.VISIBLE);
        }
    }

    private void hideAlphabetScroller() {
        if (alphabetScrollerContainer != null) {
            alphabetScrollerContainer.setVisibility(View.GONE);
        }
        ensurePlayerSheetVisible();
    }

    private void scheduleAlphabetHide() {
        alphabetHideHandler.removeCallbacks(alphabetHideRunnable);
        alphabetHideHandler.postDelayed(alphabetHideRunnable, 1200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        alphabetHideHandler.removeCallbacks(alphabetHideRunnable);
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
            if (playerSheet != null) {
                playerSheet.setVisibility(View.VISIBLE);
                playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
    }

    @Override
    protected void onStart() {
        super.onStart();
            if (playerSheet != null) {
                playerSheet.setVisibility(View.VISIBLE);
                playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
            if (hasFocus && playerSheet != null) {
                playerSheet.setVisibility(View.VISIBLE);
                playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
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

        playerSheetBehavior = BottomSheetBehavior.from(playerSheet);
        playerSheetBehavior.setHideable(false);
        playerSheetBehavior.setSkipCollapsed(false);
        playerSheetBehavior.setSaveFlags(BottomSheetBehavior.SAVE_NONE);
        playerSheetBehavior.setDraggable(true);
        updateCollapsedPeekHeight();
        playerSheetBehavior.setPeekHeight(collapsedPeekHeight, false);
        playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        playerSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

        playerSheet.setOnClickListener(v -> expandPlayer());
        playerHeader.setOnClickListener(v -> expandPlayer());
        playerTitleView.setVisibility(View.VISIBLE);
        playerArtistView.setOnClickListener(v -> {});

        shuffleButton.setOnClickListener(v -> toggleShuffle());
        backButton.setOnClickListener(v -> handleBackPress());
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        forwardButton.setOnClickListener(v -> handleForwardPress());
        repeatButton.setOnClickListener(v -> toggleRepeat());
        updateControlTint(shuffleButton, false);
        updateControlTint(repeatButton, false);
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
        playerSheetBehavior.setPeekHeight(collapsedPeekHeight, false);
        playerSheetBehavior.setHideable(false);
        playerSheetBehavior.setDraggable(true);
        playerSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        playerSheet.setVisibility(View.VISIBLE);
        playerSheet.bringToFront();
        playerSheet.setAlpha(1f);
        playerSheet.setTranslationZ(100f);
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

    private void startPlayback(int index) {
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
        releasePlayer();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                updatePlayPauseIcon(true);
                startArtworkSpin();
                updateNotification(true);
                ensurePlayerSheetVisible();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                if (isRepeatEnabled) {
                    mp.seekTo(0);
                    mp.start();
                    startArtworkSpin();
                    updateNotification(true);
                    ensurePlayerSheetVisible();
                } else {
                    playNextTrack();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(this, "Unable to play song", Toast.LENGTH_SHORT).show();
            updatePlayPauseIcon(false);
            updateNotification(false);
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updatePlayPauseIcon(false);
            pauseArtworkSpin();
            updateNotification(false);
        } else {
            mediaPlayer.start();
            updatePlayPauseIcon(true);
            startArtworkSpin();
            updateNotification(true);
        }
    }

    private void toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled;
        updateControlTint(shuffleButton, isShuffleEnabled);
        if (isShuffleEnabled) {
            playRandomTrack();
        }
        updateNotification(mediaPlayer != null && mediaPlayer.isPlaying());
    }

    private void toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled;
        updateControlTint(repeatButton, isRepeatEnabled);
        updateNotification(mediaPlayer != null && mediaPlayer.isPlaying());
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
        int nextIndex;
        if (isShuffleEnabled) {
            nextIndex = randomIndexDifferentFrom(currentSongIndex);
        } else {
            nextIndex = (currentSongIndex + 1) % songs.size();
        }
        startPlayback(nextIndex);
        updateNotification(mediaPlayer != null && mediaPlayer.isPlaying());
    }

    private void playPreviousTrack() {
        if (songs.isEmpty()) return;
        int prevIndex;
        if (isShuffleEnabled) {
            prevIndex = randomIndexDifferentFrom(currentSongIndex);
        } else {
            prevIndex = currentSongIndex - 1;
            if (prevIndex < 0) {
                prevIndex = songs.size() - 1;
            }
        }
        startPlayback(prevIndex);
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
    }

    private void updateControlTint(ImageButton button, boolean active) {
        if (button == null) return;
        int color = ContextCompat.getColor(this, active ? R.color.player_accent : R.color.player_control);
        button.setColorFilter(color);
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
        } else {
            artworkView.setImageResource(R.mipmap.ic_launcher);
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
    }

    private void pauseArtworkSpin() {
        if (artworkAnimator != null && artworkAnimator.isRunning()) {
            artworkAnimator.pause();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {
                // Player might not be prepared yet.
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
        ensurePlayerSheetVisible();
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private void expandPlayer() {
        if (playerSheetBehavior == null) return;
        playerSheetBehavior.setSkipCollapsed(false);
        playerSheetBehavior.setHideable(false);
        playerSheetBehavior.setDraggable(true);
        playerSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showSection(View targetSection) {
        setSectionVisibility(tracksContainer, targetSection == tracksContainer);
        setSectionVisibility(playlistsContainer, targetSection == playlistsContainer);
        setSectionVisibility(albumsContainer, targetSection == albumsContainer);
        setSectionVisibility(settingsContainer, targetSection == settingsContainer);

        if (targetSection == tracksContainer) {
            if (songs.isEmpty()) {
                showEmptyState(hasReadMediaPermission() ? R.string.empty_state_no_music : R.string.empty_state_no_permission);
            } else {
                hideEmptyState();
            }
            if (!songs.isEmpty()) {
                scheduleAlphabetHide();
            }
            ensurePlayerSheetVisible();
        }
        // Keep player visible even when switching sections
        hideEmptyState();
        hideAlphabetScroller();
        ensurePlayerSheetVisible();
    }

    private void setSectionVisibility(View section, boolean visible) {
        if (section != null) {
            section.setVisibility(visible ? View.VISIBLE : View.GONE);
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
