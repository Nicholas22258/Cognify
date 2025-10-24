package com.example.cognify;

import com.google.firebase.Timestamp;

public class Report {
        private String id;
        private String userId;
        private String username;
        private String message;
        private Timestamp dateSent;
        private boolean addressed;

        // Required empty constructor for Firestore
        public Report() {}

        // Getters & Setters for all fields
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Timestamp getDateSent() { return dateSent; }
        public void setDateSent(Timestamp dateSent) { this.dateSent = dateSent; }

        public boolean isAddressed() { return addressed; }
        public void setAddressed(boolean addressed) { this.addressed = addressed; }
}
