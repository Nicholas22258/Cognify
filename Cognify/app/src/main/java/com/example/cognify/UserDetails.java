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

public class UserDetails {
    private static String username;
    private static String userID;
    private static String email;
    private static int totalPoints;

    private static UserDetails[] ud = new UserDetails[1];

    public UserDetails(String username, String userID, String email, int totalPoints){
        this.username = username;
        this.userID = userID;
        this.email = email;
        this.totalPoints = totalPoints;
    }

    public static void setUD(UserDetails userDetails){
        ud[0] = userDetails;
    }

    public static UserDetails getUD(){
        return ud[0];
    }

    public static String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public static String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        int matchingGamePoints = GameDetailsTracker.getMatchingGamePoints();
        int definitionBuilderPoints = GameDetailsTracker.getDefinitionBuilderPoints();
        int crossWordPoints = GameDetailsTracker.getCrosswordPoints();
        this.totalPoints = matchingGamePoints + definitionBuilderPoints + crossWordPoints;

//        this.totalPoints = totalPoints;
    }
}
