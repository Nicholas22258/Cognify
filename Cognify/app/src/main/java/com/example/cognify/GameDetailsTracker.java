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

public class GameDetailsTracker {
    private static int matchingGameStreak;
    private static int definitionBuilderStreak;
    private static int crosswordStreak;

    private static int matchingGamePoints;
    private static int definitionBuilderPoints;
    private static int crosswordPoints;

    private static GameDetailsTracker[] gdt = new GameDetailsTracker[1];

    public GameDetailsTracker(int mgStreak, int dbStreak, int crStreak, int mgPoints, int dbPoints, int crPoints){
        this.matchingGameStreak = mgStreak;
        this.definitionBuilderStreak = dbStreak;
        this.crosswordStreak = crStreak;
        this.matchingGamePoints = mgPoints;
        this.definitionBuilderPoints = dbPoints;
        this.crosswordPoints = crPoints;
    }

    public static void setGdt(GameDetailsTracker gameDetailsTracker){
        gdt[0] = gameDetailsTracker;
    }

    public static GameDetailsTracker getGdt(){
        return gdt[0];
    }

    public static int getMatchingGameStreak() {
        return matchingGameStreak;
    }

    public void setMatchingGameStreak(int matchingGameStreak) {
        this.matchingGameStreak = matchingGameStreak;
    }

    public static int getDefinitionBuilderStreak() {
        return definitionBuilderStreak;
    }

    public void setDefinitionBuilderStreak(int definitionBuilderStreak) {
        this.definitionBuilderStreak = definitionBuilderStreak;
    }

    public static int getCrosswordStreak() {
        return crosswordStreak;
    }

    public void setCrosswordStreak(int crosswordStreak) {
        this.crosswordStreak = crosswordStreak;
    }

    //==============================Points==========================================================

    public static int getCrosswordPoints() {
        return crosswordPoints;
    }

    public void setCrosswordPoints(int crosswordPoints) {
        this.crosswordPoints = crosswordPoints;
    }

    public static int getDefinitionBuilderPoints() {
        return definitionBuilderPoints;
    }

    public void setDefinitionBuilderPoints(int definitionBuilderPoints) {
        this.definitionBuilderPoints = definitionBuilderPoints;
    }

    public static int getMatchingGamePoints() {
        return matchingGamePoints;
    }

    public void setMatchingGamePoints(int matchingGamePoints) {
        this.matchingGamePoints = matchingGamePoints;
    }
}
