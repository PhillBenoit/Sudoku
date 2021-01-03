package com.example.user1.sudoku;

import java.util.ArrayList;
import java.util.Random;

public class Gen {

    /**
     * provides randomization
     */
    private static Random rng = new Random();

    /**
     * holds size of the board
     */
    private static int size;

    /**
     * Returns a randomly generated GenBoard
     *
     * @param size square number to be used for the total board dimensions
     *
     * @return randomized board
     */
    public static GenBoard GetBoard(int size) {

        //creates blank board in memory
        GenBoard board = new GenBoard(size);

        //sets the global board size
        Gen.size = size;

        //fill first row
        FirstRow(board);

        //fills the rest
        board = FillBoard(board);

        //return completed board
        return board;
    }

    /**
     * fills in the first row of the board
     * used to cut down on recursion calls to fill rest of the board
     *
     * @param board the GenBoard to be filled
     */
    private static void FirstRow(GenBoard board) {

        //array list with remaining values to be used
        ArrayList<Integer> values = new ArrayList<>();

        //cursor to fill in the top row (row defaults to 0)
        SquareSolution cursor = new SquareSolution();

        //fill the list of remaining values
        for (int x = 1; x<size+1; x++) values.add(x);

        //step through the columns of the first row
        for(cursor.column = 0; cursor.column < size; cursor.column++) {

            //get a random index to retrieve a remaining value
            int rnd_index = rng.nextInt(values.size());

            //set the value for the cursor
            cursor.value = values.get(rnd_index);

            //remove the used value from the list of remaining values
            values.remove(rnd_index);

            //assign the cursor to the board
            board.Assign(cursor);
        }

    }

    /**
     * Recursive algorithm used to fill board
     *
     * @param board GenBoard to be filled

     * @return null if there is no solution or address of solved puzzle
     */
    private static GenBoard FillBoard(GenBoard board) {

        //create a copy of the board to work on
        GenBoard testBoard = null;

        //get a list of guesses to work with
        ArrayList<SquareSolution> guesses = FindTestGuesses(board);

        //run through guesses unless a solution has been found
        while (!guesses.isEmpty() && testBoard == null) {

            //index for a guess
            int guess = rng.nextInt(guesses.size());

            //create a copy of the board as it exists
            testBoard = (GenBoard) board.clone();

            //assign the guess
            testBoard.Assign(guesses.get(guess));

            //remove the guess from the list of guesses
            guesses.remove(guess);

            //unless the board is solved, attempt to fill it further
            if (!testBoard.isSolved()) testBoard = FillBoard(testBoard);
        }

        //returns null or a solved board
        return testBoard;
    }

    /**
     * Probes the board for possible solutions
     * counts the possible solutions for each square on the board
     * maintains a list of the lowest
     * returns list of possible values for a random square from the list
     *
     * @param board GenBoard to be probed
     *
     * @return possible solutions for a random square with lowest number of possibilities
     */
    private static ArrayList<SquareSolution> FindTestGuesses(GenBoard board) {

        //declare space in memory for the return list
        ArrayList<SquareSolution> return_list = new ArrayList<>();

        //set lowest to max value
        int lowest = size;

        //local address for a square on the GenBoard's possibilities
        boolean[] possible;

        //step through the board
        for(int row = 0; row < size; row++)
            for(int column = 0; column < size; column++) {

                //if square is used, move on
                if (board.getSquare(row, column) != 0) continue;

                //get possibilities for current position
                possible = board.getPossible(row, column);

                //count possibilities
                int count = CountBits(possible);

                //if count of possibilities is lower than current lowest
                if (count<lowest) {

                    //make it the lowest
                    lowest = count;

                    //clear the list
                    return_list.clear();
                }

                //add SquareSolution to the list if it's count is equal to the lowest
                if (count == lowest)
                    return_list.add(new SquareSolution(row, column, 0));
            }

        //select a random square from the list to return.
        int row, column, random_selection;
        random_selection = rng.nextInt(return_list.size());
        row = return_list.get(random_selection).row;
        column = return_list.get(random_selection).column;
        return_list.clear();
        possible = board.getPossible(row, column);

        //fill return list with values for selected square
        for(int cursor = 0; cursor < size; cursor++)
            if (possible[cursor])
                return_list.add(new SquareSolution(row, column, cursor+1));

        return return_list;
    }

    /**
     * counts true bits in arrays of booleans
     *
     * @param b array of booleans to be counted
     *
     * @return number of true bits counted
     */
    private static int CountBits(boolean[] b) {
        int count = 0;
        for(boolean bit:b) if (bit) count++;
        return count;
    }

}
