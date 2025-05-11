package main;

import main.controller.AutocompleteController;
import main.controller.GameController;
import main.model.MovieDatabase;
import main.view.AutocompleteView;
import main.view.GameView;

/**
 * Main application class for the Movie Name Game.
 * Initializes all components and starts the game.
 */
public class MovieNameGame {
    private static final String MOVIE_FILE_PATH = "data/tmdb_5000_movies.csv";
    private static final String CREDITS_FILE_PATH = "data/tmdb_5000_credits.csv";

    private MovieDatabase movieDatabase;
    private GameController gameController;
    private AutocompleteController autocompleteController;
    private GameView gameView;
    private AutocompleteView autocompleteView;

    /**
     * Constructor
     */
    public MovieNameGame() {
        initializeComponents();
        connectComponents();
    }

    /**
     * Initialize all components
     */
    private void initializeComponents() {
        // Initialize models
        movieDatabase = new MovieDatabase();

        // Initialize controllers
        gameController = new GameController(movieDatabase);
        autocompleteController = new AutocompleteController(movieDatabase);

        // Initialize views
        gameView = new GameView();
        autocompleteView = new AutocompleteView();
    }

    /**
     * Connect components according to MVC pattern
     */
    private void connectComponents() {
        // Connect controllers with views
        gameController.setGameView(gameView);
        autocompleteController.setAutocompleteView(autocompleteView);

        // Connect views with controllers
        gameView.setGameController(gameController);
        gameView.setAutocompleteComponents(autocompleteController, autocompleteView);
        autocompleteView.setAutocompleteController(autocompleteController);

        // Set up movie selection listener (simplified)
        autocompleteView.setMovieSelectedListener(movie -> {
            // When a movie is selected from autocomplete, just return it
            // The actual game logic is handled in GameView
            System.out.println("Selected: " + movie.getTitle());
        });
    }

    /**
     * Start the application
     */
    public void start() {
        // Load movie data
        gameController.initialize(MOVIE_FILE_PATH, CREDITS_FILE_PATH);

        // Show main menu
        gameView.showMainMenu();
    }

    /**
     * Main method
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        MovieNameGame game = new MovieNameGame();
        game.start();
    }
}