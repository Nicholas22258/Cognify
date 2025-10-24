package com.example.cognify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserActionListener listener;

    // Interface for handling user actions
    public interface OnUserActionListener {
        void onViewDetails(User user);
        void onSuspendUser(User user);
        void onActivateUser(User user);
        void onMakeAdmin(User user); // New callback
    }

    // Constructor
    public UserAdapter(Context context, List<User> userList, OnUserActionListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Set user data
        holder.userNameText.setText(user.getUsername() != null ? user.getUsername() : "Unknown");
        holder.userEmailText.setText(user.getEmail() != null ? user.getEmail() : "No email");

        // Format join date
        if (user.getJoinDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            holder.joinDateText.setText("Joined: " + sdf.format(user.getJoinDate()));
        } else {
            holder.joinDateText.setText("Joined: Unknown");
        }

        // Set stats
        holder.materialsUploadedText.setText(String.valueOf(user.getTotalMaterialsUploaded()));
        holder.gamesPlayedText.setText(String.valueOf(user.getTotalGamesPlayed()));
        holder.totalPointsText.setText(String.valueOf(user.getTotalPoints()));

        // Set status badge and button based on user status
        if (user.isAdmin()) {
            holder.statusBadge.setText("Admin");
            holder.statusBadge.setBackgroundResource(R.drawable.badge_admin);
            holder.suspendButton.setVisibility(View.GONE);
            holder.makeAdminButton.setVisibility(View.GONE); // Hide Make Admin for admins
        } else if (user.isActive()) {
            holder.statusBadge.setText("Active");
            holder.statusBadge.setBackgroundResource(R.drawable.badge_active);
            holder.suspendButton.setText("Suspend");
            holder.suspendButton.setVisibility(View.VISIBLE);
            holder.suspendButton.setBackgroundTintList(
                    context.getResources().getColorStateList(android.R.color.holo_red_dark));
            holder.makeAdminButton.setVisibility(View.VISIBLE);
        } else {
            holder.statusBadge.setText("Inactive");
            holder.statusBadge.setBackgroundResource(R.drawable.badges_suspended);
            holder.suspendButton.setText("Activate");
            holder.suspendButton.setVisibility(View.VISIBLE);
            holder.suspendButton.setBackgroundTintList(
                    context.getResources().getColorStateList(android.R.color.holo_green_dark));
            holder.makeAdminButton.setVisibility(View.VISIBLE);
        }

        // Button click listeners
        holder.viewDetailsButton.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetails(user);
        });

        holder.suspendButton.setOnClickListener(v -> {
            if (listener != null) {
                if (user.isActive()) listener.onSuspendUser(user);
                else listener.onActivateUser(user);
            }
        });

        holder.makeAdminButton.setOnClickListener(v -> {
            if (listener != null) listener.onMakeAdmin(user);
        });

        // Card click listener for viewing details
        holder.userCard.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetails(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // ViewHolder class
    static class UserViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView userCard;
        CircleImageView userProfileImage;
        TextView userNameText, userEmailText, joinDateText;
        TextView statusBadge;
        TextView materialsUploadedText, gamesPlayedText, totalPointsText;
        MaterialButton viewDetailsButton, suspendButton, makeAdminButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userCard = itemView.findViewById(R.id.userCard);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            userNameText = itemView.findViewById(R.id.userNameText);
            userEmailText = itemView.findViewById(R.id.userEmailText);
            joinDateText = itemView.findViewById(R.id.joinDateText);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            materialsUploadedText = itemView.findViewById(R.id.materialsUploadedText);
            gamesPlayedText = itemView.findViewById(R.id.gamesPlayedText);
            totalPointsText = itemView.findViewById(R.id.totalPointsText);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            suspendButton = itemView.findViewById(R.id.suspendButton);
            makeAdminButton = itemView.findViewById(R.id.makeAdminButton); // new
        }
    }

    // Method to update the list
    public void updateList(List<User> newList) {
        this.userList.clear();
        this.userList.addAll(newList);
        notifyDataSetChanged();
    }
}
