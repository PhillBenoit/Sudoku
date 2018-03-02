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

public class SudokuActivity extends AppCompatActivity {

    //Size of the board to be generated.  For the most part, I tried to write code that could be
    //easily adjusted to accommodate a larger board
    private static final byte BOARD_SIZE = 9;
    private static final byte SUB_GRID_SIZE = (byte) Math.sqrt(BOARD_SIZE);

    //holds the complete board object
    private SudokuBoard board;

    //text at the bottom of the app
    private TextView game_text;

    //buttons for entering 1 - 9
    private Button[] input_button;

    //cursor object that holds the position of the last board square tapped
    private Cursor cursor;

    //background colors for highlighting squares
    private int YELLOW_BG, BLUE_BG, RED_BG;

    //holds the value of an empty board space
    private CharSequence space;

    //backup of the drawable background resources used to restore them after colors are used
    private Drawable[][] bg_backup;

    //count of the number of wrong guesses
    private byte wrong_count;

    //string to display at the bottom of the screen
    private String wrong_count_string;

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
        //Log.d("TESTBUG", "Height: " + height);
        //Log.d("TESTBUG", "Width: " + width);
        //Log.d("TESTBUG", "Font Size: " + font_size);


        //background colors
        YELLOW_BG = res.getColor(R.color.yellowBG);
        BLUE_BG = res.getColor(R.color.blueBG);
        RED_BG = res.getColor(R.color.redBG);

        //text used at the bottom of the screen
        wrong_count_string = res.getString(R.string.wrong);

        //text box at the bottom of the screen
        game_text = findViewById(R.id.game_text);
        game_text.setTextSize(font_size*0.6f);

        //new board object
        board = new SudokuBoard(BOARD_SIZE, SUB_GRID_SIZE);

        //new array to hold backgrounds
        bg_backup = new Drawable[BOARD_SIZE][BOARD_SIZE];

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

                //backs up default background
                bg_backup[row][column] = board.visible_board[row][column].getBackground();
            }
            //get buttons by name
            int id = res.getIdentifier("button"+(row+1),
                    "id",
                    getPackageName());
            input_button[row] = findViewById(id);
            input_button[row].setTextSize(font_size);
            input_button[row].setOnClickListener(new buttonClick(row));
        }

        //get default empty text
        space = board.visible_board[0][0].getText();

        //assign new cursor object
        cursor = new Cursor();
        cursor.x = 0;
        cursor.y = 0;

        startGame();
    }

    //gets a new game board while keeping the array of textviews associated with the visible board
    private void newGame() {
        board = new SudokuBoard(BOARD_SIZE, SUB_GRID_SIZE, board.visible_board);
        for (byte row = 0; row < BOARD_SIZE; row++)
            for (byte column = 0; column < BOARD_SIZE; column++) {
                board.visible_board[row][column].setText(space);
                board.visible_board[row][column].setBackground(bg_backup[row][column]);
            }
        startGame();
        boardClickEvent(cursor.x,cursor.y);
    }

    //reveals 3 tiles at random from each row
    private void startGame() {
        for (byte row = 0; row < BOARD_SIZE; row++) {
            boolean[] used = new boolean[BOARD_SIZE];
            for (byte revealed = 0; revealed < SUB_GRID_SIZE; revealed++) {
                byte random_number = GridOperations.getRandomNumber(BOARD_SIZE);
                while (used[random_number])
                    random_number = GridOperations.getRandomNumber(BOARD_SIZE);
                used[random_number] = true;
                GridOperations.setNumber(board, row, random_number, BOARD_SIZE, SUB_GRID_SIZE,
                        board.solution[row][random_number], BLUE_BG);
            }
        }

        //reset count
        wrong_count = 0;
    }

    //class to hold coordinates
    private class Cursor {
        byte x, y;
    }

    //Button click event
    private void buttonClickEvent(byte index) {

        //tests if clicked button is the right answer
        if (board.solution[cursor.x][cursor.y]==(index+1))
            //set number in visible grid
            GridOperations.setNumber(board, cursor.x, cursor.y, BOARD_SIZE, SUB_GRID_SIZE,
                    ++index, BLUE_BG);
        else {
            //increase the count of wrong guesses
            wrong_count++;

            //remove from possible numbers and set blue background when appropriate
            board.possible[cursor.x][cursor.y][index] = false;
            if (--board.possible_count[cursor.x][cursor.y] == 1)
                board.visible_board[cursor.x][cursor.y].setBackgroundColor(BLUE_BG);
        }

        //simulate board click to u[date the background
        boardClickEvent(cursor.x,cursor.y);
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
    private void boardClickEvent(byte x, byte y) {
        //checks for solved number as last clicked
        if (board.possible_count[cursor.x][cursor.y] == 0) {
            //clears all red backgrounds
            for (byte row = 0; row < BOARD_SIZE; row++)
                for (byte column = 0; column < BOARD_SIZE; column++) {
                //does not reset blue backgrounds
                if (board.possible_count[row][column] != 1)
                        board.visible_board[row][column].setBackground(bg_backup[row][column]);
                }

        //resets background on previously clicked location
        } else
            board.visible_board[cursor.x][cursor.y]
                    .setBackground(bg_backup[cursor.x][cursor.y]);

        //sets new location
        cursor.x = x;
        cursor.y = y;

        //automatically solves position if only 1 solution remaining
        if (board.possible_count[x][y] == 1)
            GridOperations.setNumber(board, x, y, BOARD_SIZE, SUB_GRID_SIZE,
                    board.solution[x][y], BLUE_BG);

        //sets background to yellow if blank
        if (board.visible_board[x][y].getText().equals(space))
            board.visible_board[x][y].setBackgroundColor(YELLOW_BG);
        else {
            //sets red background on visible number plus all numbers with the same value
            board.visible_board[x][y].setBackgroundColor(RED_BG);
            CharSequence value = board.visible_board[x][y].getText();
            for (byte row = 0; row < BOARD_SIZE; row++)
                for (byte column = 0; column < BOARD_SIZE; column++)
                    if (value.equals(board.visible_board[row][column].getText()))
                        board.visible_board[row][column].setBackgroundColor(RED_BG);
        }

        //only possible numbers are enabled
        for (byte step = 0; step < BOARD_SIZE; step++)
            input_button[step].setEnabled(board.possible[x][y][step]);

        //set bottom text with wrong guess count
        game_text.setText(String.format(wrong_count_string, wrong_count));
    }

    //board click listener that holds coordinate info
    private class boardClick implements View.OnClickListener {

        private byte x, y;

        boardClick(byte x, byte y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void onClick(View view) {
            boardClickEvent(x, y);
        }
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

