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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;

public class MilestoneAdapter extends BaseAdapter {
    private Context context;
    private List<Milestone> milestones;
    private LayoutInflater inflater;

    public MilestoneAdapter(Context context, List<Milestone> milestones) {
        this.context = context;
        this.milestones = milestones;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return milestones.size();
    }

    @Override
    public Object getItem(int position) {
        return milestones.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_milestone, parent, false);
            holder = new ViewHolder();
            holder.milestoneIcon = convertView.findViewById(R.id.iv_milestone_icon);
            holder.milestoneName = convertView.findViewById(R.id.tv_milestone_name);
            holder.milestoneDescription = convertView.findViewById(R.id.tv_milestone_description);
            holder.xpReward = convertView.findViewById(R.id.tv_xp_reward);
            holder.status = convertView.findViewById(R.id.tv_status);
            holder.progressBar = convertView.findViewById(R.id.progress_milestone);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Milestone milestone = milestones.get(position);
        holder.milestoneName.setText(milestone.getName());
        holder.milestoneDescription.setText(milestone.getDescription());
        holder.xpReward.setText("+" + milestone.getXpReward() + " XP");
        holder.status.setText(milestone.getStatus());

        // Dynamically update icon, progress, and alpha based on status
        switch (milestone.getStatus()) {
            case "Completed":
                holder.milestoneIcon.setImageResource(R.drawable.ic_milestone_completed);
                holder.progressBar.setProgress(100);
                convertView.setAlpha(1.0f);
                break;
            case "In Progress":
                holder.milestoneIcon.setImageResource(R.drawable.ic_milestone_in_progress);
                holder.progressBar.setProgress(65); // Can be calculated based on actual progress
                convertView.setAlpha(0.8f);
                break;
            case "Locked":
            default:
                holder.milestoneIcon.setImageResource(R.drawable.ic_milestone_locked);
                holder.progressBar.setProgress(0);
                convertView.setAlpha(0.5f);
                break;
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView milestoneIcon;
        TextView milestoneName;
        TextView milestoneDescription;
        TextView xpReward;
        TextView status;
        ProgressBar progressBar;
    }
}