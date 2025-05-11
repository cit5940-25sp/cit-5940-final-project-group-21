package main.controller;

import com.opencsv.exceptions.CsvValidationException;
import main.model.GameState;
import main.model.Movie;
import main.model.Player;
import main.model.MovieDatabase;

import main.view.GameView;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller class for the Movie Name Game.
 * Handles game logic and user interactions.
 */
public class GameController {
    private GameState gameState;
    private MovieDatabase movieDatabase;
    private GameView gameView;
    private Timer turnTimer;
    private static final int TURN_TIME_SECONDS = 30;

    /**
     * Constructor for the GameController
     *
     * @param movieDatabase Movie database
     */
    public GameController(MovieDatabase movieDatabase) {
        this.movieDatabase = movieDatabase;
        this.gameState = new GameState();
    }

    /**
     * Set the game view
     *
     * @param gameView Game view
     */
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    /**
     * Initialize the game
     *
     * @param movieFilePath,creditsFilePath Path to movie data file
     */
    public void initialize(String movieFilePath, String creditsFilePath) {
        try {
            movieDatabase.loadMoviesFromCSV(movieFilePath);
            movieDatabase.loadCreditsFromCSV(creditsFilePath);
            gameView.showMessage("Loaded " + movieDatabase.getAllMovies().size() + " movies with credits");
        } catch (IOException | CsvValidationException e) {
            gameView.showError("Error loading movie or credits data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add a player to the game
     *
     * @param name Player name
     * @return true if player added successfully, false otherwise
     */
    public boolean addPlayer(String name) {
        boolean added = gameState.addPlayer(name);

        if (added) {
            gameView.updatePlayers(gameState.getPlayer1(), gameState.getPlayer2());

            // Only show connection selection for the FIRST player after second player joins
            if (gameState.getCurrentState() == GameState.State.SETTING_WIN_CONDITIONS &&
                    gameState.getPlayer2() != null &&
                    gameState.getPlayer1().getConnectionType() == null) {
                // Start with player 1
                gameView.showWinConditionSelection(gameState.getPlayer1(), getAllGenres());
            }
        }

        return added;
    }

    /**
     * Set a player's connection type (genre or person)
     *
     * @param player Player
     * @param connectionType Connection type ("genre" or "person")
     */
    public void setPlayerConnectionType(Player player, String connectionType) {
        player.setConnectionType(connectionType);

        // If player 1 just selected, show selection for player 2
        if (player == gameState.getPlayer1() && gameState.getPlayer2().getConnectionType() == null) {
            gameView.showWinConditionSelection(gameState.getPlayer2(), getAllGenres());
        }

        // Check if both players have set their connection types
        else if (gameState.getPlayer1().getConnectionType() != null &&
                gameState.getPlayer2().getConnectionType() != null) {

            System.out.println("DEBUG: Both players have selected. Starting game...");

            // Set the state to PLAYING before calling startGame()
            gameState.setState(GameState.State.PLAYING);
            gameState.setCurrentPlayer(gameState.getPlayer1()); // Make sure player 1 starts

            startGame();
        }
    }

    /**
     * Get a player's connection type
     *
     * @param player Player
     * @return Connection type ("genre" or "person")
     */
    public String getPlayerConnectionType(Player player) {
        return player.getConnectionType();
    }

    /**
     * Get the current player
     *
     * @return Current player
     */
    public Player getCurrentPlayer() {
        return gameState.getCurrentPlayer();
    }

    /**
     * Start the game
     */
    private void startGame() {
        System.out.println("DEBUG: startGame() method called");

        Movie startingMovie = movieDatabase.getRandomMovie();
        System.out.println("DEBUG: Random movie selected: " + (startingMovie != null ? startingMovie.getTitle() : "null"));

        try {
            gameState.startGame(startingMovie);
            System.out.println("DEBUG: gameState.startGame() completed");
        } catch (Exception e) {
            System.out.println("DEBUG: Error in gameState.startGame()");
            e.printStackTrace();
            return;
        }

        try {
            gameView.updateGameState(gameState);
            System.out.println("DEBUG: gameView.updateGameState() completed");
        } catch (Exception e) {
            System.out.println("DEBUG: Error in gameView.updateGameState()");
            e.printStackTrace();
            return;
        }

        gameView.showMessage("Game started with movie: " + startingMovie.getTitle());

        try {
            startTurnTimer();
            System.out.println("DEBUG: startTurnTimer() completed");
        } catch (Exception e) {
            System.out.println("DEBUG: Error in startTurnTimer()");
            e.printStackTrace();
        }
    }

    /**
     * Handle movie selection by genre
     *
     * @param movieTitle Selected movie title
     */
    public void selectMovieByGenre(String movieTitle) {
        List<Movie> movies = movieDatabase.findMoviesByTitle(movieTitle);

        if (movies.isEmpty()) {
            gameView.showError("Movie not found: " + movieTitle);
            return;
        }

        Movie selectedMovie = movies.get(0);
        Movie currentMovie = gameState.getCurrentMovie();

        // Check if the selected movie shares any genre with the current movie
        boolean hasSharedGenre = false;
        String sharedGenre = null;
        for (String genre : selectedMovie.getGenres()) {
            if (currentMovie.getGenres().contains(genre)) {
                hasSharedGenre = true;
                sharedGenre = genre;
                break;
            }
        }

        if (!hasSharedGenre) {
            gameView.showError("Selected movie does not share any genre with the current movie.");
            return;
        }

        // Use the existing selectMovie method with a genre connection
        boolean success = gameState.selectMovie(selectedMovie, "genre", sharedGenre);

        if (success) {
            resetTurnTimer();
            gameView.updateGameState(gameState);

            if (gameState.getCurrentState() == GameState.State.GAME_OVER) {
                endGame();
            }
        } else {
            gameView.showError("Invalid selection. Please try again.");
        }
    }

    /**
     * Handle movie selection by person
     *
     * @param movieTitle Selected movie title
     * @param connectionType Connection type (actor, director, etc.)
     * @param personName Name of the person making the connection
     */
    public void selectMovieByPerson(String movieTitle, String connectionType, String personName) {
        List<Movie> movies = movieDatabase.findMoviesByTitle(movieTitle);

        if (movies.isEmpty()) {
            gameView.showError("Movie not found: " + movieTitle);
            return;
        }

        Movie selectedMovie = movies.get(0);

        // First check if the connection actually exists
        if (!gameState.verifyConnection(gameState.getCurrentMovie(), selectedMovie, connectionType, personName)) {
            gameView.showError("Invalid connection. " + personName + " is not a " + connectionType + " in both movies.");
            return;
        }

        // Use the existing selectMovie method with person-specific connection
        boolean success = gameState.selectMovie(selectedMovie, connectionType, personName);

        if (success) {
            resetTurnTimer();
            gameView.updateGameState(gameState);

            if (gameState.getCurrentState() == GameState.State.GAME_OVER) {
                endGame();
            }
        } else {
            gameView.showError("Invalid selection. Person connection may have been used too many times.");
        }
    }

    /**
     * Handle movie selection (backward compatibility - not used with new logic)
     *
     * @param movieTitle Selected movie title
     * @param connection Connection type (actor, director, etc.)
     */
    public void selectMovie(String movieTitle, String connection) {
        // This method is now handled by selectMovieByGenre or selectMovieByPerson
        gameView.showError("Please use the connection method you selected at the beginning.");
    }

    /**
     * Start the turn timer
     */
    public void startTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
        }

        timerPaused = false; // Reset pause flag
        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            private int secondsLeft = TURN_TIME_SECONDS;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    if (!timerPaused) { // Only update display when not paused
                        gameView.updateTimer(secondsLeft);
                    }
                    secondsLeft--;
                } else {
                    handleTimeUp();
                    cancel();
                }
            }
        }, 0, 1000);
    }

    /**
     * Reset the turn timer
     */
    private void resetTurnTimer() {
        startTurnTimer();
    }

    /**
     * Handle time up event
     */
    private void handleTimeUp() {
        if (gameState.getCurrentState() == GameState.State.PLAYING) {
            // Stop the timer first
            if (turnTimer != null) {
                turnTimer.cancel();
                turnTimer = null;
            }

            // Clear any remaining timer display
            System.out.println(); // Move to a new line

            Player winner = (gameState.getCurrentPlayer() == gameState.getPlayer1())
                    ? gameState.getPlayer2() : gameState.getPlayer1();

            gameView.showMessage("Time's up! " + winner.getName() + " wins!");
            endGame();
        }
    }

    /**
     * End the game
     */
    private void endGame() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }

        Player winner = null;
        if (gameState.getPlayer1().hasWon()) {
            winner = gameState.getPlayer1();
        } else if (gameState.getPlayer2().hasWon()) {
            winner = gameState.getPlayer2();
        }

        if (winner != null) {
            gameView.showGameOver(winner);
        }
    }

    /**
     * Get all genres from the movie database
     *
     * @return Set of all genres
     */
    public Set<String> getAllGenres() {
        return movieDatabase.getAllGenres();
    }

    /**
     * Get movie suggestions based on title prefix
     *
     * @param prefix Title prefix
     * @param maxResults Maximum number of results
     * @return List of matching movies
     */
    public List<Movie> getMovieSuggestions(String prefix, int maxResults) {
        return movieDatabase.findMoviesByTitlePrefix(prefix, maxResults);
    }

    // Add flag and methods
    private boolean timerPaused = false;

    public void pauseTimer() {
        timerPaused = true;
    }

    public void resumeTimer() {
        timerPaused = false;
    }
}