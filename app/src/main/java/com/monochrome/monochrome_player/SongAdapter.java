package com.monochrome.monochrome_player;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> itemList;
    private OnItemClickListener listener;
    private OnHeaderClickListener headerClickListener;
    private OnItemLongClickListener longClickListener;

    public interface OnItemClickListener {
        void onItemClick(Song song, int position);
    }

    public interface OnHeaderClickListener {
        void onHeaderClick();
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Song song, int position);
    }

    public SongAdapter(List<ListItem> itemList) {
        this.itemList = itemList;
    }

  
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        this.headerClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener l) { this.longClickListener = l; }

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
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_song, parent, false);
            return new SongViewHolder(view);
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
                if (headerClickListener != null) {
                    headerClickListener.onHeaderClick();
                }
            });
        } else if (holder instanceof SongViewHolder) {
            Song song = item.getSong();
            SongViewHolder songHolder = (SongViewHolder) holder;
            songHolder.titleText.setText(song.getTitle());
            songHolder.artistText.setText(song.getArtist());
            if (currentTheme != null) {
                songHolder.titleText.setTextColor(currentTheme.onSurfaceColor);
                songHolder.artistText.setTextColor(currentTheme.onSurfaceVariantColor);
                songHolder.itemView.setBackgroundColor(currentTheme.listSurfaceColor);
                songHolder.itemView.setForeground(ContextCompat.getDrawable(
                        songHolder.itemView.getContext(), android.R.drawable.list_selector_background));
            }

            songHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(song, item.getPosition());
                }
            });
            songHolder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(song, item.getPosition());
                    return true;
                }
                return false;
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

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView artistText;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.songTitle);
            artistText = itemView.findViewById(R.id.songArtist);
        }
    }
}
