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

public class UserProgress {
    private static int currentXP = 350;

    public static int getCurrentXP() {
        return currentXP;
    }

    public static void setCurrentXP(int xp) {
        if (xp < 0) {
            currentXP = 0;
        } else {
            currentXP = xp;
        }
    }

    public static void addXP(int amount) {
        if (amount <= 0) return;
        currentXP += amount;
    }
}