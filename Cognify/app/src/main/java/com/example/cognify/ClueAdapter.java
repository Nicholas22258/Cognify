package com.example.cognify;

/*
 * @Author Nicholas Leong        EDUV4551823
 * @Author Aarya Manowah         be.2023.q4t9k6
 * @Author Nyasha Masket        BE.2023.R3M0Y0
 * @Author Sakhile Lesedi Mnisi  BE.2022.j9f3j4
 * @Author Dominic Newton       EDUV4818782
 * @Author Kimberly Sean Sibanda EDUV4818746
 *
 * Supervisor: Stacey Byrne      Stacey.byrne@eduvos.com
 * */

//import static androidx.appcompat.graphics.drawable.DrawableContainerCompat.Api21Impl.getResources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClueAdapter extends RecyclerView.Adapter<ClueAdapter.ClueViewHolder> {
    private List<String> dataSet;

    public ClueAdapter(List<String> dataSet){
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public ClueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cw_clue_item, parent, false);
        return new ClueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClueViewHolder holder, int position) {
        holder.textViewClue.setText(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    static class ClueViewHolder extends RecyclerView.ViewHolder{
        TextView textViewClue;

        public ClueViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClue = itemView.findViewById(R.id.textViewClue);
            textViewClue.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
        }

//        public void bind(final Word word, final View.OnClickListener listener, boolean isSelected){
//            String clueNumber
//        }
    }


}
