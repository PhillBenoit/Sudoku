package com.example.user1.sudoku;

/**
 *  Holds byte values for x / y coordinates.
 */

public class Cursor {
    public byte x, y;

    public Cursor() {
        x = 0;
        y = 0;
    }

    public Cursor(byte x, byte y) {
        this.x = x;
        this.y = y;
    }

    public boolean isEqual(Cursor passed_cursor) {
        return x == passed_cursor.x && y == passed_cursor.y;
    }

    public void setEqual(Cursor passed_cursor) {
        x = passed_cursor.x;
        y = passed_cursor.y;
    }

}