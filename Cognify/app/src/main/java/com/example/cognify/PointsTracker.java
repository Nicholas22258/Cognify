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

public class PointsTracker {
    public static final int POINTS_FOR_CORRECT_ANSWER = 10;
    public static final int POINTS_FOR_INCORRECT_ANSWER = -5;
    private static int pointsForCurrentGame = 0;

    public static void addPoints(){
        pointsForCurrentGame += POINTS_FOR_CORRECT_ANSWER;
    }

    public static void addPoints(double multiplier){
        pointsForCurrentGame += (int) (POINTS_FOR_CORRECT_ANSWER * multiplier);
    }

    public static void removePoints(){
        pointsForCurrentGame += POINTS_FOR_INCORRECT_ANSWER;
    }

    public static void setPointsFromGame(int points){
        pointsForCurrentGame = points;
    }

    public static int getPointsForCurrentGame(){
        return pointsForCurrentGame;
    }

    public static void resetPoints(){
        pointsForCurrentGame = 0;
    }

    //==============================================================================================


}
