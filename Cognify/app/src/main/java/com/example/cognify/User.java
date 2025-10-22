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

import java.util.Date;

public class User {
    private String userId;
    private String username;
    private String email;
    private String profilePicUrl;
    private Date joinDate;
    private boolean isActive;
    private boolean isAdmin;
    private int totalMaterialsUploaded;
    private int totalGamesPlayed;
    private int totalPoints;
    private Date lastActive;

    // Empty constructor required for Firestore
    public User() {
    }

    // Constructor with parameters
    public User(String userId, String username, String email, String profilePicUrl,
                Date joinDate, boolean isActive, boolean isAdmin,
                int totalMaterialsUploaded, int totalGamesPlayed, int totalPoints, Date lastActive) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.profilePicUrl = profilePicUrl;
        this.joinDate = joinDate;
        this.isActive = isActive;
        this.isAdmin = isAdmin;
        this.totalMaterialsUploaded = totalMaterialsUploaded;
        this.totalGamesPlayed = totalGamesPlayed;
        this.totalPoints = totalPoints;
        this.lastActive = lastActive;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public int getTotalMaterialsUploaded() {
        return totalMaterialsUploaded;
    }

    public void setTotalMaterialsUploaded(int totalMaterialsUploaded) {
        this.totalMaterialsUploaded = totalMaterialsUploaded;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public void setTotalGamesPlayed(int totalGamesPlayed) {
        this.totalGamesPlayed = totalGamesPlayed;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Date getLastActive() {
        return lastActive;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", profilePicUrl='" + profilePicUrl + '\'' +
                ", joinDate=" + joinDate +
                ", isActive=" + isActive +
                ", isAdmin=" + isAdmin +
                ", totalMaterialsUploaded=" + totalMaterialsUploaded +
                ", totalGamesPlayed=" + totalGamesPlayed +
                ", totalPoints=" + totalPoints +
                ", lastActive=" + lastActive +
                '}';
    }
}