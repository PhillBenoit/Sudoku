package com.example.user1.sudoku;

import android.widget.TextView;

import java.util.Arrays;

/**
 *  Object with all board information
 */

public class SudokuBoard {

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

    public SudokuBoard(byte size, byte sub_grid_size) {

        //text boxes that make up the game grid
        visible_board = new TextView[size][size];

        //calculates random solution
        solution = GridOperations.getSolution(size, sub_grid_size);

        //starts all positions as possible
        possible = new boolean[size][size][size];
        for (boolean[][] row:possible)
            for (boolean[] possible_number:row)
                Arrays.fill(possible_number, true);

        //starts count at 9
        possible_count = new byte[size][size];
        for (byte[] row:possible_count) Arrays.fill(row, size);
    }

    public SudokuBoard(byte size, byte sub_grid_size, TextView[][] visible) {

        //text boxes that make up the game grid
        visible_board = visible;

        //calculates random solution
        solution = GridOperations.getSolution(size, sub_grid_size);

        //starts all positions as possible
        possible = new boolean[size][size][size];
        for (boolean[][] row:possible)
            for (boolean[] possible_number:row)
                Arrays.fill(possible_number, true);

        //starts count at 9
        possible_count = new byte[size][size];
        for (byte[] row:possible_count) Arrays.fill(row, size);
    }

}
