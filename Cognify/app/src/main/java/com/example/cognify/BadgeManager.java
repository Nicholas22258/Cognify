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

import java.util.ArrayList;

public class BadgeManager {

    public ArrayList<Badge> getAllBadges() {
        ArrayList<Badge> badges = new ArrayList<>();
        badges.add(new Badge("Beginner", "Welcome aboard!", 0, false));
        badges.add(new Badge("Quick Learner", "Earn 100 XP", 100, false));
        badges.add(new Badge("Knowledge Seeker", "Earn 250 XP", 250, false));
        badges.add(new Badge("Quiz Master", "Earn 500 XP", 500, false));
        badges.add(new Badge("Advanced Scholar", "Earn 1000 XP", 1000, false));
        badges.add(new Badge("Expert Learner", "Earn 2000 XP", 2000, false));
        return badges;
    }

    public boolean checkBadgeEarned(int currentXP, Badge badge) {
        return currentXP >= badge.getRequiredXP();
    }

    public ArrayList<Badge> getEarnedBadges(int currentXP) {
        ArrayList<Badge> earned = new ArrayList<>();
        for (Badge badge : getAllBadges()) {
            boolean isEarned = checkBadgeEarned(currentXP, badge);
            badge.setEarned(isEarned);
            if (isEarned) {
                earned.add(badge);
            }
        }
        return earned;
    }
}