package main;

import main.controller.AutocompleteController;
import main.controller.GameController;
import main.model.MovieDatabase;
import main.view.TerminalGameUI;

import java.io.IOException;

/**
 * The main application class for the Movie Name Game.
 * Initializes all components including the database and controllers, then launches the TUI.
 * This game is based on the cine2nerdle.app battle mode, using movie data from TMDB.
 *
 * @author Group 21
 * @version May 12, 2025
 */
public class MovieNameGame {
    // Path to the TMDB 5000 movie dataset file.
    private static final String MOVIE_FILE_PATH   = "data/tmdb_5000_movies.csv";
    // Path to the TMDB 5000 credits dataset file.
    private static final String CREDITS_FILE_PATH = "data/tmdb_5000_credits.csv";

    private final MovieDatabase movieDatabase;
    private final GameController gameController;
    private final AutocompleteController autocompleteController;

    /**
     * Constructs the MovieNameGame instance.
     * Initializes the movie database and controllers, and loads movie data from file.
     */
    public MovieNameGame() {
        movieDatabase = new MovieDatabase();
        gameController = new GameController(movieDatabase);
        autocompleteController = new AutocompleteController(movieDatabase);
        gameController.initialize(MOVIE_FILE_PATH, CREDITS_FILE_PATH);
    }

    /**
     * Starts the terminal-based user interface and begins the game.
     *
     * @throws IOException if the terminal screen cannot be initialized
     */
    public void start() throws IOException {
        TerminalGameUI tui = new TerminalGameUI(gameController, autocompleteController);
        tui.run();
    }

    /**
     * Launches the Movie Name Game application.
     *
     * @param args command-line arguments (not used)
     * @throws IOException if TUI fails to launch
     */
    public static void main(String[] args) throws IOException {
        MovieNameGame app = new MovieNameGame();
        app.start();
    }
}