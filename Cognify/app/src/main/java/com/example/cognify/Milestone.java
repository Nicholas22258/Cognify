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

public class Milestone {
    // Firestore properties (for database storage)
    private String id;          
    private int order;          
    
    // Display properties (for UI listing)
    private String name;
    private String description;
    private int xpReward;
    private boolean completed;
    private String status;
    
    // XP progression properties (for level system)
    private int level;
    private String title;
    private int requiredXP;
    private int maxXP;

    // Empty constructor required for Firestore
    public Milestone() {}

    // Constructor for Firebase-based milestones (SweetInvasion's approach)
    public Milestone(String name, String description, int xpReward, int order) {
        this.name = name;
        this.description = description;
        this.xpReward = xpReward;
        this.order = order;
        this.completed = false;
        this.status = "Locked";
        this.level = order;
        this.title = name;
        this.requiredXP = 0;
        this.maxXP = xpReward;
    }

    // Constructor for UI-based milestones (simple XP calculation)
    public Milestone(String name, String description, int xpReward, boolean completed, String status) {
        this.name = name;
        this.description = description;
        this.xpReward = xpReward;
        this.completed = completed;
        this.status = status;
        this.level = 0;
        this.title = name;
        this.requiredXP = 0;
        this.maxXP = xpReward;
        this.order = 0;
    }

    // Constructor for XP-based progression system (MilestoneManager)
    public Milestone(int level, String title, int requiredXP, int maxXP) {
        this.level = level;
        this.title = title;
        this.requiredXP = requiredXP;
        this.maxXP = maxXP;
        this.name = title;
        this.description = "";
        this.xpReward = maxXP;
        this.completed = false;
        this.status = "";
        this.order = level;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getRequiredXP() { return requiredXP; }
    public void setRequiredXP(int requiredXP) { this.requiredXP = requiredXP; }

    public int getMaxXP() { return maxXP; }
    public void setMaxXP(int maxXP) { this.maxXP = maxXP; }
}