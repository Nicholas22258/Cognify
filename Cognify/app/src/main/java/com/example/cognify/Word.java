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

enum Direction {ACROSS, DOWN};
public class Word {
    public String id; // e.g., "1A", "3D"
    public String term;
    public String definition;
    public int startRow;
    public int startCol;
    public Direction direction;
    public boolean isSolved;

    public Word(String id, String term, String definition, int startRow, int startCol, Direction direction) {
        this.id = id;
        this.term = term;
        this.definition = definition;
        this.startRow = startRow;
        this.startCol = startCol;
        this.direction = direction;
        this.isSolved = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public void setStartCol(int startCol) {
        this.startCol = startCol;
    }

//    public Direction getDirection() {
//        return direction;
//    }
//
//    public void setDirection(Direction direction) {
//        this.direction = direction;
//    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setSolved(boolean solved) {
        isSolved = solved;
    }

}
