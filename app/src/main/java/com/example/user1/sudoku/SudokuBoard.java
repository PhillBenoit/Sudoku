package com.example.user1.sudoku;

import android.annotation.SuppressLint;
import android.widget.TextView;

import java.util.Arrays;

/**
 *  Object with all board information
 */

public class SudokuBoard {

    //TODO make parcelable


    //begin members-------------------------------------------------------
    //TextViews that hold the visible board
    public TextView[][] visible_board;

    //solution the computer expects
    public byte[][] solution;

    //Indicates weather a number is possible in a specific position on the board.
    //When a number is added to the visible board, that number is set to false in
    //all adjacent squares in that row, column, and sub grid.
    public boolean[][][] possible;

    //count of remaining possible numbers in each square on the grid.
    public byte[][] possible_count;

    //counters with .toString() methods
    private Byte remaining, wrong;

    //textviews for counters
    private TextView remaining_txt, wrong_txt;

    //size of the board and size of a sub grid square
    public byte BOARD_SIZE, SUB_GRID_SIZE;

    //int value for auto solve background
    public int AUTO_BG;

    //cursor object that holds the position of the last board square tapped
    public Cursor cursor;
    //end members-----------------------------------------------------------

    private void setDefaults() {
        //count of wrong guesses
        wrong = 0;

        //calculates random solution
        getSolution();

        //starts all positions as possible
        possible = new boolean[BOARD_SIZE][BOARD_SIZE][BOARD_SIZE];
        for (boolean[][] row:possible)
            for (boolean[] possible_number:row)
                Arrays.fill(possible_number, true);

        //starts count at 9
        possible_count = new byte[BOARD_SIZE][BOARD_SIZE];
        for (byte[] row:possible_count) Arrays.fill(row, BOARD_SIZE);

    }

    //default constructor, gets constant values
    public SudokuBoard(byte size, byte sub_grid_size, int auto) {

        BOARD_SIZE = size;
        SUB_GRID_SIZE = sub_grid_size;
        AUTO_BG = auto;
        remaining = (byte) Math.pow(size, 2);
        cursor = new Cursor();

        //text boxes that make up the game grid
        visible_board = new TextView[size][size];

        setDefaults();
    }

    //constructor with existing values for starting a new game
    @SuppressWarnings("CopyConstructorMissesField")
    public SudokuBoard(SudokuBoard old_board) {

        BOARD_SIZE = old_board.BOARD_SIZE;
        SUB_GRID_SIZE = old_board.SUB_GRID_SIZE;
        AUTO_BG = old_board.AUTO_BG;
        remaining = (byte) Math.pow(BOARD_SIZE, 2);
        cursor = old_board.cursor;

        setDefaults();

        //text boxes that make up the game grid
        visible_board = old_board.visible_board;
        setCountTextViews(old_board.wrong_txt, old_board.remaining_txt);
    }

    //set textviews for counters
    @SuppressLint("SetTextI18n")
    public void setCountTextViews(TextView wrong_view, TextView remaining_view) {
        wrong_txt = wrong_view;
        wrong_txt.setText(wrong.toString());

        remaining_txt = remaining_view;
        remaining_txt.setText(remaining.toString());
    }

    //increment wrong count and update the textview
    @SuppressLint("SetTextI18n")
    public void incWrong(){
        wrong++;
        wrong_txt.setText(wrong.toString());
    }

    //decrement remaining count and update the textview
    @SuppressLint("SetTextI18n")
    public void decRemaining(){
        remaining--;
        remaining_txt.setText(remaining.toString());
    }

    //generates a solution
    private void getSolution() {
        solution = new byte[BOARD_SIZE][BOARD_SIZE];

        GenBoard generated = Gen.GetBoard(BOARD_SIZE);

        for (int x = 0; x< BOARD_SIZE; x++)
            for (int y = 0; y < BOARD_SIZE; y++)
                solution[x][y] = (byte)generated.getSquare(x,y);
    }

    //get random number from 0 to (high-1)
    public static byte getRandomNumber (byte high) {
        return (byte)(Math.random() * high);
    }

    //sets a visible number and marks it's presence in the possibility grids
    public void setNumber() {

        //viably set the number
        visible_board[cursor.x][cursor.y].
                setText(String.valueOf(solution[cursor.x][cursor.y]));

        //reduce the number for array indexing
        byte number = (byte)(solution[cursor.x][cursor.y] - 1);

        //reset possible count to prevent the tile from turning blue
        possible_count[cursor.x][cursor.y] = 0;

        //used to calculate starting point for sub grid elimination
        byte sub_grid_offset_x = (byte)((cursor.x/SUB_GRID_SIZE)*SUB_GRID_SIZE);
        byte sub_grid_offset_y = (byte)((cursor.y/SUB_GRID_SIZE)*SUB_GRID_SIZE);

        //runs 9 times
        for (byte step = 0; step < BOARD_SIZE; step++) {

            //eliminate the number as possible (row)
            //sets background if only 1 left possible
            removePossible(cursor.x, step, number);

            //eliminate the number as possible (column)
            //sets background if only 1 left possible
            removePossible(step, cursor.y, number);

            //eliminate the number as possible (sub grid)
            //sets background if only 1 left possible
            byte sub_grid_x = (byte)(sub_grid_offset_x + (step/SUB_GRID_SIZE));
            byte sub_grid_y = (byte)(sub_grid_offset_y + (step%SUB_GRID_SIZE));
            removePossible(sub_grid_x, sub_grid_y, number);

            //eliminate all numbers as possible in the square
            possible[cursor.x][cursor.y][step] = false;
        }

        //reset possible count
        possible_count[cursor.x][cursor.y] = 0;

        //decreases remaining counter
        decRemaining();
    }

    //eliminate the number as possible from specified point on the grid
    //sets background if only 1 left possible
    public void removePossible(byte row, byte column, byte number) {
        if (possible[row][column][number]) {
            possible[row][column][number] = false;
            if (--possible_count[row][column] == 1)
                visible_board[row][column].setBackgroundColor(AUTO_BG);
        }
    }

}
