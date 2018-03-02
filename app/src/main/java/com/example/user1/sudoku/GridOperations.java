package com.example.user1.sudoku;

//import android.util.Log;

//import java.util.Arrays;

/**
 *
 */

public class GridOperations {


    //generates a solution
    static public byte[][] getSolution(byte size, byte sub_grid_size) {
        byte[][] solution = new byte[size][size];

        //fills first row
        fillFirstRow(solution, size);

        //get the order used to stagger each row to make a valid solution
        byte[] fill_order = getFillOrder(size, sub_grid_size);

        //fills the rest of the board
        for (byte row = 1; row < size; row++) {
            fillRow(solution, size, row, fill_order[row]);
        }

        //Shuffles the board several times to get a truly randomized gird.
        //Works on the premise that as long as sub grids are kept together,
        // the solution can never be erroneous.
        solution = shuffleRows(solution, size, sub_grid_size);
        solution = shuffleColumns(solution, size, sub_grid_size);
        solution = shuffleGridRows(solution, size, sub_grid_size);
        solution = shuffleGridColumns(solution, size, sub_grid_size);

        return solution;
    }

    //shuffles the rows in each sub grid
    private static byte[][] shuffleRows(byte[][] solution, byte size, byte sub_grid_size) {
        byte[][] shuffle = new byte[size][size];

        for (byte grid = 0; grid < sub_grid_size; grid++) {
            byte[] order = tripleOrder();
            for (byte row = 0; row < sub_grid_size; row++)
                System.arraycopy(solution[order[row] + (grid * sub_grid_size)], 0,
                        shuffle[row + (grid * sub_grid_size)], 0, size);
        }

        return shuffle;
    }

    //shuffles the sub grid rows (moves 3 at a time across the board)
    private static byte[][] shuffleGridRows(byte[][] solution, byte size, byte sub_grid_size) {
        byte[][] shuffle = new byte[size][size];

        byte[] order = tripleOrder();
        for (byte grid = 0; grid < sub_grid_size; grid++) {
            for (byte row = 0; row < sub_grid_size; row++)
                System.arraycopy(solution[row + (order[grid] * sub_grid_size)], 0,
                        shuffle[row + (grid * sub_grid_size)], 0, size);
        }

        return shuffle;
    }

    //shuffles the columns in each sub grid
    private static byte[][] shuffleColumns(byte[][] solution, byte size, byte sub_grid_size) {
        byte[][] shuffle = new byte[size][size];

        for (byte grid = 0; grid < sub_grid_size; grid++) {
            byte[] order = tripleOrder();
            for (byte column = 0; column < sub_grid_size; column++)
                for (byte row = 0; row < size; row++)
                    shuffle[row][column+(grid * sub_grid_size)] =
                            solution [row] [order[column]+(grid * sub_grid_size)];
        }

        return shuffle;
    }

    //shuffles the sub grid columns (moves 3 at a time across the board)
    private static byte[][] shuffleGridColumns(byte[][] solution, byte size, byte sub_grid_size) {
        byte[][] shuffle = new byte[size][size];

        byte[] order = tripleOrder();
        for (byte grid = 0; grid < sub_grid_size; grid++) {
            for (byte column = 0; column < sub_grid_size; column++)
                for (byte row = 0; row < size; row++)
                    shuffle[row][column+(grid * sub_grid_size)] =
                            solution [row] [column+(order[grid] * sub_grid_size)];
        }

        return shuffle;
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
    static private void fillFirstRow(byte[][] solution, byte size) {

        //keeps track of what numbers have been used
        boolean[] used_numbers = new boolean[size];

        //used to step through columns in the top row
        for (byte x = 0; x < size; x++) {

            //get random number
            byte random_number = getRandomNumber(size);

            //count up or down (randomized) until an unused number is found.
            if (used_numbers[random_number]) {
                byte direction = getRandomNumber((byte)2);
                if (direction == 0)
                    while (used_numbers[random_number])
                        random_number = getPrevious(random_number, size);
                else
                    while (used_numbers[random_number])
                        random_number = getNext(random_number, size);
            }

            //assign it
            used_numbers[random_number] = true;
            solution[0][x] = ++random_number;
        }
    }

    static private void fillRow(byte[][] solution, byte size, byte row, byte start_digit) {
        for (byte x = 0; x < size; x++) {
            solution[row][x] = solution[0][start_digit];
            start_digit = getNext(start_digit, size);
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
    static private byte[] getFillOrder(byte size, byte sub_grid_size) {
        byte[] order = new byte[size];
        for (byte step = 0; step < size; step++) {
            int mod = step % sub_grid_size;
            int div = step / sub_grid_size;
            order[step] = (byte) ((sub_grid_size*mod) + div);
        }
        return order;
    }

    //sets a visible number and marks it's presence in the possibility grids
    public static void setNumber(SudokuBoard board, byte x, byte y,
                                 byte size, byte sub_grid_size, byte number, int blue) {
        //viably set the number
        board.visible_board[x][y].setText(String.valueOf(number));

        //reduce the number for array indexing
        number--;

        //reset possible count to prevent the tile from turning blue
        board.possible_count[x][y] = 0;

        //used to calculate starting point for sub grid elimination
        byte sub_grid_offset_x = (byte)((x/sub_grid_size)*sub_grid_size);
        byte sub_grid_offset_y = (byte)((y/sub_grid_size)*sub_grid_size);

        //runs 9 times
        for (byte step = 0; step < size; step++) {

            //eliminate the number as possible (row)
            //sets blue background if only 1 left possible
            if (board.possible[x][step][number]) {
                board.possible[x][step][number] = false;
                if (--board.possible_count[x][step] == 1)
                    board.visible_board[x][step].setBackgroundColor(blue);
            }

            //eliminate the number as possible (column)
            //sets blue background if only 1 left possible
            if (board.possible[step][y][number]) {
                board.possible[step][y][number] = false;
                if (--board.possible_count[step][y] == 1)
                    board.visible_board[step][y].setBackgroundColor(blue);
            }

            //eliminate the number as possible (sub grid)
            //sets blue background if only 1 left possible
            byte sub_grid_x = (byte)(sub_grid_offset_x + (step/sub_grid_size));
            byte sub_grid_y = (byte)(sub_grid_offset_y + (step%sub_grid_size));
            if (board.possible[sub_grid_x][sub_grid_y][number]) {
                board.possible[sub_grid_x][sub_grid_y][number] = false;
                if (--board.possible_count[sub_grid_x][sub_grid_y] == 1)
                    board.visible_board[sub_grid_x][sub_grid_y].setBackgroundColor(blue);
            }

            //eliminate all numbers as possible in the square
            board.possible[x][y][step] = false;
        }

        //reset possible count
        board.possible_count[x][y] = 0;
    }
}
