package com.example.cognify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class WordChoicesAdapter extends RecyclerView.Adapter<WordChoicesAdapter.WordViewHolder> {

    private final List<String> wordList;
    private final LayoutInflater inflater;
    private OnWordClickListener listener;

    // Interface for click events
    public interface OnWordClickListener {
        void onWordClick(String word, int position);
    }

    public WordChoicesAdapter(Context context, List<String> wordList, OnWordClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.wordList = wordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_word_choice, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        String word = wordList.get(position);
        holder.wordTextView.setText(word);
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    // ViewHolder class
    class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView;

        WordViewHolder(View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);

            // Set the click listener for the item
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onWordClick(wordList.get(position), position);
                }
            });
        }
    }
}
    