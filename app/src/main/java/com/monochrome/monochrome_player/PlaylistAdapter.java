package com.monochrome.monochrome_player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private final List<Playlist> playlists;
    private final OnItemClickListener listener;
    private final Context context;
    private OnItemLongClickListener longClickListener;
    private ThemeColors currentTheme;

    public interface OnItemClickListener { void onItemClick(Playlist p, int position); }
    public interface OnItemLongClickListener { void onItemLongClick(Playlist p, int position); }

    public PlaylistAdapter(Context ctx, List<Playlist> playlists, OnItemClickListener listener) {
        this.context = ctx;
        this.playlists = playlists;
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) { this.longClickListener = l; }
    public void setTheme(ThemeColors theme) {
        this.currentTheme = theme;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist p = playlists.get(position);
        holder.name.setText(p.getName());
        holder.count.setText(p.size() + (p.size() == 1 ? " song" : " songs"));
        Drawable thumb = null;
        if (!p.getSongPaths().isEmpty()) {
            String path = p.getSongPaths().get(0);
            thumb = getArtworkDrawableForPath(path);
        }
        if (thumb != null) holder.thumb.setImageDrawable(thumb);
        else holder.thumb.setImageResource(R.mipmap.ic_launcher);
        if (currentTheme != null) {
            holder.name.setTextColor(currentTheme.onSurfaceColor);
            holder.count.setTextColor(currentTheme.onSurfaceVariantColor);
            holder.itemView.setBackgroundColor(currentTheme.listSurfaceColor);
            holder.itemView.setForeground(ContextCompat.getDrawable(context, android.R.drawable.list_selector_background));
        }

        holder.itemView.setClickable(true);
        holder.itemView.setOnClickListener(v -> {
            android.util.Log.d("MonochromePlayer", "playlist clicked: " + p.getName());
            if (listener != null) listener.onItemClick(p, position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(p, position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() { return playlists.size(); }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView thumb;
        TextView name;
        TextView count;
        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.playlist_thumb);
            name = itemView.findViewById(R.id.playlist_name);
            count = itemView.findViewById(R.id.playlist_count);
        }
    }

    private Drawable getArtworkDrawableForPath(String path) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(path);
            byte[] art = mmr.getEmbeddedPicture();
            mmr.release();
            if (art != null && art.length > 0) {
                Bitmap bmp = BitmapFactory.decodeByteArray(art, 0, art.length);
                return new BitmapDrawable(context.getResources(), bmp);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
