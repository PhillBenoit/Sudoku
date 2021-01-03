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

        //fills first row with random series of numbers
        fillFirstRow();

        //get the order used to stagger each row to make a valid solution
        byte[] fill_order = getFillOrder();

        //fills the rest of the board
        for (byte row = 1; row < BOARD_SIZE; row++)
            fillRow(row, fill_order[row]);

        //Shuffles the board several times to get a truly randomized gird.
        //Works on the premise that as long as sub grids are kept together,
        // the solution can never be erroneous.
        shuffleRows();
        shuffleColumns();
        shuffleGridRows();
        shuffleGridColumns();
    }

    //shuffles the rows in each sub grid
    private void shuffleRows() {
        byte[][] shuffle = new byte[BOARD_SIZE][BOARD_SIZE];

        for (byte grid = 0; grid < SUB_GRID_SIZE; grid++) {
            byte[] order = tripleOrder();
            for (byte row = 0; row < SUB_GRID_SIZE; row++)
                System.arraycopy(solution[order[row] + (grid * SUB_GRID_SIZE)], 0,
                        shuffle[row + (grid * SUB_GRID_SIZE)], 0, BOARD_SIZE);
        }

        solution = shuffle;
    }

    //shuffles the sub grid rows (moves 3 at a time across the board)
    private void shuffleGridRows() {
        byte[][] shuffle = new byte[BOARD_SIZE][BOARD_SIZE];

        byte[] order = tripleOrder();
        for (byte grid = 0; grid < SUB_GRID_SIZE; grid++) {
            for (byte row = 0; row < SUB_GRID_SIZE; row++)
                System.arraycopy(solution[row + (order[grid] * SUB_GRID_SIZE)], 0,
                        shuffle[row + (grid * SUB_GRID_SIZE)], 0, BOARD_SIZE);
        }

        solution = shuffle;
    }

    //shuffles the columns in each sub grid
    private void shuffleColumns() {
        byte[][] shuffle = new byte[BOARD_SIZE][BOARD_SIZE];

        for (byte grid = 0; grid < SUB_GRID_SIZE; grid++) {
            byte[] order = tripleOrder();
            for (byte column = 0; column < SUB_GRID_SIZE; column++)
                for (byte row = 0; row < BOARD_SIZE; row++)
                    shuffle[row][column+(grid * SUB_GRID_SIZE)] =
                            solution [row] [order[column]+(grid * SUB_GRID_SIZE)];
        }

        solution = shuffle;
    }

    //shuffles the sub grid columns (moves 3 at a time across the board)
    private void shuffleGridColumns() {
        byte[][] shuffle = new byte[BOARD_SIZE][BOARD_SIZE];

        byte[] order = tripleOrder();
        for (byte grid = 0; grid < SUB_GRID_SIZE; grid++) {
            for (byte column = 0; column < SUB_GRID_SIZE; column++)
                for (byte row = 0; row < BOARD_SIZE; row++)
                    shuffle[row][column+(grid * SUB_GRID_SIZE)] =
                            solution [row] [column+(order[grid] * SUB_GRID_SIZE)];
        }

        solution = shuffle;
    }

    //generates a random order for 0-2.  One of the only parts of the program that does not scale
    static private byte[] tripleOrder() {
        byte[] order = new byte[3];

        //generate first two numbers
        order[0] = getRandomNumber((byte)3);
        order[1] = getRandomNumber((byte)3);

        //check if they're unique
        if (order[0] == order[1]) {

            //randomize direction to prevent always counting up from interfering with randomization
            byte direction = getRandomNumber((byte)2);

            //count down to fill remaining numbers
            if (direction == 0) {
                order[1] = getPrevious(order[1], (byte) 3);
                order[2] = getPrevious(order[1], (byte) 3);
            }

            //count up to fill remaining numbers
            else {
                order[1] = getNext(order[1], (byte) 3);
                order[2] = getNext(order[1], (byte) 3);
            }
            //fill last space if 0 and 1 were unique
        } else {
            order[2] = getNext(order[1], (byte) 3);
            if (order[2] == order[0]) order[2] = getNext(order[2], (byte) 3);
        }

        return order;
    }

    //fills the first row with randomly ordered numbers
    private void fillFirstRow() {

        //keeps track of what numbers have been used
        boolean[] used_numbers = new boolean[BOARD_SIZE];

        //used to step through columns in the top row
        for (byte x = 0; x < BOARD_SIZE; x++) {

            //get random number
            byte random_number = getRandomNumber(BOARD_SIZE);

            //count up or down (randomized) until an unused number is found.
            if (used_numbers[random_number]) {
                byte direction = getRandomNumber((byte)2);
                if (direction == 0)
                    while (used_numbers[random_number])
                        random_number = getPrevious(random_number, BOARD_SIZE);
                else
                    while (used_numbers[random_number])
                        random_number = getNext(random_number, BOARD_SIZE);
            }

            //assign it
            used_numbers[random_number] = true;
            solution[0][x] = ++random_number;
        }
    }

    //uses the top row to to copy solution into the destination
    //start digit is incremented starting with the slot specified
    private void fillRow(byte row, byte start_digit) {
        for (byte x = 0; x < BOARD_SIZE; x++) {
            solution[row][x] = solution[0][start_digit];
            start_digit = getNext(start_digit, BOARD_SIZE);
        }
    }

    //get random number from 0 to (high-1)
    public static byte getRandomNumber (byte high) {
        return (byte)(Math.random() * high);
    }

    //gets next number from 0 to (max-1)
    //loops back to 0
    public static byte getNext(byte num, byte max) {
        return (++num == max) ? 0 : num;
    }

    //get previous number from 0 to (max-1)
    //loops back to (maz-1)
    public static byte getPrevious(byte num, byte max) {
        return (--num < 0) ? --max : num;
    }

    //generates the index for the first column
    //used to stagger each row to make a valid solution
    // 0 1 2 3 4 5 6 7 8
    // 3 4 5 6 7 8 0 1 2
    // 6 7 8 0 1 2 3 4 5
    // 1 2 3 4 5 6 7 8 9
    // 4 5 6 7 8 0 1 2 3
    // 7 8 0 1 2 3 4 5 6
    // 2 3 4 5 6 7 8 0 1
    // 5 6 7 8 0 1 2 3 4
    // 8 0 1 2 3 4 5 6 7
    private byte[] getFillOrder() {
        byte[] order = new byte[BOARD_SIZE];
        for (byte step = 0; step < BOARD_SIZE; step++) {
            int mod = step % SUB_GRID_SIZE;
            int div = step / SUB_GRID_SIZE;
            order[step] = (byte) ((SUB_GRID_SIZE*mod) + div);
        }
        return order;
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
