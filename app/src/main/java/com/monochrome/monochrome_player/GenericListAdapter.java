package com.monochrome.monochrome_player;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import java.util.List;

public class GenericListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> itemList;
    private OnItemClickListener listener;
    private ThemeColors currentTheme;

    public interface OnItemClickListener {
        void onItemClick(ListItem item);
    }

    public GenericListAdapter(List<ListItem> itemList) {
        this.itemList = itemList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTheme(ThemeColors theme) {
        this.currentTheme = theme;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ListItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == ListItem.TYPE_ARTIST) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_artist, parent, false);
            return new ArtistViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_album, parent, false);
            return new AlbumViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = itemList.get(position);
        
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.letterText.setText(item.getHeader());
            if (currentTheme != null) {
                headerHolder.letterText.setTextColor(currentTheme.accentColor);
            }
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        } else if (holder instanceof ArtistViewHolder) {
            Artist artist = item.getArtist();
            ArtistViewHolder artistHolder = (ArtistViewHolder) holder;
            artistHolder.nameText.setText(artist.getName());
            artistHolder.countText.setText(artist.getSongCount() + " songs");
            if (currentTheme != null) {
                artistHolder.nameText.setTextColor(currentTheme.onSurfaceColor);
                artistHolder.countText.setTextColor(currentTheme.onSurfaceVariantColor);
                artistHolder.itemView.setBackgroundColor(currentTheme.listSurfaceColor);
                artistHolder.itemView.setForeground(ContextCompat.getDrawable(
                        artistHolder.itemView.getContext(), android.R.drawable.list_selector_background));
            }
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        } else if (holder instanceof AlbumViewHolder) {
            Album album = item.getAlbum();
            AlbumViewHolder albumHolder = (AlbumViewHolder) holder;
            albumHolder.nameText.setText(album.getName());
            albumHolder.artistText.setText(album.getArtist());
            albumHolder.countText.setText(album.getSongCount() + " songs");
            if (currentTheme != null) {
                albumHolder.nameText.setTextColor(currentTheme.onSurfaceColor);
                albumHolder.artistText.setTextColor(currentTheme.onSurfaceColor);
                albumHolder.countText.setTextColor(currentTheme.onSurfaceVariantColor);
                albumHolder.itemView.setBackgroundColor(currentTheme.listSurfaceColor);
                albumHolder.itemView.setForeground(ContextCompat.getDrawable(
                        albumHolder.itemView.getContext(), android.R.drawable.list_selector_background));
            }
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public ListItem getItemAtPosition(int position) {
        if (position >= 0 && position < itemList.size()) {
            return itemList.get(position);
        }
        return null;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView letterText;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            letterText = itemView.findViewById(R.id.headerLetter);
        }
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView countText;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.artistName);
            countText = itemView.findViewById(R.id.artistSongCount);
        }
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView artistText;
        TextView countText;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.albumName);
            artistText = itemView.findViewById(R.id.albumArtist);
            countText = itemView.findViewById(R.id.albumSongCount);
        }
    }
}
