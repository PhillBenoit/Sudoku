package com.example.user1.sudoku;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
//import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class SudokuActivity extends AppCompatActivity {

    //Size of the board to be generated.  For the most part, I tried to write code that could be
    //easily adjusted to accommodate a larger board
    private static final byte BOARD_SIZE = 9;
    private static final byte SUB_GRID_SIZE = (byte) Math.sqrt(BOARD_SIZE);

    //holds the complete board object
    private SudokuBoard board;

    //----------------------------------------------------------

    //buttons for entering 1 - 9
    private Button[] input_button;

    //background colors for highlighting squares
    private int SELECTED_BG;
    private int SIMILAR_BG;

    //holds the value of an empty board space
    private CharSequence space;

    //list of squares that are highlighted to show the same number
    private ArrayList<Cursor> similar_highlighted_squares;

    //backup of the drawable background resources
    private Drawable[][] bg_backup;

    //------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //default android operations
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get android XML resources
        Resources res = getResources();

        //use screen width and scale density to calculate font size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        //int height = size.y;
        float font_size = (width * 0.09f)/res.getDisplayMetrics().scaledDensity;
        float small_font_size = font_size*0.6f;

        //background colors
        SELECTED_BG = res.getColor(R.color.selectedBG);
        SIMILAR_BG = res.getColor(R.color.similarBG);
        int AUTO_BG = res.getColor(R.color.autoBG);

        //set small font size on text boxes at the bottom of the screen
        TextView wrong_count_text = findViewById(R.id.wrong_count_textview);
        TextView remaining_count_text = findViewById(R.id.remaining_count_textview);
        TextView remaining_text = findViewById(R.id.remaining_textview);
        TextView wrong_text = findViewById(R.id.wrong_textview);

        wrong_text.setTextSize(small_font_size);
        remaining_text.setTextSize(small_font_size);
        wrong_count_text.setTextSize(small_font_size);
        remaining_count_text.setTextSize(small_font_size);


        //new board object
        board = new SudokuBoard(BOARD_SIZE, SUB_GRID_SIZE, AUTO_BG);
        board.setCountTextViews(wrong_count_text,remaining_count_text);

        //new array to hold backgrounds
        //uses the same background for every sub grid
        bg_backup = new Drawable[SUB_GRID_SIZE][SUB_GRID_SIZE];

        //array with input buttons
        input_button = new Button[BOARD_SIZE];

        for (byte row = 0; row < BOARD_SIZE; row++) {
            for (byte column = 0; column < BOARD_SIZE; column++) {
                //find ID by name
                int id = res.getIdentifier("board_"+row+column,
                        "id",
                        getPackageName());

                //board tiles from XML file
                board.visible_board[row][column] = findViewById(id);
                board.visible_board[row][column].setTextSize(font_size);
                board.visible_board[row][column].setOnClickListener(new boardClick(row, column));

            }
            //Get buttons by name.
            //Row counter used instead of button index to
            //consolidate the use of loops.
            int id = res.getIdentifier("button"+(row+1),
                    "id",
                    getPackageName());
            input_button[row] = findViewById(id);
            input_button[row].setTextSize(font_size);
            input_button[row].setText(String.valueOf(row+1));
            input_button[row].setOnClickListener(new buttonClick(row));

            //backs up default background
            int x = row / SUB_GRID_SIZE;
            int y = row % SUB_GRID_SIZE;
            bg_backup[x][y] = board.visible_board[x][y].getBackground();
        }

        //get default empty text
        space = board.visible_board[0][0].getText();

        //declare new list for similar highlighted squares
        similar_highlighted_squares = new ArrayList<>();

        startGame();
    }

    //gets a new game board while keeping the array of textviews associated with the visible board
    private void newGame() {
        board = new SudokuBoard(board);
        similar_highlighted_squares.clear();
        for (byte row = 0; row < BOARD_SIZE; row++)
            for (byte column = 0; column < BOARD_SIZE; column++) {
                //clear each visible square
                board.visible_board[row][column].setText(space);
                board.visible_board[row][column].setBackgroundColor(SELECTED_BG);
                setBG(new Cursor(row, column));
            }

        startGame();
    }

    //TODO game start adjustable options
    //reveals 3 tiles at random from each row and deactivates buttons
    private void startGame() {
        for (byte row = 0; row < BOARD_SIZE; row++) {
            board.cursor.x = row;
            input_button[row].setEnabled(false);
            boolean[] used = new boolean[BOARD_SIZE];
            for (byte revealed = 0; revealed < SUB_GRID_SIZE; revealed++) {
                byte random_number = SudokuBoard.getRandomNumber(BOARD_SIZE);
                //prevents revealing the same number twice or using spaces
                // with only one possible solution
                while (used[random_number] ||
                        board.possible_count[row][random_number] == 1)
                    random_number = SudokuBoard.getRandomNumber(BOARD_SIZE);
                used[random_number] = true;
                board.cursor.y = random_number;
                board.setNumber();
            }
        }

    }


    //Button click event
    private void buttonClickEvent(byte index) {

        //tests if clicked button is the right answer
        if (board.solution[board.cursor.x][board.cursor.y]==(index+1))
            //set number in visible grid
            board.setNumber();
        else {
            //increase the count of wrong guesses
            board.incWrong();

            //remove from possible numbers and set blue background when appropriate
            board.removePossible(board.cursor.x, board.cursor.y, index);
        }

        //simulate board click to update the background
        boardClickEvent(board.cursor);
    }

    //click listener with variable for id/index of the button clicked
    private class buttonClick implements View.OnClickListener {

        private byte id;

        buttonClick(byte id) {
            this.id = id;
        }

        @Override
        public void onClick(View view) {
            buttonClickEvent(id);
        }
    }

    //board click event
    private void boardClickEvent(Cursor location) {
        //checks for solved number as last clicked
        if (board.possible_count[board.cursor.x][board.cursor.y] == 0) {
            //clears all highlighted similar backgrounds
            for (Cursor c:similar_highlighted_squares) setBG(c);

            //clears the list of changed backgrounds
            similar_highlighted_squares.clear();

        //resets background on previously clicked location
        } else
            setBG(board.cursor);

        //sets new location
        board.cursor.setEqual(location);

        //automatically solves position if only 1 solution remaining
        if (board.possible_count[location.x][location.y] == 1)
            board.setNumber();

        //sets background to selected if blank
        if (board.visible_board[location.x][location.y].getText().equals(space))
            board.visible_board[location.x][location.y].setBackgroundColor(SELECTED_BG);
        else {
            //sets similar color background all numbers with the same value
            CharSequence value = board.visible_board[location.x][location.y].getText();
            for (byte row = 0; row < BOARD_SIZE; row++)
                for (byte column = 0; column < BOARD_SIZE; column++)
                    if (value.equals(board.visible_board[row][column].getText())) {
                        board.visible_board[row][column].setBackgroundColor(SIMILAR_BG);
                        similar_highlighted_squares.add(new Cursor(row, column));

                        //exit column loop once match is found for that row
                        column = BOARD_SIZE;
                    }
        }

        //only possible number buttons are enabled
        for (byte step = 0; step < BOARD_SIZE; step++)
            input_button[step].setEnabled(board.possible[location.x][location.y][step]);
    }

    //board click listener that holds coordinate info
    private class boardClick implements View.OnClickListener {

        private Cursor location;

        boardClick(byte x, byte y) {
            location = new Cursor(x, y);
        }

        @Override
        public void onClick(View view) {
            boardClickEvent(location);
        }
    }

    //calculates appropriate background to set from backup
    private void setBG(Cursor c) {
        board.visible_board[c.x][c.y].
                setBackground(bg_backup[c.x%SUB_GRID_SIZE][c.y%SUB_GRID_SIZE]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sudoku, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_game) {
            newGame();
        }

        return super.onOptionsItemSelected(item);
    }
}

