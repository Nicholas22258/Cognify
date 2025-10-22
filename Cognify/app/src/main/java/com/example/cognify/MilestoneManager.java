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

public class MilestoneManager {

    public ArrayList<Milestone> getAllMilestones() {
        ArrayList<Milestone> milestones = new ArrayList<>();
        milestones.add(new Milestone(1, "Beginner", 0, 200));
        milestones.add(new Milestone(2, "Learner", 200, 500));
        milestones.add(new Milestone(3, "Advanced Learner", 500, 1000));
        milestones.add(new Milestone(4, "Expert", 1000, 2000));
        milestones.add(new Milestone(5, "Master", 2000, Integer.MAX_VALUE));
        return milestones;
    }

    public Milestone getCurrentMilestone(int currentXP) {
        for (Milestone milestone : getAllMilestones()) {
            if (currentXP >= milestone.getRequiredXP() && currentXP < milestone.getMaxXP()) {
                return milestone;
            }
        }
        return getAllMilestones().get(0);
    }

    public int calculateProgress(int currentXP, Milestone milestone) {
        int min = milestone.getRequiredXP();
        int maxExclusive = milestone.getMaxXP();
        int range = Math.max(1, maxExclusive - min);
        int progress = (int) (((double) (currentXP - min) / range) * 100.0);
        if (progress < 0) return 0;
        if (progress > 100) return 100;
        return progress;
    }
}